package uz.dream.app.data

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import uz.dream.app.data.remote.ApiException
import uz.dream.app.data.remote.AuthSession
import uz.dream.app.data.remote.SupabaseClient
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.util.UUID

/** A problem the user can fix (empty field, limit reached). The message is shown as is. */
class UserError(message: String) : Exception(message)

/** Who is signed in. In local mode the tokens are empty. */
data class Session(
    val userId: String,
    val email: String,
    val fullName: String,
    val goal: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long
)

/**
 * The one place that knows where data comes from.
 * Online (Supabase configured): every change is sent to Supabase, Room keeps a copy for the screens.
 * Local mode (Supabase not configured): everything stays in Room on the phone.
 */
class DreamRepository(
    db: AppDatabase,
    private val prefs: SharedPreferences,
    private val api: SupabaseClient
) {
    private val dao = db.dao()

    /** true = connected to Supabase, false = local mode. */
    val online: Boolean get() = api.configured

    private val _session = MutableStateFlow(loadSession())
    val session: StateFlow<Session?> = _session.asStateFlow()

    // ---------- reading (always from Room, so screens are instant and work offline) ----------
    fun cards(kind: String): Flow<List<InfoCard>> = dao.cards(kind)
    fun card(id: String): Flow<InfoCard?> = dao.card(id)
    fun courses(): Flow<List<Course>> = dao.courses()
    fun course(id: String): Flow<Course?> = dao.course(id)
    fun lessons(courseId: String): Flow<List<Lesson>> = dao.lessons(courseId)
    fun lesson(id: String): Flow<Lesson?> = dao.lesson(id)
    fun universities(): Flow<List<University>> = dao.universities()
    fun doneIds(userId: String): Flow<List<String>> = dao.doneIds(userId)
    fun enrolledIds(userId: String): Flow<List<String>> = dao.enrolledIds(userId)
    fun interestIds(userId: String): Flow<List<String>> = dao.interestIds(userId)
    fun messages(roomId: String): Flow<List<Message>> = dao.messages(roomId)
    fun questions(courseId: String, userId: String): Flow<List<Question>> = dao.questions(courseId, userId)

    /** First start: put the built-in content into the empty database. */
    suspend fun seedIfEmpty() {
        if (dao.cardCount() == 0) {
            dao.replaceContent(SeedData.cards, SeedData.courses, SeedData.lessons, SeedData.universities)
        }
    }

    // ---------- accounts ----------

    /** Returns true when the user is signed in, false when Supabase first wants the e-mail confirmed. */
    suspend fun register(fullName: String, email: String, password: String): Boolean {
        val name = fullName.trim()
        val mail = email.trim().lowercase()
        if (name.length < 2) throw UserError("Напиши своё имя")
        if (!mail.contains("@") || !mail.contains(".")) throw UserError("Проверь адрес email")
        if (password.length < 6) throw UserError("Пароль: минимум 6 символов")

        if (online) {
            val auth = api.signUp(mail, password, name) ?: return false
            saveSession(auth.toSession(fallbackName = name, goal = ""))
            sync()
        } else {
            if (dao.findAccount(mail) != null) throw UserError("Этот email уже зарегистрирован")
            val salt = UUID.randomUUID().toString()
            val account = LocalAccount(mail, "local-" + UUID.randomUUID(), name, salt, hash(salt, password))
            dao.insertAccount(account)
            saveSession(Session(account.userId, mail, name, "", "", "", 0L))
        }
        return true
    }

    suspend fun login(email: String, password: String) {
        val mail = email.trim().lowercase()
        if (mail.isBlank() || password.isBlank()) throw UserError("Введи email и пароль")

        if (online) {
            val auth = api.signIn(mail, password)
            saveSession(auth.toSession(fallbackName = mail.substringBefore("@"), goal = ""))
            sync()
        } else {
            val account = dao.findAccount(mail)
            if (account == null || account.passwordHash != hash(account.salt, password)) {
                throw UserError("Неверный email или пароль")
            }
            saveSession(Session(account.userId, mail, account.fullName, prefs.getString("goal_" + account.userId, "") ?: "", "", "", 0L))
        }
    }

    suspend fun logout() {
        val s = _session.value
        if (online && s != null && s.accessToken.isNotBlank()) {
            try {
                api.signOut(s.accessToken)
            } catch (e: Exception) {
                // the phone forgets the session anyway
            }
        }
        clearSession()
    }

    // ---------- getting fresh data from Supabase ----------

    /** Downloads the content and this user's rows, then replaces the local copy. No-op in local mode. */
    suspend fun sync() {
        if (!online) return
        val s = _session.value ?: return
        val t = token()

        val cards = api.select("cards", "select=*&order=sort.asc", t).objects().map {
            InfoCard(
                it.getString("id"), it.getString("kind"), it.getString("section"), it.optInt("sort"),
                it.getString("title"), it.getString("body"), it.optString("source"), it.optString("source_url"),
                it.optString("status", "check"), it.optString("verified_at")
            )
        }
        val courses = api.select("courses", "select=*&order=sort.asc", t).objects().map {
            Course(
                it.getString("id"), it.optInt("sort"), it.getString("title"), it.getString("teacher"),
                it.getString("level"), it.optLong("price_uzs"), it.getString("glyph"), it.getString("description")
            )
        }
        val lessons = api.select("lessons", "select=*&order=sort.asc", t).objects().map {
            Lesson(
                it.getString("id"), it.getString("course_id"), it.optInt("sort"), it.getString("title"),
                it.optInt("minutes"), it.optBoolean("free"), it.optString("content")
            )
        }
        val universities = api.select("universities", "select=*&order=sort.asc", t).objects().map {
            University(
                it.getString("id"), it.optInt("sort"), it.getString("name"), it.getString("city"),
                it.getString("type"), it.optString("url")
            )
        }
        // An empty answer means schema.sql was not run yet: keep the built-in content.
        if (cards.isNotEmpty()) dao.replaceContent(cards, courses, lessons, universities)

        val uid = s.userId
        val done = api.select("progress", "select=card_id&user_id=eq.$uid", t).objects()
            .map { StepDone(uid, it.getString("card_id")) }
        val enrollments = api.select("enrollments", "select=*&user_id=eq.$uid", t).objects().map {
            Enrollment(
                uid, it.getString("course_id"), it.optLong("price_uzs"), it.optLong("platform_fee"),
                it.optLong("teacher_share"), millis(it.optString("created_at"))
            )
        }
        val interests = api.select("interests", "select=*&user_id=eq.$uid", t).objects().map {
            Interest(uid, it.getString("university_id"), it.optBoolean("consent", true), millis(it.optString("created_at")))
        }
        dao.replaceUserData(uid, done, enrollments, interests)

        val profile = api.select("profiles", "select=full_name,goal&id=eq.$uid", t).objects().firstOrNull()
        if (profile != null) {
            val current = _session.value ?: return
            val name = profile.optString("full_name").ifBlank { current.fullName }
            saveSession(current.copy(fullName = name, goal = profile.optString("goal")))
        }
    }

    // ---------- roadmap ----------
    suspend fun setStepDone(cardId: String, done: Boolean) {
        val s = requireSession()
        // the screen changes at once; if the server says no, the change is taken back
        if (done) dao.insertDone(StepDone(s.userId, cardId)) else dao.deleteDone(s.userId, cardId)
        if (!online) return
        try {
            val t = token()
            if (done) {
                api.insert("progress", JSONObject().put("user_id", s.userId).put("card_id", cardId), t, ifExists = "ignore")
            } else {
                api.delete("progress", "user_id=eq.${s.userId}&card_id=eq.$cardId", t)
            }
        } catch (e: Exception) {
            if (done) dao.deleteDone(s.userId, cardId) else dao.insertDone(StepDone(s.userId, cardId))
            throw e
        }
    }

    // ---------- courses ----------

    /** Demo purchase: no real money moves. The split is saved: 15% platform, 85% teacher. */
    suspend fun buyCourse(courseId: String) {
        val s = requireSession()
        val course = dao.findCourse(courseId) ?: throw UserError("Курс не найден")
        val fee = platformFee(course.priceUzs)
        val share = course.priceUzs - fee
        if (online) {
            val row = JSONObject()
                .put("user_id", s.userId).put("course_id", courseId)
                .put("price_uzs", course.priceUzs).put("platform_fee", fee).put("teacher_share", share)
            api.insert("enrollments", row, token(), ifExists = "ignore")
        }
        dao.insertEnrollment(Enrollment(s.userId, courseId, course.priceUzs, fee, share, System.currentTimeMillis()))
    }

    suspend fun refreshQuestions(courseId: String) {
        if (!online) return
        val s = _session.value ?: return
        val rows = api.select("questions", "select=*&course_id=eq.$courseId&user_id=eq.${s.userId}&order=created_at.asc", token())
        dao.insertQuestions(rows.objects().map { it.toQuestion() })
    }

    suspend fun askQuestion(courseId: String, text: String) {
        val s = requireSession()
        val body = text.trim()
        if (body.isEmpty()) throw UserError("Напиши вопрос")
        if (body.length > 1000) throw UserError("Вопрос слишком длинный (максимум 1000 символов)")
        if (dao.questionCount(courseId, s.userId) >= QUESTION_LIMIT) {
            throw UserError("В этом курсе можно задать только $QUESTION_LIMIT вопроса преподавателю")
        }
        if (online) {
            val row = JSONObject().put("user_id", s.userId).put("course_id", courseId).put("body", body)
            dao.insertQuestions(api.insert("questions", row, token()).objects().map { it.toQuestion() })
        } else {
            dao.insertQuestions(
                listOf(Question(UUID.randomUUID().toString(), courseId, s.userId, body, "", System.currentTimeMillis()))
            )
        }
    }

    // ---------- community rooms ----------
    suspend fun refreshRoom(roomId: String) {
        if (!online) return
        if (_session.value == null) return
        val rows = api.select("messages", "select=*&room_id=eq.$roomId&order=created_at.desc&limit=100", token())
        dao.insertMessages(rows.objects().map { it.toMessage() })
    }

    suspend fun sendMessage(roomId: String, text: String) {
        val s = requireSession()
        val body = text.trim()
        if (body.isEmpty()) return
        if (body.length > 1000) throw UserError("Сообщение слишком длинное (максимум 1000 символов)")
        if (online) {
            val row = JSONObject().put("user_id", s.userId).put("room_id", roomId).put("author", s.fullName).put("body", body)
            dao.insertMessages(api.insert("messages", row, token()).objects().map { it.toMessage() })
        } else {
            dao.insertMessages(
                listOf(Message(UUID.randomUUID().toString(), roomId, s.userId, s.fullName, body, System.currentTimeMillis()))
            )
        }
    }

    // ---------- universities ----------

    /** on = true is called only after the user ticked the consent box. */
    suspend fun setInterest(universityId: String, on: Boolean) {
        val s = requireSession()
        if (online) {
            val t = token()
            if (on) {
                val row = JSONObject().put("user_id", s.userId).put("university_id", universityId).put("consent", true)
                api.insert("interests", row, t, ifExists = "ignore")
            } else {
                api.delete("interests", "user_id=eq.${s.userId}&university_id=eq.$universityId", t)
            }
        }
        if (on) {
            dao.insertInterest(Interest(s.userId, universityId, true, System.currentTimeMillis()))
        } else {
            dao.deleteInterest(s.userId, universityId)
        }
    }

    // ---------- profile ----------
    suspend fun setGoal(goal: String) {
        val s = requireSession()
        if (online) {
            val row = JSONObject().put("id", s.userId).put("full_name", s.fullName).put("goal", goal)
            api.insert("profiles", row, token(), ifExists = "merge")
        } else {
            prefs.edit().putString("goal_" + s.userId, goal).apply()
        }
        saveSession((_session.value ?: s).copy(goal = goal))
    }

    // ---------- session ----------
    private fun requireSession(): Session = _session.value ?: throw UserError("Сначала войди в аккаунт")

    private val refreshLock = Mutex()

    /** The access token for Supabase calls. Supabase tokens live about an hour, so renew when needed. */
    private suspend fun token(): String? {
        if (!online) return null
        return refreshLock.withLock {
            val s = requireSession()
            if (s.expiresAt - System.currentTimeMillis() > 60_000L) {
                s.accessToken
            } else {
                try {
                    val fresh = api.refresh(s.refreshToken)
                    saveSession(fresh.toSession(fallbackName = s.fullName, goal = s.goal))
                    fresh.accessToken
                } catch (e: ApiException) {
                    clearSession()
                    throw UserError("Сессия закончилась. Войди ещё раз.")
                }
            }
        }
    }

    private fun AuthSession.toSession(fallbackName: String, goal: String) = Session(
        userId = userId,
        email = email,
        fullName = fullName.ifBlank { fallbackName },
        goal = goal,
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresAt = expiresAt
    )

    private fun loadSession(): Session? {
        val id = prefs.getString("user_id", null) ?: return null
        return Session(
            userId = id,
            email = prefs.getString("email", "") ?: "",
            fullName = prefs.getString("full_name", "") ?: "",
            goal = prefs.getString("goal", "") ?: "",
            accessToken = prefs.getString("access_token", "") ?: "",
            refreshToken = prefs.getString("refresh_token", "") ?: "",
            expiresAt = prefs.getLong("expires_at", 0L)
        )
    }

    private fun saveSession(s: Session) {
        prefs.edit()
            .putString("user_id", s.userId)
            .putString("email", s.email)
            .putString("full_name", s.fullName)
            .putString("goal", s.goal)
            .putString("access_token", s.accessToken)
            .putString("refresh_token", s.refreshToken)
            .putLong("expires_at", s.expiresAt)
            .apply()
        _session.value = s
    }

    private fun clearSession() {
        prefs.edit()
            .remove("user_id").remove("email").remove("full_name").remove("goal")
            .remove("access_token").remove("refresh_token").remove("expires_at")
            .apply()
        _session.value = null
    }

    // ---------- small helpers ----------
    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }

    private fun JSONObject.toMessage() = Message(
        id = get("id").toString(),
        roomId = getString("room_id"),
        userId = getString("user_id"),
        author = optString("author"),
        body = getString("body"),
        createdAt = millis(optString("created_at"))
    )

    private fun JSONObject.toQuestion() = Question(
        id = get("id").toString(),
        courseId = getString("course_id"),
        userId = getString("user_id"),
        body = getString("body"),
        answer = optString("answer"),
        createdAt = millis(optString("created_at"))
    )

    /** "2026-11-12T16:30:00.123456+00:00" -> epoch millis. */
    private fun millis(iso: String): Long =
        try {
            OffsetDateTime.parse(iso).toInstant().toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

    private fun hash(salt: String, password: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest((salt + password).toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    companion object {
        const val QUESTION_LIMIT = 3
        const val PLATFORM_PERCENT = 15L

        /** The platform's part of a price: 15%. The teacher gets the rest (85%). */
        fun platformFee(priceUzs: Long): Long = priceUzs * PLATFORM_PERCENT / 100L
    }
}

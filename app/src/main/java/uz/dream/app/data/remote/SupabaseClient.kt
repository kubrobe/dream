package uz.dream.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** The server answered with an error (wrong password, no permission, ...). */
class ApiException(val status: Int, message: String) : Exception(message)

/** No internet, or Supabase could not be reached. */
class OfflineException : Exception("Нет соединения с сервером. Проверь интернет и попробуй ещё раз.")

/** What Supabase Auth returns after login. expiresAt = epoch millis when accessToken stops working. */
class AuthSession(
    val userId: String,
    val email: String,
    val fullName: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long
)

/**
 * Talks to Supabase over HTTPS: Auth (/auth/v1) and the database REST API (/rest/v1).
 * Uses only classes built into Android (HttpURLConnection + org.json), so no extra libraries.
 * Every call runs on Dispatchers.IO, never on the screen's main thread.
 */
class SupabaseClient(
    url: String = SupabaseConfig.URL,
    private val key: String = SupabaseConfig.ANON_KEY
) {
    private val baseUrl = url.trim().trimEnd('/')

    /** false = the URL and key are not filled in yet -> the app works in local mode. */
    val configured: Boolean get() = baseUrl.startsWith("https://") && key.isNotBlank()

    // ---------- accounts ----------

    /** Returns null when Supabase wants the e-mail confirmed first (no session yet). */
    suspend fun signUp(email: String, password: String, fullName: String): AuthSession? {
        val body = JSONObject()
            .put("email", email)
            .put("password", password)
            .put("data", JSONObject().put("full_name", fullName))
        val json = JSONObject(request("POST", "/auth/v1/signup", null, body.toString()))
        return if (json.has("access_token")) session(json) else null
    }

    suspend fun signIn(email: String, password: String): AuthSession {
        val body = JSONObject().put("email", email).put("password", password)
        return session(JSONObject(request("POST", "/auth/v1/token?grant_type=password", null, body.toString())))
    }

    suspend fun refresh(refreshToken: String): AuthSession {
        val body = JSONObject().put("refresh_token", refreshToken)
        return session(JSONObject(request("POST", "/auth/v1/token?grant_type=refresh_token", null, body.toString())))
    }

    suspend fun signOut(token: String) {
        request("POST", "/auth/v1/logout", token, "{}")
    }

    private fun session(json: JSONObject): AuthSession {
        val user = json.getJSONObject("user")
        val meta = user.optJSONObject("user_metadata")
        return AuthSession(
            userId = user.getString("id"),
            email = user.optString("email"),
            fullName = meta?.optString("full_name").orEmpty(),
            accessToken = json.getString("access_token"),
            refreshToken = json.optString("refresh_token"),
            expiresAt = System.currentTimeMillis() + json.optLong("expires_in", 3600L) * 1000L
        )
    }

    // ---------- database ----------

    /** SELECT. query example: "select=*&order=sort.asc" */
    suspend fun select(table: String, query: String, token: String?): JSONArray =
        array(request("GET", "/rest/v1/$table?$query", token, null))

    /**
     * INSERT one row and get it back.
     * ifExists: null = error when the row exists, "ignore" = keep the old row, "merge" = update it.
     */
    suspend fun insert(table: String, row: JSONObject, token: String?, ifExists: String? = null): JSONArray {
        val prefer = when (ifExists) {
            "ignore" -> "return=representation,resolution=ignore-duplicates"
            "merge" -> "return=representation,resolution=merge-duplicates"
            else -> "return=representation"
        }
        return array(request("POST", "/rest/v1/$table", token, row.toString(), prefer))
    }

    /** DELETE rows that match the filter, e.g. "card_id=eq.r_topik". */
    suspend fun delete(table: String, filter: String, token: String?) {
        request("DELETE", "/rest/v1/$table?$filter", token, null)
    }

    private fun array(text: String): JSONArray =
        try {
            if (text.isBlank()) JSONArray() else JSONArray(text)
        } catch (e: JSONException) {
            JSONArray()
        }

    // ---------- one HTTP request ----------
    private suspend fun request(
        method: String, path: String, token: String?, body: String?, prefer: String? = null
    ): String = withContext(Dispatchers.IO) {
        val conn = try {
            URL(baseUrl + path).openConnection() as HttpURLConnection
        } catch (e: IOException) {
            throw OfflineException()
        }
        try {
            conn.requestMethod = method
            conn.connectTimeout = 15_000
            conn.readTimeout = 20_000
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("apikey", key)
            // Signed-in user: their own token. Not signed in: the old "eyJ..." anon keys are sent
            // as the token too; the new "sb_publishable_..." keys go in the apikey header only.
            if (token != null) {
                conn.setRequestProperty("Authorization", "Bearer $token")
            } else if (key.startsWith("eyJ")) {
                conn.setRequestProperty("Authorization", "Bearer $key")
            }
            if (prefer != null) conn.setRequestProperty("Prefer", prefer)
            if (body != null) {
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
            if (code !in 200..299) throw ApiException(code, errorText(code, text))
            text
        } catch (e: IOException) {
            throw OfflineException()
        } finally {
            conn.disconnect()
        }
    }

    /** Turns Supabase's English error into a short Russian line for the user. */
    private fun errorText(code: Int, text: String): String {
        val json = try {
            JSONObject(text)
        } catch (e: JSONException) {
            JSONObject()
        }
        val raw = listOf("msg", "message", "error_description", "error")
            .map { json.optString(it) }
            .firstOrNull { it.isNotBlank() && it != "null" }
            ?: ""
        val low = raw.lowercase()
        return when {
            "invalid login credentials" in low -> "Неверный email или пароль"
            "already registered" in low || "already been registered" in low -> "Этот email уже зарегистрирован"
            "email not confirmed" in low -> "Email не подтверждён. Открой письмо от Supabase и перейди по ссылке."
            "password should be at least" in low || "weak" in low -> "Пароль слишком простой: нужно минимум 6 символов"
            "invalid format" in low || "is invalid" in low -> "Проверь адрес email"
            "rate limit" in low -> "Слишком много попыток. Подожди минуту."
            code == 401 || code == 403 -> "Нет доступа. Войди в аккаунт ещё раз."
            code == 404 -> "Таблица не найдена. Запусти supabase/schema.sql в Supabase."
            raw.isNotBlank() -> raw
            else -> "Ошибка сервера ($code)"
        }
    }
}

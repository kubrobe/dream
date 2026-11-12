package uz.dream.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

// All SQL the app runs on the phone.
// Flow<...> = live data: the screen redraws by itself when the table changes.

@Dao
abstract class DreamDao {

    // ---------- content ----------
    @Query("SELECT * FROM cards WHERE kind = :kind ORDER BY sort")
    abstract fun cards(kind: String): Flow<List<InfoCard>>

    @Query("SELECT * FROM cards WHERE id = :id")
    abstract fun card(id: String): Flow<InfoCard?>

    @Query("SELECT COUNT(*) FROM cards")
    abstract suspend fun cardCount(): Int

    @Query("SELECT * FROM courses ORDER BY sort")
    abstract fun courses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE id = :id")
    abstract fun course(id: String): Flow<Course?>

    @Query("SELECT * FROM courses WHERE id = :id")
    abstract suspend fun findCourse(id: String): Course?

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY sort")
    abstract fun lessons(courseId: String): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE id = :id")
    abstract fun lesson(id: String): Flow<Lesson?>

    @Query("SELECT * FROM universities ORDER BY sort")
    abstract fun universities(): Flow<List<University>>

    @Query("DELETE FROM cards") protected abstract suspend fun clearCards()
    @Query("DELETE FROM courses") protected abstract suspend fun clearCourses()
    @Query("DELETE FROM lessons") protected abstract suspend fun clearLessons()
    @Query("DELETE FROM universities") protected abstract suspend fun clearUniversities()

    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertCards(items: List<InfoCard>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertCourses(items: List<Course>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertLessons(items: List<Lesson>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertUniversities(items: List<University>)

    /** Replaces all content in one transaction, so a screen never sees half of it. */
    @Transaction
    open suspend fun replaceContent(
        cards: List<InfoCard>, courses: List<Course>, lessons: List<Lesson>, universities: List<University>
    ) {
        clearCards(); clearCourses(); clearLessons(); clearUniversities()
        insertCards(cards)
        insertCourses(courses)
        insertLessons(lessons)
        insertUniversities(universities)
    }

    // ---------- what the user did ----------
    @Query("SELECT cardId FROM progress WHERE userId = :userId")
    abstract fun doneIds(userId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDone(item: StepDone)

    @Query("DELETE FROM progress WHERE userId = :userId AND cardId = :cardId")
    abstract suspend fun deleteDone(userId: String, cardId: String)

    @Query("SELECT courseId FROM enrollments WHERE userId = :userId")
    abstract fun enrolledIds(userId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertEnrollment(item: Enrollment)

    @Query("SELECT universityId FROM interests WHERE userId = :userId")
    abstract fun interestIds(userId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertInterest(item: Interest)

    @Query("DELETE FROM interests WHERE userId = :userId AND universityId = :universityId")
    abstract suspend fun deleteInterest(userId: String, universityId: String)

    @Query("DELETE FROM progress WHERE userId = :userId") protected abstract suspend fun clearDone(userId: String)
    @Query("DELETE FROM enrollments WHERE userId = :userId") protected abstract suspend fun clearEnrollments(userId: String)
    @Query("DELETE FROM interests WHERE userId = :userId") protected abstract suspend fun clearInterests(userId: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertDoneList(items: List<StepDone>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertEnrollments(items: List<Enrollment>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertInterests(items: List<Interest>)

    /** Replaces this user's rows with what the server has. */
    @Transaction
    open suspend fun replaceUserData(
        userId: String, done: List<StepDone>, enrollments: List<Enrollment>, interests: List<Interest>
    ) {
        clearDone(userId); clearEnrollments(userId); clearInterests(userId)
        insertDoneList(done)
        insertEnrollments(enrollments)
        insertInterests(interests)
    }

    // ---------- rooms and questions ----------
    @Query("SELECT * FROM messages WHERE roomId = :roomId ORDER BY createdAt")
    abstract fun messages(roomId: String): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMessages(items: List<Message>)

    @Query("SELECT * FROM questions WHERE courseId = :courseId AND userId = :userId ORDER BY createdAt")
    abstract fun questions(courseId: String, userId: String): Flow<List<Question>>

    @Query("SELECT COUNT(*) FROM questions WHERE courseId = :courseId AND userId = :userId")
    abstract suspend fun questionCount(courseId: String, userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertQuestions(items: List<Question>)

    // ---------- accounts on the phone (only without Supabase) ----------
    @Query("SELECT * FROM local_accounts WHERE email = :email LIMIT 1")
    abstract suspend fun findAccount(email: String): LocalAccount?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertAccount(account: LocalAccount)
}

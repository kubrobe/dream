package uz.dream.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Each @Entity class is one table in the phone's SQLite database (Room).
// The same tables exist in Supabase (see supabase/schema.sql); Room is the local copy.
// Text columns without a suffix are Russian; *En is English, *Uz is Uzbek (Latin).
// Localized.kt picks the right one for the language the user chose.

/** One card of the roadmap or of the after-arrival guide. status: "verified" | "check" | "tip". */
@Entity(tableName = "cards")
data class InfoCard(
    @PrimaryKey val id: String,
    val kind: String,          // "roadmap" or "guide"
    val section: String,       // key from Sections
    val sort: Int,
    val title: String,
    val body: String,
    val source: String,
    val sourceUrl: String,
    val status: String,
    val verifiedAt: String,    // "2026-11-12", or "" when not verified
    val titleEn: String = "",
    val titleUz: String = "",
    val bodyEn: String = "",
    val bodyUz: String = "",
    val sourceEn: String = "",
    val sourceUz: String = ""
)

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey val id: String,
    val sort: Int,
    val title: String,
    val teacher: String,
    val level: String,
    val priceUzs: Long,
    val glyph: String,         // big hangul sign on the course tile
    val description: String,
    val titleEn: String = "",
    val titleUz: String = "",
    val levelEn: String = "",
    val levelUz: String = "",
    val descriptionEn: String = "",
    val descriptionUz: String = ""
)

@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey val id: String,
    val courseId: String,
    val sort: Int,
    val title: String,
    val minutes: Int,
    val free: Boolean,         // free lessons are open before buying
    val content: String,
    val titleEn: String = "",
    val titleUz: String = "",
    val contentEn: String = "",
    val contentUz: String = ""
)

@Entity(tableName = "universities")
data class University(
    @PrimaryKey val id: String,
    val sort: Int,
    val name: String,
    val city: String,
    val type: String,
    val url: String,
    val cityEn: String = "",
    val cityUz: String = "",
    val typeEn: String = "",
    val typeUz: String = ""
)

/** The user ticked this roadmap step. */
@Entity(tableName = "progress", primaryKeys = ["userId", "cardId"])
data class StepDone(val userId: String, val cardId: String)

/** A bought course. platformFee is 15% of the price, teacherShare is 85%. */
@Entity(tableName = "enrollments", primaryKeys = ["userId", "courseId"])
data class Enrollment(
    val userId: String,
    val courseId: String,
    val priceUzs: Long,
    val platformFee: Long,
    val teacherShare: Long,
    val createdAt: Long
)

/** The user agreed to show their profile to this university. */
@Entity(tableName = "interests", primaryKeys = ["userId", "universityId"])
data class Interest(val userId: String, val universityId: String, val consent: Boolean, val createdAt: Long)

/** One message in a community room. */
@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val roomId: String,
    val userId: String,
    val author: String,
    val body: String,
    val createdAt: Long
)

/** A question to the teacher of a course (3 per course). */
@Entity(tableName = "questions")
data class Question(
    @PrimaryKey val id: String,
    val courseId: String,
    val userId: String,
    val body: String,
    val answer: String,
    val createdAt: Long
)

/** Used only when Supabase is not configured: accounts kept on the phone. */
@Entity(tableName = "local_accounts")
data class LocalAccount(
    @PrimaryKey val email: String,
    val userId: String,
    val fullName: String,
    val salt: String,
    val passwordHash: String   // never the plain password
)

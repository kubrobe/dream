package uz.dream.app.data

import androidx.annotation.StringRes
import uz.dream.app.R

/** A group of cards. Every section also has its own community room. */
class Section(val key: String, val kind: String, @StringRes val title: Int, val glyph: String)

object Sections {
    val all = listOf(
        Section("lang", "roadmap", R.string.section_lang, "말"),
        Section("univ", "roadmap", R.string.section_univ, "학"),
        Section("docs", "roadmap", R.string.section_docs, "문"),
        Section("visa", "roadmap", R.string.section_visa, "비"),
        Section("arrival", "roadmap", R.string.section_arrival, "길"),
        Section("first90", "guide", R.string.section_first90, "첫"),
        Section("health", "guide", R.string.section_health, "몸"),
        Section("work", "guide", R.string.section_work, "일"),
        Section("life", "guide", R.string.section_life, "삶")
    )

    fun of(kind: String): List<Section> = all.filter { it.kind == kind }

    fun find(key: String): Section? = all.firstOrNull { it.key == key }

    @StringRes
    fun title(key: String): Int = find(key)?.title ?: R.string.section_unknown

    fun glyph(key: String): String = find(key)?.glyph ?: "꿈"
}

/** Room ids: "topic_<section key>" and "course_<course id>". */
object Rooms {
    fun topic(sectionKey: String) = "topic_$sectionKey"
    fun course(courseId: String) = "course_$courseId"
    fun isCourse(roomId: String) = roomId.startsWith("course_")
    fun courseId(roomId: String) = roomId.removePrefix("course_")
    fun sectionKey(roomId: String) = roomId.removePrefix("topic_")
}

/** Study goals. The key is saved in the profile; the label is translated on screen. */
enum class Goal(val key: String, @StringRes val label: Int) {
    BACHELOR("bachelor", R.string.goal_bachelor),
    MASTER("master", R.string.goal_master),
    LANGUAGE("language", R.string.goal_language),
    EXCHANGE("exchange", R.string.goal_exchange);

    companion object {
        /** Also understands the Russian words saved by the first version of the app. */
        fun of(saved: String): Goal? = when (saved.trim()) {
            "bachelor", "Бакалавриат" -> BACHELOR
            "master", "Магистратура" -> MASTER
            "language", "Языковые курсы" -> LANGUAGE
            "exchange", "Обмен" -> EXCHANGE
            else -> null
        }
    }
}

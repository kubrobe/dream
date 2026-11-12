package uz.dream.app.data

/** A group of cards. Every section also has its own community room. */
class Section(val key: String, val kind: String, val title: String, val glyph: String)

object Sections {
    val all = listOf(
        Section("lang", "roadmap", "Цель и язык", "말"),
        Section("univ", "roadmap", "Вуз и стипендии", "학"),
        Section("docs", "roadmap", "Документы", "문"),
        Section("visa", "roadmap", "Виза", "비"),
        Section("arrival", "roadmap", "Перед вылетом", "길"),
        Section("first90", "guide", "Первые 90 дней", "첫"),
        Section("health", "guide", "Здоровье", "몸"),
        Section("work", "guide", "Работа", "일"),
        Section("life", "guide", "Быт", "삶")
    )

    fun of(kind: String): List<Section> = all.filter { it.kind == kind }

    fun title(key: String): String = all.firstOrNull { it.key == key }?.title ?: key
}

/** Room ids: "topic_<section key>" and "course_<course id>". */
object Rooms {
    fun topic(sectionKey: String) = "topic_$sectionKey"
    fun course(courseId: String) = "course_$courseId"
    fun isCourse(roomId: String) = roomId.startsWith("course_")
    fun courseId(roomId: String) = roomId.removePrefix("course_")
    fun sectionKey(roomId: String) = roomId.removePrefix("topic_")
}

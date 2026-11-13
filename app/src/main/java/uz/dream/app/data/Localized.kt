package uz.dream.app.data

// Picks the text in the user's language. If a translation is missing, the Russian original is shown.

private fun pick(lang: Lang, ru: String, en: String, uz: String): String = when (lang) {
    Lang.RU -> ru
    Lang.EN -> en.ifBlank { ru }
    Lang.UZ -> uz.ifBlank { ru }
}

fun InfoCard.title(lang: Lang) = pick(lang, title, titleEn, titleUz)
fun InfoCard.body(lang: Lang) = pick(lang, body, bodyEn, bodyUz)
fun InfoCard.source(lang: Lang) = pick(lang, source, sourceEn, sourceUz)

fun Course.title(lang: Lang) = pick(lang, title, titleEn, titleUz)
fun Course.level(lang: Lang) = pick(lang, level, levelEn, levelUz)
fun Course.description(lang: Lang) = pick(lang, description, descriptionEn, descriptionUz)

fun Lesson.title(lang: Lang) = pick(lang, title, titleEn, titleUz)
fun Lesson.content(lang: Lang) = pick(lang, content, contentEn, contentUz)

fun University.city(lang: Lang) = pick(lang, city, cityEn, cityUz)
fun University.type(lang: Lang) = pick(lang, type, typeEn, typeUz)

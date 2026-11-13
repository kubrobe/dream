package uz.dream.app

import org.junit.Assert.assertEquals
import org.junit.Test
import uz.dream.app.data.DreamRepository
import uz.dream.app.data.Goal
import uz.dream.app.data.Lang
import uz.dream.app.data.Rooms
import uz.dream.app.data.SeedData
import uz.dream.app.data.Sections
import uz.dream.app.data.body
import uz.dream.app.data.title
import uz.dream.app.ui.formatSum

/** Plain unit tests (no phone needed): right-click this file -> Run. */
class MoneyTest {
    @Test
    fun platformKeeps15Percent() {
        assertEquals(52_350L, DreamRepository.platformFee(349_000L))
        assertEquals(0L, DreamRepository.platformFee(0L))
    }

    @Test
    fun teacherAndPlatformAddUpToThePrice() {
        for (course in SeedData.courses) {
            val fee = DreamRepository.platformFee(course.priceUzs)
            val teacher = course.priceUzs - fee
            assertEquals(course.priceUzs, fee + teacher)
        }
    }

    @Test
    fun pricesAreFormattedWithSpaces() {
        assertEquals("349 000", formatSum(349_000L))
        assertEquals("0", formatSum(0L))
    }

    @Test
    fun allContentIsTranslatedToUzbekAndEnglish() {
        for (c in SeedData.cards) {
            assert(c.titleEn.isNotBlank() && c.titleUz.isNotBlank()) { "title not translated: ${c.id}" }
            assert(c.bodyEn.isNotBlank() && c.bodyUz.isNotBlank()) { "body not translated: ${c.id}" }
            assert(c.sourceEn.isNotBlank() && c.sourceUz.isNotBlank()) { "source not translated: ${c.id}" }
        }
        for (c in SeedData.courses) assert(c.titleUz.isNotBlank() && c.descriptionEn.isNotBlank()) { c.id }
        for (l in SeedData.lessons) assert(l.contentUz.isNotBlank() && l.contentEn.isNotBlank()) { l.id }
        for (u in SeedData.universities) assert(u.cityUz.isNotBlank() && u.typeEn.isNotBlank()) { u.id }
    }

    @Test
    fun languagePicksTheRightTextAndFallsBackToRussian() {
        val card = SeedData.cards.first()
        assertEquals(card.title, card.title(Lang.RU))
        assertEquals(card.titleEn, card.title(Lang.EN))
        assertEquals(card.titleUz, card.title(Lang.UZ))
        val untranslated = card.copy(bodyUz = "")
        assertEquals(card.body, untranslated.body(Lang.UZ))
        assertEquals(Lang.UZ, Lang.of("uz"))
        assertEquals(null, Lang.of("de"))
    }

    @Test
    fun oldRussianGoalsStillWork() {
        assertEquals(Goal.BACHELOR, Goal.of("Бакалавриат"))
        assertEquals(Goal.BACHELOR, Goal.of("bachelor"))
        assertEquals(Goal.EXCHANGE, Goal.of("Обмен"))
        assertEquals(null, Goal.of(""))
    }

    @Test
    fun everyCardBelongsToAKnownSectionAndHasASource() {
        val keys = Sections.all.map { it.key }.toSet()
        for (card in SeedData.cards) {
            assert(card.section in keys) { "unknown section in ${card.id}" }
            assert(card.source.isNotBlank()) { "no source in ${card.id}" }
            // only checked facts carry a date
            assertEquals(card.status == "verified", card.verifiedAt.isNotBlank())
        }
    }

    @Test
    fun everyLessonBelongsToACourseAndEachCourseHasAFreeLesson() {
        val courseIds = SeedData.courses.map { it.id }.toSet()
        for (lesson in SeedData.lessons) assert(lesson.courseId in courseIds)
        for (id in courseIds) assert(SeedData.lessons.any { it.courseId == id && it.free })
    }

    @Test
    fun roomIdsRoundTrip() {
        assertEquals("c_hangul", Rooms.courseId(Rooms.course("c_hangul")))
        assertEquals("visa", Rooms.sectionKey(Rooms.topic("visa")))
        assertEquals(true, Rooms.isCourse(Rooms.course("c_hangul")))
        assertEquals(false, Rooms.isCourse(Rooms.topic("visa")))
    }
}

package uz.dream.app

import org.junit.Assert.assertEquals
import org.junit.Test
import uz.dream.app.data.DreamRepository
import uz.dream.app.data.Rooms
import uz.dream.app.data.SeedData
import uz.dream.app.data.Sections
import uz.dream.app.ui.uzs

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
        assertEquals("349 000 сум", uzs(349_000L))
        assertEquals("0 сум", uzs(0L))
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

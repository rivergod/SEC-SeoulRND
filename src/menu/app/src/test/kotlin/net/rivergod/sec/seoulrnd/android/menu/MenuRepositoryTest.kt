package net.rivergod.sec.seoulrnd.android.menu

import kotlinx.coroutines.runBlocking
import net.rivergod.sec.seoulrnd.android.menu.data.MenuDataSource
import net.rivergod.sec.seoulrnd.android.menu.data.MenuLoadException
import net.rivergod.sec.seoulrnd.android.menu.data.MenuRepository
import net.rivergod.sec.seoulrnd.android.menu.data.SampleMenuDataSource
import net.rivergod.sec.seoulrnd.android.menu.dto.Cafeteria
import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO
import net.rivergod.sec.seoulrnd.android.menu.dto.MealType
import net.rivergod.sec.seoulrnd.android.menu.dto.MenuArea
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime

class MenuRepositoryTest {

    private val date = LocalDate.of(2026, 10, 2)

    private class FailingSource(override val cafeteria: Cafeteria) : MenuDataSource {
        override val isSample = false
        override suspend fun fetch(date: LocalDate): List<CuisineDTO> = throw IOException("offline")
    }

    @Test
    fun sampleDataCoversBothCafeteriasAndChangesByDate() = runBlocking {
        val repository = MenuRepository(SampleMenuDataSource.all())
        val day1 = repository.load(date)
        val day2 = repository.load(date.plusDays(1))

        assertTrue(repository.isSample)
        for (cafeteria in Cafeteria.entries) {
            assertTrue("$cafeteria", day1.cuisines.any { it.cafeteria == cafeteria && it.sideDishes.isNotEmpty() })
        }
        MealType.entries.forEach { meal -> assertTrue("$meal is empty", day1.forMeal(meal).isNotEmpty()) }
        assertEquals(day1.cuisines.size, day1.cuisines.map { it.id }.toSet().size)
        assertNotEquals(day1.cuisines.map { it.title }, day2.cuisines.map { it.title })
    }

    @Test
    fun keepsOtherCafeteriaWhenOneFails() = runBlocking {
        val repository = MenuRepository(
            listOf(FailingSource(Cafeteria.CAFETERIA_1), SampleMenuDataSource(Cafeteria.CAFETERIA_2))
        )
        val day = repository.load(date)

        assertEquals(setOf(Cafeteria.CAFETERIA_1), day.failedCafeterias)
        assertTrue(day.cuisines.isNotEmpty() && day.cuisines.all { it.cafeteria == Cafeteria.CAFETERIA_2 })

        // 실패한 식당도 목록에 묶음으로 남아 다시 시도할 수 있게 한다
        val sections = MenuUiState(
            date = date,
            loadState = MenuLoadState.Loaded(day),
            selectedMeal = MealType.LUNCH,
            firstCafeteria = Cafeteria.CAFETERIA_1,
        ).sections
        assertEquals(Cafeteria.CAFETERIA_1, sections.first().cafeteria)
        assertTrue(sections.first().failed)
        assertEquals(listOf(MenuArea.DINE_IN, MenuArea.TAKE_OUT), sections.drop(1).map { it.area })
        assertEquals("2식당 Take Out", sections.last().title)
    }

    @Test(expected = MenuLoadException::class)
    fun failsWhenAllCafeteriasFail(): Unit = runBlocking {
        MenuRepository(Cafeteria.entries.map { FailingSource(it) }).load(date)
    }

    @Test
    fun ordersSectionsByPreferredCafeteria() = runBlocking {
        val day = MenuRepository(SampleMenuDataSource.all()).load(date)
        fun order(first: Cafeteria) = MenuUiState(
            date = date,
            loadState = MenuLoadState.Loaded(day),
            selectedMeal = MealType.LUNCH,
            firstCafeteria = first,
        ).sections.map { it.cafeteria to it.area }

        assertEquals(
            listOf(
                Cafeteria.CAFETERIA_2 to MenuArea.DINE_IN, Cafeteria.CAFETERIA_2 to MenuArea.TAKE_OUT,
                Cafeteria.CAFETERIA_1 to MenuArea.DINE_IN, Cafeteria.CAFETERIA_1 to MenuArea.TAKE_OUT,
            ),
            order(Cafeteria.CAFETERIA_2)
        )
        assertEquals(Cafeteria.CAFETERIA_1, order(Cafeteria.CAFETERIA_1).first().first)
    }

    @Test
    fun parsesAlarmTimeInput() {
        assertEquals(AlarmTime(12, 30), AlarmTime.parse("1230"))
        assertEquals(AlarmTime(9, 5), AlarmTime.parse("905"))
        assertEquals(AlarmTime(11, 30), AlarmTime.parse("11:30"))
        assertEquals("09:05", AlarmTime(9, 5).toString())
        listOf("", "12", "12345", "2400", "1260", "abcd").forEach {
            assertEquals("'$it' should be rejected", null, AlarmTime.parse(it))
        }
    }

    @Test
    fun picksMealByTimeOfDay() {
        assertEquals(MealType.BREAKFAST, mealTypeFor(LocalTime.of(9, 59)))
        assertEquals(MealType.LUNCH, mealTypeFor(LocalTime.of(10, 0)))
        assertEquals(MealType.LUNCH, mealTypeFor(LocalTime.of(13, 59)))
        assertEquals(MealType.DINNER, mealTypeFor(LocalTime.of(14, 0)))
    }
}

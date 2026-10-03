package net.rivergod.sec.seoulrnd.android.menu

import kotlinx.coroutines.runBlocking
import net.rivergod.sec.seoulrnd.android.menu.data.MenuParseException
import net.rivergod.sec.seoulrnd.android.menu.data.SampleMenuDataSource
import net.rivergod.sec.seoulrnd.android.menu.data.WelstoryMenuParser
import net.rivergod.sec.seoulrnd.android.menu.dto.MealType
import net.rivergod.sec.seoulrnd.android.menu.dto.MenuArea
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WelstoryMenuParserTest {

    private val date = LocalDate.of(2024, 11, 1)

    private fun row(meal: String, course: String, hall: String, typical: Boolean, name: String, kcal: String = "", courseTxt: String = "") =
        """{"menu_meal_type":"$meal","menu_course_type":"$course","hall_no":"$hall","course_txt":"$courseTxt",""" +
            """"typical_menu":"${if (typical) "Y" else "N"}","menu_name":"$name","tot_kcal":"$kcal"}"""

    @Test
    fun groupsSideDishesUnderTypicalMenu() {
        val json = listOf(
            row("2", "AB", "E5J2", typical = false, name = "쌀밥"),
            row("2", "AB", "E5J2", typical = true, name = "돼지김치찌개", kcal = "760", courseTxt = "도담찌개"),
            row("2", "AB", "E5J2", typical = false, name = "깍두기"),
            row("3", "AA", "E5J3", typical = true, name = "제육볶음(840kcal)", kcal = "840", courseTxt = "봄이온소반"),
            row("1", "CB", "E5J4", typical = true, name = "샌드위치[2~3Coin]", kcal = "480", courseTxt = "T/O 샌드위치"),
            // 대표 메뉴가 없는 코스의 곁들임은 버린다
            row("2", "ZZ", "E5J2", typical = false, name = "고아 반찬"),
        ).joinToString(",", "[", "]")

        val day = WelstoryMenuParser.parse(json, date)

        assertEquals(3, day.cuisines.size)

        val lunch = day.forMeal(MealType.LUNCH).single()
        assertEquals("돼지김치찌개", lunch.displayTitle)
        assertEquals(listOf("쌀밥", "깍두기"), lunch.sideDishes)
        assertEquals("760 kcal", lunch.displayCalorie)
        assertEquals(MenuArea.DINE_IN, lunch.area)
        assertEquals(R.drawable.icon_dodam, lunch.iconRes)

        val dinner = day.forMeal(MealType.DINNER).single()
        assertEquals("제육볶음", dinner.displayTitle)
        assertEquals(MenuArea.DINE_IN, dinner.area)
        assertEquals(R.drawable.icon_soban, dinner.iconRes)

        val breakfast = day.forMeal(MealType.BREAKFAST).single()
        assertEquals(MenuArea.TAKE_OUT, breakfast.area)
        assertEquals(R.drawable.icon_takeout, breakfast.iconRes)
    }

    /** 2026-10-02 welmenu.welstory.com 실제 응답을 코스별 곁들임 3개까지로 줄인 것. */
    @Test
    fun parsesRealResponse() {
        val json = javaClass.classLoader!!.getResource("welmenu_20261002.json")!!.readText()
        val day = WelstoryMenuParser.parse(json, LocalDate.of(2026, 10, 2))

        MealType.entries.forEach { meal ->
            val cuisines = day.forMeal(meal)
            assertTrue("$meal 식당", cuisines.any { it.area == MenuArea.DINE_IN })
            assertTrue("$meal Take Out", cuisines.any { it.area == MenuArea.TAKE_OUT })
        }
        assertEquals(6, day.forMeal(MealType.LUNCH).count { it.area == MenuArea.DINE_IN })

        val soup = day.cuisines.first { it.id == "1AAE5J2" }
        assertEquals("소머리국밥", soup.displayTitle)
        assertEquals("봄이온소반", soup.courseName)
        assertEquals("쌀밥", soup.sideDishes.first())
        assertEquals("725 kcal", soup.displayCalorie)

        // tot_kcal 이 소수인 경우와 0 인 경우
        assertEquals("1346 kcal", day.cuisines.first { it.id == "2DAE5J4" }.displayCalorie)
        assertEquals("", day.cuisines.first { it.id == "2ZQE5J4" }.displayCalorie)
        // null 값이 "null" 문자열로 들어오지 않아야 한다
        assertTrue(day.cuisines.none { it.title == "null" || it.sideDishes.contains("null") })
    }

    @Test
    fun emptyArrayMeansNoMenu() {
        // 주말 등 메뉴가 없는 날 실제 서버 응답
        assertTrue(WelstoryMenuParser.parse("[]", date).cuisines.isEmpty())
    }

    @Test(expected = MenuParseException::class)
    fun rejectsNonArrayResponse() {
        WelstoryMenuParser.parse("""{"result":"error"}""", date)
    }

    @Test(expected = MenuParseException::class)
    fun rejectsHtmlResponse() {
        WelstoryMenuParser.parse("<html><body>점검중</body></html>", date)
    }

    @Test
    fun sampleDataHasEveryMealAndChangesByDate() = runBlocking {
        val source = SampleMenuDataSource()
        val day1 = WelstoryMenuParser.parse(source.fetch(date), date)
        val day2 = WelstoryMenuParser.parse(source.fetch(date.plusDays(1)), date.plusDays(1))

        MealType.entries.forEach { meal -> assertTrue("$meal is empty", day1.forMeal(meal).isNotEmpty()) }
        assertTrue(day1.cuisines.any { it.area == MenuArea.TAKE_OUT })
        assertEquals(day1.cuisines.size, day1.cuisines.map { it.id }.toSet().size)
        assertTrue(day1.cuisines.all { it.iconRes != 0 && it.sideDishes.isNotEmpty() })
        assertNotEquals(day1.cuisines.map { it.title }, day2.cuisines.map { it.title })
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
        assertEquals(MealType.BREAKFAST, mealTypeFor(java.time.LocalTime.of(9, 59)))
        assertEquals(MealType.LUNCH, mealTypeFor(java.time.LocalTime.of(10, 0)))
        assertEquals(MealType.LUNCH, mealTypeFor(java.time.LocalTime.of(13, 59)))
        assertEquals(MealType.DINNER, mealTypeFor(java.time.LocalTime.of(14, 0)))
    }
}

package net.rivergod.sec.seoulrnd.android.menu

import net.rivergod.sec.seoulrnd.android.menu.data.MenuParseException
import net.rivergod.sec.seoulrnd.android.menu.data.WelstoryMenuParser
import net.rivergod.sec.seoulrnd.android.menu.dto.Cafeteria
import net.rivergod.sec.seoulrnd.android.menu.dto.MealType
import net.rivergod.sec.seoulrnd.android.menu.dto.MenuArea
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WelstoryMenuParserTest {

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

        val cuisines = WelstoryMenuParser.parse(json)

        assertEquals(3, cuisines.size)
        assertTrue(cuisines.all { it.cafeteria == Cafeteria.CAFETERIA_2 })

        val lunch = cuisines.single { it.mealType == MealType.LUNCH }
        assertEquals("돼지김치찌개", lunch.displayTitle)
        assertEquals(listOf("쌀밥", "깍두기"), lunch.sideDishes)
        assertEquals("760 kcal", lunch.displayCalorie)
        assertEquals(MenuArea.DINE_IN, lunch.area)
        assertEquals(R.drawable.icon_dodam, lunch.iconRes)

        val dinner = cuisines.single { it.mealType == MealType.DINNER }
        assertEquals("제육볶음", dinner.displayTitle)
        assertEquals(R.drawable.icon_soban, dinner.iconRes)

        val breakfast = cuisines.single { it.mealType == MealType.BREAKFAST }
        assertEquals(MenuArea.TAKE_OUT, breakfast.area)
        assertEquals(R.drawable.icon_takeout, breakfast.iconRes)
    }

    /** 2026-10-02 welmenu.welstory.com 실제 응답을 코스별 곁들임 3개까지로 줄인 것. */
    @Test
    fun parsesRealResponse() {
        val cuisines = WelstoryMenuParser.parse(resource("welmenu_20261002.json"))

        MealType.entries.forEach { meal ->
            val forMeal = cuisines.filter { it.mealType == meal }
            assertTrue("$meal 식당", forMeal.any { it.area == MenuArea.DINE_IN })
            assertTrue("$meal Take Out", forMeal.any { it.area == MenuArea.TAKE_OUT })
        }
        assertEquals(6, cuisines.count { it.mealType == MealType.LUNCH && it.area == MenuArea.DINE_IN })

        val soup = cuisines.first { it.id == "W1AAE5J2" }
        assertEquals("소머리국밥", soup.displayTitle)
        assertEquals("봄이온소반", soup.courseName)
        assertEquals("쌀밥", soup.sideDishes.first())
        assertEquals("725 kcal", soup.displayCalorie)

        // tot_kcal 이 소수인 경우와 0 인 경우
        assertEquals("1346 kcal", cuisines.first { it.id == "W2DAE5J4" }.displayCalorie)
        assertEquals("", cuisines.first { it.id == "W2ZQE5J4" }.displayCalorie)
        // null 값이 "null" 문자열로 들어오지 않아야 한다
        assertTrue(cuisines.none { it.title == "null" || it.sideDishes.contains("null") })
    }

    @Test
    fun emptyArrayMeansNoMenu() {
        // 주말 등 메뉴가 없는 날 실제 서버 응답
        assertTrue(WelstoryMenuParser.parse("[]").isEmpty())
    }

    @Test(expected = MenuParseException::class)
    fun rejectsNonArrayResponse() {
        WelstoryMenuParser.parse("""{"result":"error"}""")
    }

    @Test(expected = MenuParseException::class)
    fun rejectsHtmlResponse() {
        WelstoryMenuParser.parse("<html><body>점검중</body></html>")
    }
}

internal fun Any.resource(name: String): String = javaClass.classLoader!!.getResource(name)!!.readText()

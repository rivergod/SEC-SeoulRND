package net.rivergod.sec.seoulrnd.android.menu

import net.rivergod.sec.seoulrnd.android.menu.data.MenuParseException
import net.rivergod.sec.seoulrnd.android.menu.data.PulmuoneMenuParser
import net.rivergod.sec.seoulrnd.android.menu.dto.Cafeteria
import net.rivergod.sec.seoulrnd.android.menu.dto.MealType
import net.rivergod.sec.seoulrnd.android.menu.dto.MenuArea
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PulmuoneMenuParserTest {

    /** 2026-10-02 puls2.pulmuone.com 실제 응답을 코스별 구성 메뉴 4개까지로 줄인 것. */
    @Test
    fun parsesRealResponse() {
        val cuisines = PulmuoneMenuParser.parse(resource("pulmuone_20261002.json"))

        assertTrue(cuisines.all { it.cafeteria == Cafeteria.CAFETERIA_1 && it.iconRes == 0 })
        assertEquals(cuisines.size, cuisines.map { it.id }.toSet().size)
        MealType.entries.forEach { meal ->
            assertTrue("$meal Take Out", cuisines.any { it.mealType == meal && it.area == MenuArea.TAKE_OUT })
        }

        val lunchDineIn = cuisines.filter { it.mealType == MealType.LUNCH && it.area == MenuArea.DINE_IN }
        assertEquals(listOf("ASIAN", "쉐피스트", "보글보글", "88℃온도", "POPUP", "풀스바", "찬장", "찬장_죽"), lunchDineIn.map { it.courseName })

        val asian = lunchDineIn.first()
        assertEquals("들깨칼국수", asian.title)
        assertEquals("1097 kcal", asian.displayCalorie)
        // 구성 메뉴 첫 행(주메뉴와 같은 이름)은 곁들임에서 빠진다
        assertEquals(listOf("미니열무비빔밥", "수제녹두빈대떡＆양파고추절임", "해초무침"), asian.sideDishes)

        // 주메뉴와 이름이 다른 구성 메뉴는 그대로 둔다
        val popup = lunchDineIn.first { it.courseName == "POPUP" }
        assertEquals("후덕죽싸이버거SET", popup.title)
        assertEquals("후덕죽싸이버거", popup.sideDishes.first())

        // 공백 없는 "T/O컵과일" 도 Take Out
        assertEquals(MenuArea.TAKE_OUT, cuisines.first { it.courseName == "T/O컵과일" }.area)
        assertEquals(MenuArea.DINE_IN, cuisines.first { it.mealType == MealType.BREAKFAST && it.courseName == "죽or수프＆찜채소" }.area)
    }

    @Test
    fun nullMealDataMeansNoMenu() {
        // 주말 등 메뉴가 없는 날 실제 서버 응답 (query 는 생략)
        assertTrue(PulmuoneMenuParser.parse("""{"mealData":{"mealData":null,"query":"SELECT ..."}}""").isEmpty())
    }

    @Test(expected = MenuParseException::class)
    fun rejectsUnexpectedShape() {
        PulmuoneMenuParser.parse("""[]""")
    }
}

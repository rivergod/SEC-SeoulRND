package net.rivergod.sec.seoulrnd.android.menu.data

import net.rivergod.sec.seoulrnd.android.menu.MenuItemIconResource
import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO
import net.rivergod.sec.seoulrnd.android.menu.dto.DayCuisionsDTO
import net.rivergod.sec.seoulrnd.android.menu.dto.MealType
import net.rivergod.sec.seoulrnd.android.menu.dto.MenuArea
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.time.LocalDate

class MenuParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * getSeoulRndMenuList.do 응답(JSON 배열) 파서. 0.9.14 의 MenuActivity.getMenu() 파싱 로직과 동일한 규칙을 따른다.
 *
 * 항목 예 (2026-10-02 실제 응답 일부):
 * ```
 * { "menu_meal_type": "1", "menu_course_type": "AA", "hall_no": "E5J2", "course_txt": "봄이온소반",
 *   "typical_menu": "Y", "menu_name": "소머리국밥", "kcal": 269, "tot_kcal": 725, "menu_dt": "20261002",
 *   "eng_menu_name": "Korean Beef Head Meat Soup", ... }
 * ```
 * - menu_meal_type + menu_course_type + hall_no 가 같은 항목들이 하나의 코스를 이룬다. (웹페이지도 같은 키를 씀)
 * - typical_menu == "Y" 인 항목이 대표 메뉴, 나머지는 곁들임 메뉴.
 * - menu_meal_type: "1" 조식, "2" 중식, "3" 석식. 요청의 meal_type 과 무관하게 하루치 세 끼가 모두 온다.
 * - 주말 등 메뉴가 없는 날은 빈 배열이 온다.
 */
object WelstoryMenuParser {

    /**
     * Take Out 으로 묶을 hall_no. E5J2, E5J3 은 식당 코너, E5J4 는 T/O 코너와 웰핏이다. (2026-10 기준)
     * hall_no 가 바뀌어도 코너 이름이 "T/O" 로 시작하면 Take Out 으로 본다.
     */
    val TAKE_OUT_HALL_NOS = setOf("E5J4")

    fun parse(json: String, date: LocalDate): DayCuisionsDTO {
        val root = try {
            JSONTokener(json).nextValue()
        } catch (e: JSONException) {
            throw MenuParseException("JSON 형식이 아닙니다.", e)
        }
        if (root !is JSONArray) {
            throw MenuParseException("JSON 배열 형식이 아닙니다.")
        }

        val rows = (0 until root.length()).mapNotNull { root.optJSONObject(it) }

        val mains = LinkedHashMap<String, CuisineDTO>()
        val sides = HashMap<String, MutableList<String>>()

        for (row in rows) {
            val mealCode = row.text("menu_meal_type")
            val hallNo = row.text("hall_no")
            val id = mealCode + row.text("menu_course_type") + hallNo
            val menuName = row.text("menu_name")

            if (row.text("typical_menu") == "Y") {
                val courseName = row.text("course_txt")
                mains[id] = CuisineDTO(
                    id = id,
                    mealType = when (mealCode) {
                        "3" -> MealType.DINNER
                        "2" -> MealType.LUNCH
                        else -> MealType.BREAKFAST
                    },
                    area = if (hallNo in TAKE_OUT_HALL_NOS || courseName.startsWith("T/O")) {
                        MenuArea.TAKE_OUT
                    } else {
                        MenuArea.DINE_IN
                    },
                    courseName = courseName,
                    iconRes = MenuItemIconResource.getMenuIcon(courseName),
                    title = menuName,
                    sideDishes = emptyList(),
                    calorie = row.text("tot_kcal"),
                )
            } else if (menuName.isNotEmpty()) {
                sides.getOrPut(id) { mutableListOf() }.add(menuName)
            }
        }

        // 대표 메뉴가 없는 코스의 곁들임 메뉴는 0.9.14 와 마찬가지로 버린다.
        val cuisines = mains.values.map { it.copy(sideDishes = sides[it.id].orEmpty()) }
        return DayCuisionsDTO(date, cuisines)
    }

    /** 문자열·숫자 값을 문자열로 읽는다. 응답의 null 은 optString 이 "null" 로 돌려주므로 빈 문자열로 바꾼다. */
    private fun JSONObject.text(name: String): String = if (isNull(name)) "" else optString(name).trim()
}

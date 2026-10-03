package net.rivergod.sec.seoulrnd.android.menu.data

import net.rivergod.sec.seoulrnd.android.menu.MenuItemIconResource
import net.rivergod.sec.seoulrnd.android.menu.dto.Campus
import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO
import net.rivergod.sec.seoulrnd.android.menu.dto.DayCuisionsDTO
import net.rivergod.sec.seoulrnd.android.menu.dto.MealType
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONTokener
import java.time.LocalDate

class MenuParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * getSeoulRndMenuList.do 응답(JSON 배열) 파서. 0.9.14 의 MenuActivity.getMenu() 파싱 로직과 동일한 규칙을 따른다.
 *
 * 항목 예:
 * ```
 * { "menu_meal_type": "2", "menu_course_type": "AB", "hall_no": "SRND2", "course_txt": "도담찌개",
 *   "typical_menu": "Y", "menu_name": "돼지김치찌개", "tot_kcal": "760" }
 * ```
 * - menu_meal_type + menu_course_type + hall_no 가 같은 항목들이 하나의 코스를 이룬다.
 * - typical_menu == "Y" 인 항목이 대표 메뉴, 나머지는 곁들임 메뉴.
 * - menu_meal_type: "1" 조식, "2" 중식, "3" 석식.
 */
object WelstoryMenuParser {

    /**
     * 1캠퍼스(A,B,C Tower) 식당으로 취급할 hall_no.
     * 실제 값은 확인할 수 없어 추정값이며, 0.9.14 처럼 그 외는 모두 2캠퍼스로 본다.
     */
    val CAMPUS_1_HALL_NOS = setOf("SRND1")

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
            val mealCode = row.optString("menu_meal_type")
            val id = mealCode + row.optString("menu_course_type") + row.optString("hall_no")
            val menuName = row.optString("menu_name").trim()

            if (row.optString("typical_menu") == "Y") {
                val courseName = row.optString("course_txt").trim()
                mains[id] = CuisineDTO(
                    id = id,
                    mealType = when (mealCode) {
                        "3" -> MealType.DINNER
                        "2" -> MealType.LUNCH
                        else -> MealType.BREAKFAST
                    },
                    campus = if (row.optString("hall_no") in CAMPUS_1_HALL_NOS) Campus.CAMPUS_1 else Campus.CAMPUS_2,
                    courseName = courseName,
                    iconRes = MenuItemIconResource.getMenuIcon(courseName),
                    title = menuName,
                    sideDishes = emptyList(),
                    calorie = row.optString("tot_kcal").trim(),
                )
            } else if (menuName.isNotEmpty()) {
                sides.getOrPut(id) { mutableListOf() }.add(menuName)
            }
        }

        // 대표 메뉴가 없는 코스의 곁들임 메뉴는 0.9.14 와 마찬가지로 버린다.
        val cuisines = mains.values.map { it.copy(sideDishes = sides[it.id].orEmpty()) }
        return DayCuisionsDTO(date, cuisines)
    }
}

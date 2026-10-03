package net.rivergod.sec.seoulrnd.android.menu.data

import net.rivergod.sec.seoulrnd.android.menu.dto.Cafeteria
import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO
import net.rivergod.sec.seoulrnd.android.menu.dto.MealType
import net.rivergod.sec.seoulrnd.android.menu.dto.MenuArea
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

/**
 * 1식당(풀무원) todayMealPlan_sql.php 응답 파서.
 *
 * 응답은 `{"mealData":{"mealData":[[...], ...]}}` 이고 각 행은 필드 이름 없는 17칸 배열이다.
 * 메뉴판 페이지(todayMealPlan.js)가 쓰는 칸만 읽는다.
 * ```
 * ["20261002",null,null,null,"020","002","중식","LUNCH","001","ASIAN","ASIAN","들깨칼국수","...","1097",null,"1097","Y"]
 *    [5] 끼니 코드 001 조식 / 002 중식 / 003 석식      [6] 끼니 이름
 *    [8] 코너 코드   [9] 코너 이름   [11] 메뉴 이름   [13] 메뉴 칼로리   [15] 코스 칼로리   [16] 주메뉴 여부
 * ```
 * - 끼니 코드 + 코너 코드가 같은 행들이 하나의 코스를 이루고, 주메뉴(Y) 행이 먼저 온 뒤 구성 메뉴(N) 행이 온다.
 * - 구성 메뉴의 첫 행은 보통 주메뉴와 같은 이름이라 곁들임에서 뺀다.
 * - 메뉴가 없는 날(주말 등)은 안쪽 mealData 가 null 이다.
 */
object PulmuoneMenuParser {

    private const val COL_TIME_CD = 5
    private const val COL_SHOP_CD = 8
    private const val COL_SHOP_NM = 9
    private const val COL_MENU_NM = 11
    private const val COL_TOT_CALORIE = 15
    private const val COL_MAIN_YN = 16

    fun parse(json: String): List<CuisineDTO> {
        val root = try {
            JSONTokener(json).nextValue()
        } catch (e: JSONException) {
            throw MenuParseException("JSON 형식이 아닙니다.", e)
        }
        val wrapper = (root as? JSONObject)?.optJSONObject("mealData")
            ?: throw MenuParseException("mealData 가 없습니다.")
        if (wrapper.isNull("mealData")) return emptyList()
        val rows = wrapper.optJSONArray("mealData") ?: throw MenuParseException("mealData 가 배열이 아닙니다.")

        val mains = LinkedHashMap<String, CuisineDTO>()
        val sides = HashMap<String, MutableList<String>>()

        for (i in 0 until rows.length()) {
            val row = rows.optJSONArray(i) ?: continue
            val timeCd = row.text(COL_TIME_CD)
            val id = timeCd + row.text(COL_SHOP_CD)
            val menuName = row.text(COL_MENU_NM)

            if (row.text(COL_MAIN_YN) == "Y") {
                val shopName = row.text(COL_SHOP_NM)
                mains[id] = CuisineDTO(
                    id = "P$id",
                    cafeteria = Cafeteria.CAFETERIA_1,
                    mealType = when (timeCd) {
                        "003" -> MealType.DINNER
                        "002" -> MealType.LUNCH
                        else -> MealType.BREAKFAST
                    },
                    area = if (shopName.startsWith("T/O")) MenuArea.TAKE_OUT else MenuArea.DINE_IN,
                    courseName = shopName,
                    // 아이콘 리소스는 웰스토리 코너 로고이므로 풀무원 코너에는 쓰지 않고 코너 이름을 보여준다
                    iconRes = 0,
                    title = menuName,
                    sideDishes = emptyList(),
                    calorie = row.text(COL_TOT_CALORIE),
                )
            } else if (menuName.isNotEmpty()) {
                sides.getOrPut(id) { mutableListOf() }.add(menuName)
            }
        }

        return mains.map { (id, main) ->
            val items = sides[id].orEmpty()
            main.copy(sideDishes = if (items.firstOrNull() == main.title) items.drop(1) else items)
        }
    }

    private fun JSONArray.text(index: Int): String = if (isNull(index)) "" else optString(index).trim()
}

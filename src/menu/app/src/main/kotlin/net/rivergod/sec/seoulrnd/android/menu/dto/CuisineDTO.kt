package net.rivergod.sec.seoulrnd.android.menu.dto

import androidx.annotation.DrawableRes

enum class MealType { BREAKFAST, LUNCH, DINNER }

/**
 * 식당. 0.9.14 의 1/2캠퍼스 구분에 해당하며 식당마다 운영사와 메뉴 API 가 다르다.
 * - 1식당: 풀무원 (puls2.pulmuone.com)
 * - 2식당: 삼성웰스토리 (welmenu.welstory.com)
 */
enum class Cafeteria(val label: String, val shortLabel: String, val vendor: String) {
    CAFETERIA_1("1식당 (Cafeteria 1)", "1식당", "풀무원"),
    CAFETERIA_2("2식당 (Cafeteria 2)", "2식당", "삼성웰스토리"),
}

/** 식당 안의 묶음. 두 식당 모두 식당 코너와 Take Out(T/O) 코너가 있다. */
enum class MenuArea { DINE_IN, TAKE_OUT }

/**
 * 한 코스(식당 코너)의 대표 메뉴.
 *
 * @param courseName 코너 이름. 아이콘이 없을 때 대신 표시한다.
 * @param iconRes 코너 아이콘 drawable, 매칭되는 아이콘이 없으면 0.
 * @param sideDishes 같은 코스에 속한 곁들임 메뉴들.
 * @param calorie 코스 전체 칼로리 ("725", "1346.44" 처럼 숫자 문자열).
 */
data class CuisineDTO(
    val id: String,
    val cafeteria: Cafeteria,
    val mealType: MealType,
    val area: MenuArea,
    val courseName: String,
    @param:DrawableRes val iconRes: Int,
    val title: String,
    val sideDishes: List<String>,
    val calorie: String,
) {
    /** 0.9.14 와 동일하게 "메뉴명(000kcal)" 형태의 제목에서 괄호 부분을 잘라낸다. */
    val displayTitle: String
        get() = if (title.contains("l)") && title.contains("(")) {
            title.substring(0, title.indexOf("(")).trim()
        } else {
            title
        }

    /** 소수는 반올림하고, 0 이면(T/O 상시메뉴 등) 표시하지 않는다. */
    val displayCalorie: String
        get() {
            val kcal = calorie.toDoubleOrNull() ?: return calorie
            return if (kcal > 0) "${Math.round(kcal)} kcal" else ""
        }
}

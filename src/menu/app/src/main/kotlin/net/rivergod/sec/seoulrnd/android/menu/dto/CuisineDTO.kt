package net.rivergod.sec.seoulrnd.android.menu.dto

import androidx.annotation.DrawableRes

enum class MealType { BREAKFAST, LUNCH, DINNER }

/**
 * 메뉴 묶음. 0.9.14 는 1/2캠퍼스로 나눴지만, 현재 웰스토리 메뉴(welmenu.welstory.com)는
 * Cafeteria 2 한 곳의 식당 코너와 Take Out 만 제공한다.
 */
enum class MenuArea(val label: String) {
    DINE_IN("식당 (Cafeteria 2)"),
    TAKE_OUT("Take Out"),
}

/**
 * 한 코스(식당 코너)의 대표 메뉴.
 *
 * @param courseName 코너 이름 (응답의 course_txt). 아이콘이 없을 때 대신 표시한다.
 * @param iconRes 코너 아이콘 drawable, 매칭되는 아이콘이 없으면 0.
 * @param sideDishes 같은 코스에 속한 곁들임 메뉴들 (typical_menu != "Y").
 */
data class CuisineDTO(
    val id: String,
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

    /** tot_kcal 은 "725", "1346.44" 처럼 오며, 0 이면(T/O 상시메뉴 등) 표시하지 않는다. */
    val displayCalorie: String
        get() {
            val kcal = calorie.toDoubleOrNull() ?: return calorie
            return if (kcal > 0) "${Math.round(kcal)} kcal" else ""
        }
}

package net.rivergod.sec.seoulrnd.android.menu.dto

import androidx.annotation.DrawableRes

enum class MealType { BREAKFAST, LUNCH, DINNER }

enum class Campus(val label: String) {
    CAMPUS_1("1캠퍼스 (A,B,C Tower)"),
    CAMPUS_2("2캠퍼스 (D,E,F Tower)"),
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
    val campus: Campus,
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

    val displayCalorie: String
        get() = if (calorie.isNotEmpty() && calorie.all { it.isDigit() }) "$calorie kcal" else calorie
}

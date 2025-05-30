package net.rivergod.sec.seoulrnd.android.menu.dto

data class CuisineDTO(
    var mealCode: Int?,
    var campusCode: Int?,
    var cafeteriaCode: Int?,
    var cafeteriaUrl: String?,
    var title: String?,
    var content: String?,
    var calorie: String?
) {
    companion object {
        const val MEALCODE_BREAKFAST = 0
        const val MEALCODE_LAUNCH = 1
        const val MEALCODE_DINNER = 2

        const val CAMPUSCODE_1 = 0
        const val CAMPUSCODE_2 = 1
    }
}

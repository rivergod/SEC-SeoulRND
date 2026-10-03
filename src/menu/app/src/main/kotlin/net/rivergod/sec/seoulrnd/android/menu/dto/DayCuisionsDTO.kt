package net.rivergod.sec.seoulrnd.android.menu.dto

import java.time.LocalDate

/** 하루치 식단. 순서는 응답(코스) 순서를 유지한다. */
data class DayCuisionsDTO(
    val date: LocalDate,
    val cuisines: List<CuisineDTO>,
) {
    fun forMeal(mealType: MealType): List<CuisineDTO> = cuisines.filter { it.mealType == mealType }
}

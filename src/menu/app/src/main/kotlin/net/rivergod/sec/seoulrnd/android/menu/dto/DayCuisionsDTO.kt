package net.rivergod.sec.seoulrnd.android.menu.dto

import java.time.LocalDate

/**
 * 하루치 식단. 순서는 응답(코스) 순서를 유지한다.
 *
 * @param failedCafeterias 메뉴를 불러오지 못한 식당. 다른 식당의 메뉴는 그대로 보여준다.
 */
data class DayCuisionsDTO(
    val date: LocalDate,
    val cuisines: List<CuisineDTO>,
    val failedCafeterias: Set<Cafeteria> = emptySet(),
) {
    fun forMeal(mealType: MealType): List<CuisineDTO> = cuisines.filter { it.mealType == mealType }
}

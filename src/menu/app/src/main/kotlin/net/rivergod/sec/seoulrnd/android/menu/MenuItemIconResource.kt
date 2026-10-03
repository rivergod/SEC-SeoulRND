package net.rivergod.sec.seoulrnd.android.menu

import androidx.annotation.DrawableRes

/**
 * 2식당(웰스토리) 코스 이름(course_txt) -> 코너 로고 아이콘. 1식당(풀무원) 코너에는 쓰지 않는다.
 *
 * 0.9.14 는 course_txt 로 아이콘을 찾은 뒤 바로 0 으로 덮어써서 아이콘이 표시되지 않았다.
 * 코너 이름이 조금씩 바뀌므로("헬스기빙365", "T/O 샐러드" 등) 식당 안내도(menu_position)의
 * 코너 이름을 기준으로 키워드 매칭한다.
 */
object MenuItemIconResource {

    // 순서가 중요하다: 더 구체적인 키워드를 먼저 둔다. (예: "헬스기빙 착즙주스" 는 테이크아웃)
    private val keywordIcons: List<Pair<List<String>, Int>> = listOf(
        listOf("take", "테이크", "t/o", "착즙", "피크닉") to R.drawable.icon_takeout,
        listOf("비빈", "고슬") to R.drawable.icon_bibin,
        listOf("도담") to R.drawable.icon_dodam,
        listOf("미각면", "우리") to R.drawable.icon_woori,
        listOf("아시안", "asian") to R.drawable.icon_asian,
        listOf("헬스", "health") to R.drawable.icon_health,
        listOf("소반", "봄이온") to R.drawable.icon_soban,
        listOf("가츠", "gats") to R.drawable.icon_gach,
        listOf("싱푸", "xingfu", "차이나") to R.drawable.icon_xingfu,
        listOf("그릴", "grill") to R.drawable.icon_grill,
        listOf("스냅", "snap") to R.drawable.icon_snapsnack,
        listOf("나폴리", "napoli") to R.drawable.icon_napoli,
        listOf("슬로우", "slow") to R.drawable.icon_slowpot,
        listOf("탕맛", "기픈") to R.drawable.icon_tangmat,
    )

    @DrawableRes
    fun getMenuIcon(courseName: String): Int {
        val key = courseName.lowercase().replace(" ", "")
        return keywordIcons.firstOrNull { (keywords, _) -> keywords.any { key.contains(it) } }?.second ?: 0
    }
}

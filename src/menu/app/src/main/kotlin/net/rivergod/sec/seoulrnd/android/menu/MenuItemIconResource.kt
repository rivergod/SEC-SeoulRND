package net.rivergod.sec.seoulrnd.android.menu

import android.util.Log
import java.util.HashMap

object MenuItemIconResource {

    private const val TAG = "MenuItemIconResource" // Using simple class name as tag

    private val iconMap: HashMap<String, Int> = HashMap()

    init {
        iconMap["menu_b_gosel"] = R.drawable.icon_bibin //고슬고슬비빈

        //도담찌개
        iconMap["menu_b_dodam"] = R.drawable.icon_dodam
        iconMap["cafeteria_1_menu_02"] = R.drawable.icon_dodam // 1캠퍼스

        iconMap["menu_b_woori"] = R.drawable.icon_woori //우리 미각면

        iconMap["menu_b_health_korean"] = R.drawable.icon_asian // 아시안픽스

        iconMap["menu_b_health"] = R.drawable.icon_health //헬스기빙
        iconMap["menu_b_health_special"] = R.drawable.icon_health //헬스기빙스페셜
        iconMap["menu_b_health_bibim"] = R.drawable.icon_health //헬스기빙비빔밥
        iconMap["menu_b_health_theme"] = R.drawable.icon_health //헬스기빙365 테마밥상

        iconMap["menu_b_spring"] = R.drawable.icon_soban //봄이온소반
        iconMap["cafeteria_1_menu_01"] = R.drawable.icon_soban // 봄이온소반

        // Take Out
        iconMap["menu_b_to_bibim"] = R.drawable.icon_takeout //비빔
        iconMap["menu_b_to_sandwich"] = R.drawable.icon_takeout //샌드위치
        iconMap["menu_b_to_bread"] = R.drawable.icon_takeout //즉석빵
        iconMap["menu_b_to_fruit"] = R.drawable.icon_takeout //과일
        iconMap["menu_b_to_salad"] = R.drawable.icon_takeout //샐러드
        iconMap["menu_b_to_picnic"] = R.drawable.icon_takeout
        iconMap["menu_b_to_juice"] = R.drawable.icon_takeout // 착즙 주스
        iconMap["menu_b_to_pizza"] = R.drawable.icon_takeout // 피자
        iconMap["menu_b_health_juice"] = R.drawable.icon_takeout // 헬스기빙코리안 착즙주스
        iconMap["cafeteria_1_menu_08"] = R.drawable.icon_takeout // 스냅스낵 착즙주스(T/O)
        iconMap["cafeteria_1_menu_09"] = R.drawable.icon_takeout // 스냅스낵 피크닉(T/O)
//        iconMap["cafeteria_1_menu_healthy"] = R.drawable.icon_takeout // Take me Out 헬씨팩

        iconMap["menu_b_gats"] = R.drawable.icon_gach //가츠앤
        iconMap["cafeteria_1_menu_04"] = R.drawable.icon_gach //가츠앤

        iconMap["menu_b_singfu"] = R.drawable.icon_xingfu // 싱푸차이나
        iconMap["cafeteria_1_menu_05"] = R.drawable.icon_xingfu // 싱푸차이나

        iconMap["menu_b_brown"] = R.drawable.icon_grill //브라운그릴

        iconMap["menu_b_snap"] = R.drawable.icon_snapsnack
        iconMap["menu_b_snap_kimbab"] = R.drawable.icon_snapsnack //스낵김밥
        iconMap["menu_b_snap_juice"] = R.drawable.icon_snapsnack //스낵 착즙 주스

//        cafeteria_1_menu_03 // 테이스티가든

        //
        iconMap["봄이온소반"] = R.drawable.icon_soban
    }

    fun getMenuIcon(url: String): Int {
//        url = url.substring(url.lastIndexOf("/") + 1, url.length() - 4); // This line was commented out in Java

        val returnValue = iconMap[url] ?: 0 // Use Elvis operator for default value if key not found

        Log.d(TAG, "getMenu Icon url = $url returnValue = $returnValue")

        return returnValue
    }
}

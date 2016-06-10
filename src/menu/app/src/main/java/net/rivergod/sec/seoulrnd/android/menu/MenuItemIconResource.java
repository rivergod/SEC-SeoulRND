package net.rivergod.sec.seoulrnd.android.menu;

import java.util.HashMap;

public class MenuItemIconResource {

    private static final HashMap<String, Integer> iconMap;

    static {
        iconMap = new HashMap<>();

        iconMap.put("menu_b_gosel", R.drawable.icon_bibin); //고슬고슬비빈

        //도담찌개
        iconMap.put("menu_b_dodam", R.drawable.icon_dodam);
        iconMap.put("cafeteria_1_menu_02", R.drawable.icon_dodam); // 1캠퍼스

        iconMap.put("menu_b_woori", R.drawable.icon_woori); //우리 미각면

        iconMap.put("menu_b_health_korean", R.drawable.icon_asian); // 아시안픽스

        iconMap.put("menu_b_health", R.drawable.icon_health); //헬스기빙
        iconMap.put("menu_b_health_special", R.drawable.icon_health); //헬스기빙스페셜
        iconMap.put("menu_b_health_bibim", R.drawable.icon_health); //헬스기빙비빔밥
        iconMap.put("menu_b_health_theme", R.drawable.icon_health); //헬스기빙365 테마밥상

        iconMap.put("menu_b_spring", R.drawable.icon_soban); //봄이온소반
        iconMap.put("cafeteria_1_menu_01", R.drawable.icon_soban); // 봄이온소반

        // Take Out
        iconMap.put("menu_b_to_bibim", R.drawable.icon_takeout); //비빔
        iconMap.put("menu_b_to_sandwich", R.drawable.icon_takeout); //샌드위치
        iconMap.put("menu_b_to_bread", R.drawable.icon_takeout); //즉석빵
        iconMap.put("menu_b_to_fruit", R.drawable.icon_takeout); //과일
        iconMap.put("menu_b_to_salad", R.drawable.icon_takeout); //샐러드
        iconMap.put("menu_b_to_picnic", R.drawable.icon_takeout);
        iconMap.put("menu_b_to_juice", R.drawable.icon_takeout); // 착즙 주스
        iconMap.put("menu_b_to_pizza", R.drawable.icon_takeout); // 피자
        iconMap.put("menu_b_health_juice", R.drawable.icon_takeout); // 헬스기빙코리안 착즙주스
        iconMap.put("cafeteria_1_menu_08", R.drawable.icon_takeout); // 스냅스낵 착즙주스(T/O)
        iconMap.put("cafeteria_1_menu_09", R.drawable.icon_takeout); // 스냅스낵 피크닉(T/O)
//        iconMap.put("cafeteria_1_menu_healthy", R.drawable.icon_takeout); // Take me Out 헬씨팩

        iconMap.put("menu_b_gats", R.drawable.icon_gach); //가츠앤
        iconMap.put("cafeteria_1_menu_04", R.drawable.icon_gach); //가츠앤

        iconMap.put("menu_b_singfu", R.drawable.icon_xingfu); // 싱푸차이나
        iconMap.put("cafeteria_1_menu_05", R.drawable.icon_xingfu); // 싱푸차이나

        iconMap.put("menu_b_brown", R.drawable.icon_grill); //브라운그릴

        iconMap.put("menu_b_snap", R.drawable.icon_snapsnack);
        iconMap.put("menu_b_snap_kimbab", R.drawable.icon_snapsnack); //스낵김밥
        iconMap.put("menu_b_snap_juice", R.drawable.icon_snapsnack); //스낵 착즙 주스

//        cafeteria_1_menu_03 // 테이스티가든
    }

    public static int getMenuIcon(String url) {

        url = url.substring(url.lastIndexOf("/") + 1, url.length() - 4);

        int returnValue = 0;
        if(iconMap.containsKey(url)) {
           returnValue = iconMap.get(url);
        }

        return returnValue;
    }
}

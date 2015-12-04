package net.rivergod.sec.seoulrnd.android.menu;

public class MenuItemIconResouce {

    private static final String TO_BIBIM = "to_bibim"; //비빔
    private static final String DODAM = "dodam";   //도담찌개
    private static final String WOORI = "woori";   //우리 미각면

    private static final String HEALTH = "health"; //헬스기빙
    private static final String HEALTH_KOREA = "health_korean";//헬스기빙코리아
    private static final String HEALTH_SPECIAL = "health_special";//헬스기빙스페셜

    private static final String SPRING = "spring"; //봄이온소반

    private static final String TO_SANDWICH = "to_sandwich"; //샌드위치
    private static final String TO_BEAD = "to_bread";  //즉석빵
    private static final String TO_FRUIT = "to_fruit"; //과일
    private static final String TO_SALAD = "to_salad";  //샐러드
    private static final String TO_PICNIC = "to_picnic";

    private static final String GATS = "gats"; //가츠앤
    private static final String SINGFU = "singfu"; //씽푸
    private static final String BROWN = "brown";   //브라운그릴

    private static final String SNAP = "snap";
    private static final String SNAP_KIMBAB = "snap_kimbab";   //스낵김밥
    private static final String SNAP_JUICE = "snap_juice"; //스낵 착즙 주스

    public static int getMenuIcon(String url) {

        int returnValue = 0;

        url = url.substring(url.lastIndexOf("/") + 8, url.length() - 4);

        switch (url) {
            case TO_BIBIM:
                returnValue = R.drawable.icon_bibin;
                break;
            case DODAM:
                returnValue = R.drawable.icon_dodam;
                break;
            case WOORI:
                returnValue = R.drawable.icon_woori;
                break;

            case HEALTH:
            case HEALTH_KOREA:
            case HEALTH_SPECIAL:
                returnValue = R.drawable.icon_health;
                break;

            case SPRING:
                returnValue = R.drawable.icon_soban;
                break;

            case TO_SANDWICH:
            case TO_BEAD:
            case TO_FRUIT:
            case TO_SALAD:
            case TO_PICNIC:
                returnValue = R.drawable.icon_takeout;
                break;

            case GATS:
                returnValue = R.drawable.icon_gach;
                break;
            case SINGFU:
                returnValue = R.drawable.icon_xingfu;
                break;
            case BROWN:
                returnValue = R.drawable.icon_grill;
                break;

            case SNAP:
            case SNAP_JUICE:
            case SNAP_KIMBAB:
                returnValue = R.drawable.icon_snapsnack;
                break;
        }
        return returnValue;
    }
}

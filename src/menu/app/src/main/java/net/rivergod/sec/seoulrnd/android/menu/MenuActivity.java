package net.rivergod.sec.seoulrnd.android.menu;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO;
import net.rivergod.sec.seoulrnd.android.menu.dto.DayCuisionsDTO;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MenuActivity extends Activity {
    private static final String TAG = "MenuActivity";
    private final int TIME_TYPE_BREAKFAST = 0;
    private final int TIME_TYPE_LUNCH = 1;
    private final int TIME_TYPE_DINNER = 2;
    ArrayList<CuisineDTO> menuBreakfast = new ArrayList<>();
    ArrayList<CuisineDTO> menuLunch = new ArrayList<>();
    ArrayList<CuisineDTO> menuDinner = new ArrayList<>();
    private Tracker mTracker;
    private GridView gridMenuItems;
    private MenuAdapter adapter;
    private RelativeLayout tab0;
    private RelativeLayout tab1;
    private RelativeLayout tab2;
    private ImageView tab3;

    private View tab0Select;
    private View tab1Select;
    private View tab2Select;

    private MenuBarControl rightMenu;
    private View prevOption;
    private View.OnClickListener AlarOptionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (prevOption != null) {
                prevOption.setBackground(null);
            }

            v.setBackgroundColor(Color.YELLOW);

            prevOption = v;

//            switch (v.getId()){
//                case R.id.menu_tab_dinner_select:
//
//                    break;
//            }
        }
    };
    private View.OnClickListener TabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.menu_tab_breakfast:
                    setAdapterItems(TIME_TYPE_BREAKFAST);
                    break;
                case R.id.menu_tab_lunch:
                    setAdapterItems(TIME_TYPE_LUNCH);
                    break;
                case R.id.menu_tab_dinner:
                    setAdapterItems(TIME_TYPE_DINNER);
                    break;
                case R.id.menu_tab_setting:
                    if (rightMenu.isOpen()) {
                        rightMenu.menuClose();
                    } else {
                        rightMenu.menuOpen();
                    }

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        rightMenu = new MenuBarControl();
        rightMenu.init(this);

        gridMenuItems = (GridView) findViewById(R.id.grid_menu_items);

        adapter = new MenuAdapter();
        gridMenuItems.setAdapter(adapter);

        tab0 = (RelativeLayout) findViewById(R.id.menu_tab_breakfast);
        tab1 = (RelativeLayout) findViewById(R.id.menu_tab_lunch);
        tab2 = (RelativeLayout) findViewById(R.id.menu_tab_dinner);
        tab3 = (ImageView) findViewById(R.id.menu_tab_setting);

        tab0.setOnClickListener(TabClickListener);
        tab1.setOnClickListener(TabClickListener);
        tab2.setOnClickListener(TabClickListener);
        tab3.setOnClickListener(TabClickListener);

        tab0Select = findViewById(R.id.menu_tab_breakfast_select);
        tab1Select = findViewById(R.id.menu_tab_lunch_select);
        tab2Select = findViewById(R.id.menu_tab_dinner_select);

        findViewById(R.id.menu_alarm_select_0).setOnClickListener(AlarOptionClickListener);
        findViewById(R.id.menu_alarm_select_1).setOnClickListener(AlarOptionClickListener);
        findViewById(R.id.menu_alarm_select_2).setOnClickListener(AlarOptionClickListener);
        findViewById(R.id.menu_alarm_select_3).setOnClickListener(AlarOptionClickListener);


        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int week = cal.get(Calendar.DAY_OF_WEEK);
        String Week = "";
        if (week == 1) Week = "일";
        else if (week == 2) Week = "월";
        else if (week == 3) Week = "화";
        else if (week == 4) Week = "수";
        else if (week == 5) Week = "목";
        else if (week == 6) Week = "금";
        else if (week == 7) Week = "토";

        ((TextView) findViewById(R.id.menu_top_title)).setText(month + "월 " + day + "일 (" + Week + ")");


        MenuApplication application = (MenuApplication) getApplication();
        mTracker = application.getDefaultTracker();

        Communicator.init(getApplicationContext());
        Communicator.getEventBus().register(this);
        onChangeDate(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Setting screen name: MenuActivity");
        mTracker.setScreenName("MainActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void onChangeDate(final Date targetDate) {
        Communicator.getMenu(null);
    }

    public void onEvent(DayCuisionsDTO e) {

        if (menuBreakfast == null) {
            menuBreakfast = new ArrayList<>();
        } else {
            menuBreakfast.clear();
        }

        if (menuLunch == null) {
            menuLunch = new ArrayList<>();
        } else {
            menuLunch.clear();
        }

        if (menuDinner == null) {
            menuDinner = new ArrayList<>();
        } else {
            menuDinner.clear();
        }

        if (adapter != null) {

            for (CuisineDTO item : e.getCuisines()) {
                switch (item.getMealCode()) {
                    case TIME_TYPE_BREAKFAST:
                        menuBreakfast.add(item);
                        break;
                    case TIME_TYPE_LUNCH:
                        menuLunch.add(item);
                        break;
                    case TIME_TYPE_DINNER:
                        menuDinner.add(item);
                        break;
                }
            }
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

            if (hour < 10) {
                hour = TIME_TYPE_BREAKFAST;
            } else if (hour < 14) {
                hour = TIME_TYPE_LUNCH;
            } else {
                hour = TIME_TYPE_DINNER;
            }
            setAdapterItems(hour);
        }

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("MenuActivity")
                .setAction("onEvent(e)")
                .build());

    }

    public void onEvent(VolleyError error) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("MenuActivity")
                .setAction("onEvent(error)")
                .build());

    }


    private void setAdapterItems(int timeType) {


        tab0.setBackground(null);
        tab1.setBackground(null);
        tab2.setBackground(null);

        tab0Select.setVisibility(View.GONE);
        tab1Select.setVisibility(View.GONE);
        tab2Select.setVisibility(View.GONE);


        ArrayList<CuisineDTO> setItems = new ArrayList<>();

        if (timeType == TIME_TYPE_BREAKFAST) {
            setItems = menuBreakfast;

            tab0Select.setVisibility(View.VISIBLE);
            tab0.setBackgroundColor(Color.argb(144, 144, 144, 144));
        } else if (timeType == TIME_TYPE_LUNCH) {
            setItems = menuLunch;
            tab1Select.setVisibility(View.VISIBLE);
            tab1.setBackgroundColor(Color.argb(144, 144, 144, 144));
        } else if (timeType == TIME_TYPE_DINNER) {
            setItems = menuDinner;
            tab2Select.setVisibility(View.VISIBLE);
            tab2.setBackgroundColor(Color.argb(144, 144, 144, 144));
        }

        adapter.setItems(setItems);
    }

    class MenuAdapter extends BaseAdapter {

        private final String TO_BIBIM = "to_bibim"; //비빔
        private final String DODAM = "dodam";   //도담찌개
        private final String WOORI = "woori";   //우리 미각면

        private final String HEALTH = "health"; //헬스기빙
        private final String HEALTH_KOREA = "health_korean";//헬스기빙코리아
        private final String HEALTH_SPECIAL = "health_special";//헬스기빙스페셜

        private final String SPRING = "spring"; //봄이온소반

        private final String TO_SANDWICH = "to_sandwich"; //샌드위치
        private final String TO_BEAD = "to_bread";  //즉석빵
        private final String TO_FRUIT = "to_fruit"; //과일
        private final String TO_SALAD = "to_salad";  //샐러드

        private final String GATS = "gats"; //가츠앤
        private final String SINGFU = "singfu"; //씽푸
        private final String BROWN = "brown";   //브라운그릴

        private final String SNAP = "snap";
        private final String SNAP_KIMBAB = "snap_kimbab";   //스낵김밥
        private final String SNAP_JUICE = "snap_juice"; //스낵 착즙 주스

        private List<CuisineDTO> items;

        public void setItems(List<CuisineDTO> items) {
            this.items = items;

            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (items == null) {
                return 0;
            }
            return items.size();
        }

        @Override
        public Object getItem(int position) {

            if (items == null) {
                return null;
            }

            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            if (items == null) {
                return 0;
            }

            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (items == null) {
                return null;
            }

            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();

                convertView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.menu_item, parent, false);
                holder.ivPlaceIcon = (ImageView) convertView.findViewById(R.id.menu_icon_place);
                holder.tvName = (TextView) convertView.findViewById(R.id.menu_item_name);
                holder.tvSide = (TextView) convertView.findViewById(R.id.menu_item_side);
                holder.tvCalorie = (TextView) convertView.findViewById(R.id.menu_item_calorie);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            CuisineDTO item = items.get(position);

            holder.ivPlaceIcon.setBackgroundResource(getPlaceIcon(item.getCafeteriaUrl()));

            String title = item.getTitle();
            if (title.contains("l)")) {
                title = title.substring(0, title.indexOf("("));
            }
            holder.tvName.setText(title);

            if (title.length() > 12) {
                holder.tvName.setTextSize(9);
            } else {
                holder.tvName.setTextSize(13);
            }

            String content = item.getContent();
            int lastIndexComma = content.lastIndexOf(",");
            if (lastIndexComma != -1) {
                holder.tvSide.setText(content.substring(0, lastIndexComma));
            } else {
                holder.tvSide.setText("");
            }

            holder.tvCalorie.setText(content.substring(lastIndexComma + 1, content.length()));

            return convertView;
        }

        private int getPlaceIcon(String url) {

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

        class ViewHolder {
            ImageView ivPlaceIcon;
            TextView tvName;
            TextView tvSide;
            TextView tvCalorie;
        }
    }
}

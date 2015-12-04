package net.rivergod.sec.seoulrnd.android.menu;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO;
import net.rivergod.sec.seoulrnd.android.menu.dto.DayCuisionsDTO;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MenuActivity extends Activity implements CustomPopup.ButtonClickEvent {

    private static final String TAG = "MenuActivity";

    private final int TIME_TYPE_BREAKFAST = 0;
    private final int TIME_TYPE_LUNCH = 1;
    private final int TIME_TYPE_DINNER = 2;

    private ArrayList<CuisineDTO> menuBreakfast = new ArrayList<>();
    private ArrayList<CuisineDTO> menuLunch = new ArrayList<>();
    private ArrayList<CuisineDTO> menuDinner = new ArrayList<>();

    private Tracker mTracker;

    private MenuItemAdapter adapter;

    private RelativeLayout tabBreakfast;
    private RelativeLayout tabLunch;
    private RelativeLayout tabDinner;
    private ImageView tabOption;

    private TextView tabBreakfastText;
    private TextView tabLunchText;
    private TextView tabDinnerText;

    private MenuOptionControl optionMenu;

    private TextView tvAlarmUserSet;

    private ImageView ivAlarmCheck0;
    private ImageView ivAlarmCheck1;
    private ImageView ivAlarmCheck2;
    private ImageView ivAlarmCheck3;
    private ImageView ivAlarmCheck4;

    private ImageView prevAlarm;

    private int selectAlarmIndex = -1;

    private ProgressDialog progress;

    private Timer serverResponseCheck;

    private CustomPopup popUp;

    @Override
    public void onSetTime(String time) {
        if(popUp != null){
            popUp.dismiss();
        }

        if(time.length() == 3){
            time = time.charAt(0) + " : "+ time.substring(1, 3);
        }else if(time.length() == 4){
            time = time.substring(0,2) + " : "+time.substring(2,4);
        }else{
            time = "";
        }

        tvAlarmUserSet.setText(time);

    }

    @Override
    public void onClose() {
        if(popUp != null){
            popUp.dismiss();
        }
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

    private View.OnClickListener ShowLicense = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(popUp != null){
                popUp.setTitle("Title", false);
                popUp.setMSG("Message");

                popUp.show();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        popUp = new CustomPopup(this, this);

        optionMenu = new MenuOptionControl();
        optionMenu.init(this);

        GridView gridMenuItems = (GridView) findViewById(R.id.grid_menu_items);

        adapter = new MenuItemAdapter(this);
        gridMenuItems.setAdapter(adapter);

        tabBreakfast = (RelativeLayout) findViewById(R.id.menu_tab_breakfast);
        tabLunch = (RelativeLayout) findViewById(R.id.menu_tab_lunch);
        tabDinner = (RelativeLayout) findViewById(R.id.menu_tab_dinner);
        tabOption = (ImageView) findViewById(R.id.menu_tab_setting);

        tabBreakfast.setOnClickListener(TabClickListener);
        tabLunch.setOnClickListener(TabClickListener);
        tabDinner.setOnClickListener(TabClickListener);
        tabOption.setOnClickListener(TabClickListener);

        tabBreakfastText = (TextView) findViewById(R.id.menu_tab_breakfast_select);
        tabLunchText = (TextView) findViewById(R.id.menu_tab_lunch_select);
        tabDinnerText = (TextView) findViewById(R.id.menu_tab_dinner_select);

        findViewById(R.id.menu_alarm_select_0_layout).setOnClickListener(AlarOptionClickListener);
        findViewById(R.id.menu_alarm_select_1_layout).setOnClickListener(AlarOptionClickListener);
        findViewById(R.id.menu_alarm_select_2_layout).setOnClickListener(AlarOptionClickListener);
        findViewById(R.id.menu_alarm_select_3_layout).setOnClickListener(AlarOptionClickListener);
        findViewById(R.id.menu_alarm_select_4_layout).setOnClickListener(AlarOptionClickListener);

        tvAlarmUserSet = (TextView) findViewById(R.id.option_alarm_user_set_time);

        ivAlarmCheck0 = (ImageView) findViewById(R.id.menu_alarm_select_0_check);
        ivAlarmCheck1 = (ImageView) findViewById(R.id.menu_alarm_select_1_check);
        ivAlarmCheck2 = (ImageView) findViewById(R.id.menu_alarm_select_2_check);
        ivAlarmCheck3 = (ImageView) findViewById(R.id.menu_alarm_select_3_check);
        ivAlarmCheck4 = (ImageView) findViewById(R.id.menu_alarm_select_4_check);

        findViewById(R.id.option_license).setOnClickListener(ShowLicense);

        setDate();

        MenuApplication application = (MenuApplication) getApplication();
        mTracker = application.getDefaultTracker();

        Communicator.init(getApplicationContext());
        Communicator.getEventBus().register(this);
        onChangeDate(null);

        progress = ProgressDialog.show(MenuActivity.this, "", "Menu data loading....", true);

        registerServerResponse();
    }

    private void registerServerResponse(){

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                progress.dismiss();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Network 연결을 확인 하세요.\n Server 응답이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        };

        serverResponseCheck = new Timer();
        serverResponseCheck.schedule(task, 5000);
    }

    private void setDate(){
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
    }

    private void setAdapterItems(int timeType) {
        tabBreakfast.setBackground(null);
        tabLunch.setBackground(null);
        tabDinner.setBackground(null);

        tabBreakfastText.setBackground(null);
        tabLunchText.setBackground(null);
        tabDinnerText.setBackground(null);

        tabBreakfastText.setTextColor(Color.rgb(2, 4, 101));
        tabLunchText.setTextColor(Color.rgb(2, 4, 101));
        tabDinnerText.setTextColor(Color.rgb(2, 4, 101));

        ArrayList<CuisineDTO> setAapterItems = new ArrayList<>();

        if (timeType == TIME_TYPE_BREAKFAST) {
            setAapterItems = menuBreakfast;
            tabBreakfastText.setTextColor(Color.WHITE);
            tabBreakfast.setBackgroundColor(Color.rgb(166, 229, 255));
        } else if (timeType == TIME_TYPE_LUNCH) {
            setAapterItems = menuLunch;
            tabLunchText.setTextColor(Color.WHITE);
            tabLunch.setBackgroundColor(Color.rgb(166, 229, 255));
        } else if (timeType == TIME_TYPE_DINNER) {
            setAapterItems = menuDinner;
            tabDinnerText.setTextColor(Color.WHITE);
            tabDinner.setBackgroundColor(Color.rgb(166, 229, 255));
        }
        adapter.setItems(setAapterItems);
    }

    private View.OnClickListener AlarOptionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setAlarmSelect(Integer.parseInt((String) v.getTag()));
        }
    };

    private void setAlarmSelect(int index){

        if(prevAlarm != null){
            prevAlarm.setVisibility(View.GONE);
            prevAlarm = null;
        }

        if(selectAlarmIndex != index) {
            if (index == 0) {
                prevAlarm = ivAlarmCheck0;
            } else if (index == 1) {
                prevAlarm = ivAlarmCheck1;
            } else if (index == 2) {
                prevAlarm = ivAlarmCheck2;
            } else if (index == 3) {
                prevAlarm = ivAlarmCheck3;
            } else if (index == 4) {
                prevAlarm = ivAlarmCheck4;
                popUp.setTitle("Alarm Time Set", true);
                popUp.show();

            }
            if(prevAlarm != null) {
                prevAlarm.setVisibility(View.VISIBLE);
            }

            selectAlarmIndex = index;
        }else{
            selectAlarmIndex = -1;
        }
    }

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
                    if (optionMenu.isOpen()) {
                        optionMenu.menuClose();
                    } else {
                        optionMenu.menuOpen();
                    }
                    break;
            }
        }
    };

    public void onEvent(DayCuisionsDTO e) {

        if(progress != null) {
            progress.dismiss();
            serverResponseCheck.cancel();
        }

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
}

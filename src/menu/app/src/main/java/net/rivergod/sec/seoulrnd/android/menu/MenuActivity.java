package net.rivergod.sec.seoulrnd.android.menu;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tonicartos.superslim.LayoutManager;

import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO;
import net.rivergod.sec.seoulrnd.android.menu.dto.DayCuisionsDTO;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MenuActivity extends Activity {

    private static final String TAG = "MenuActivity";

    protected  static final String CAMPUS_TAG = "Campus";

    protected  static final String ALARM_TAG = "Alarm";

    private final int TIME_TYPE_BREAKFAST = 0;
    private final int TIME_TYPE_LUNCH = 1;
    private final int TIME_TYPE_DINNER = 2;

    private ArrayList<CuisineDTO> menuBreakfast = new ArrayList<>();
    private ArrayList<CuisineDTO> menuLunch = new ArrayList<>();
    private ArrayList<CuisineDTO> menuDinner = new ArrayList<>();

    private Tracker mTracker;

    private MenuItemAdapter adapter;

    private int selectedTime = TIME_TYPE_LUNCH;
    private RelativeLayout tabBreakfast;
    private RelativeLayout tabLunch;
    private RelativeLayout tabDinner;
    private ImageView tabOption;

    private TextView tabBreakfastText;
    private TextView tabLunchText;
    private TextView tabDinnerText;

    private MenuOptionControl menuOption;

    private ProgressDialog progress;

    private Timer serverResponseCheck;

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Setting screen name: MenuActivity");
        mTracker.setScreenName("MenuActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void onChangeDate(final Date targetDate) {
        Communicator.getMenu(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        menuOption = new MenuOptionControl();
        menuOption.init(MenuActivity.this);

        menuOption.setShowLicense(this, new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                LicenseDialog licenseDialog = new LicenseDialog(MenuActivity.this);

                licenseDialog.show();
            }
        });

        RecyclerView gridMenuItems = (RecyclerView) findViewById(R.id.grid_menu_items);
        gridMenuItems.setLayoutManager(new LayoutManager(this));

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

        selectedTime = timeType;

        tabBreakfast.setBackground(null);
        tabLunch.setBackground(null);
        tabDinner.setBackground(null);

        tabBreakfastText.setBackground(null);
        tabLunchText.setBackground(null);
        tabDinnerText.setBackground(null);

        tabBreakfastText.setTextColor(Color.rgb(2, 4, 101));
        tabLunchText.setTextColor(Color.rgb(2, 4, 101));
        tabDinnerText.setTextColor(Color.rgb(2, 4, 101));

        ArrayList<CuisineDTO> setAdapterItems = new ArrayList<>();

        if (timeType == TIME_TYPE_BREAKFAST) {
            setAdapterItems = menuBreakfast;
            tabBreakfastText.setTextColor(Color.WHITE);
            tabBreakfast.setBackgroundColor(Color.rgb(166, 229, 255));
        } else if (timeType == TIME_TYPE_LUNCH) {
            setAdapterItems = menuLunch;
            tabLunchText.setTextColor(Color.WHITE);
            tabLunch.setBackgroundColor(Color.rgb(166, 229, 255));
        } else if (timeType == TIME_TYPE_DINNER) {
            setAdapterItems = menuDinner;
            tabDinnerText.setTextColor(Color.WHITE);
            tabDinner.setBackgroundColor(Color.rgb(166, 229, 255));
        }
        adapter.setItems(setAdapterItems);
    }

    public void campusChanged() {
        setAdapterItems(selectedTime);
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
                    if (menuOption.isOpen()) {
                        menuOption.menuClose();
                    } else {
                        menuOption.menuOpen();
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

        if (progress != null) {
            progress.dismiss();
        }

        Toast.makeText(getApplicationContext(), "Network 연결을 확인 하세요.\n Server 응답이 없습니다.", Toast.LENGTH_SHORT).show();
    }
}

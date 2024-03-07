package net.rivergod.sec.seoulrnd.android.menu;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tonicartos.superslim.LayoutManager;

import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO;
import net.rivergod.sec.seoulrnd.android.menu.dto.DayCuisionsDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MenuActivity extends Activity {

    protected static final String CAMPUS_TAG = "Campus";
    protected static final String ALARM_TAG = "Alarm";
    private static final String TAG = "MenuActivity";
    private final int TIME_TYPE_BREAKFAST = 0;
    private final int TIME_TYPE_LUNCH = 1;
    private final int TIME_TYPE_DINNER = 2;

    private ArrayList<CuisineDTO> menuBreakfast = new ArrayList<>();
    private ArrayList<CuisineDTO> menuLunch = new ArrayList<>();
    private ArrayList<CuisineDTO> menuDinner = new ArrayList<>();

    private MenuItemAdapter adapter;

    private int selectedTime = TIME_TYPE_LUNCH;
    private final View.OnClickListener TabClickListener = new View.OnClickListener() {
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
    }

    public void onChangeDate(final Date targetDate) {
        getMenu(null);
    }

    private void getMenu(Object o) {
        OkHttpClient client = new OkHttpClient();

//        Request request = new Request
//                .Builder()
//                .url("https://www.samsungwelstory.com/menu/seoulrnd/menu.jsp")
//                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
//                .addHeader("Accept-Encoding", "gzip, deflate, br, zstd")
//                .addHeader("Accept-Language", "ko,en-US;q=0.9,en;q=0.8,ja;q=0.7,zh;q=0.6")
//                .addHeader("Cache-Control", "no-cache")
//                .addHeader("Connection", "keep-alive")
//                .addHeader("DNT", "1")
//                .addHeader("Pragma", "no-cache")
//                .addHeader("Sec-Fetch-Dest", "document")
//                .addHeader("Sec-Fetch-Mode", "navigate")
//                .addHeader("Sec-Fetch-Site", "none")
//                .addHeader("Sec-Fetch-User", "?1")
//                .addHeader("Upgrade-Insecure-Requests", "1")
//                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
//                .addHeader("sec-ch-ua", "\"Chromium\";v=\"122\", \"Not(A:Brand\";v=\"24\", \"Google Chrome\";v=\"122\"")
//                .addHeader("sec-ch-ua-mobile", "?0")
//                .addHeader("sec-ch-ua-platform", "\"Windows\"")
//                .build();

        Request request = new Request
                .Builder()
                .url("https://www.samsungwelstory.com/menu/getSeoulRndMenuList.do?=")
                .post(new FormBody
                        .Builder()
                        .add("meal_type", "2")
                        .add("course", "AA")
                        .add("dt", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                        .add("dtFlag", "1")
                        .add("hallNm", "seoulrnd")
                        .add("engYn", "N")
                        .build())
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .addHeader("Referer", "https://www.samsungwelstory.com/menu/seoulrnd/menu.jsp")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                Log.e(MenuActivity.class.getName(), e.getMessage());
                onEvent(null);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = response.body().string();

                Log.d(MenuActivity.class.getName(), responseData);

                boolean isSuccess = false;

                final DayCuisionsDTO e = new DayCuisionsDTO();
                try {
                    Object json = new JSONTokener(responseData).nextValue();

                    if (json instanceof JSONArray) {
                        JSONArray receivedJson = (JSONArray) json;

                        for (int i = 0; i < receivedJson.length(); i++) {
                            JSONObject obj = receivedJson.getJSONObject(i);

                            String typicalMenu = obj.getString("typical_menu");
                            String menuMealType = obj.getString("menu_meal_type");
                            String menuCourseType = obj.getString("menu_course_type");
                            String hallNo = obj.getString("hall_no");
                            String id = menuMealType + menuCourseType + hallNo;

                            Log.d(TAG, "menu id = " + id);

                            if ("Y".equals(typicalMenu)) {
                                CuisineDTO c = new CuisineDTO();
                                if ("3".equals(menuMealType)) {
                                    c.setMealCode(CuisineDTO.MEALCODE_DINNER);
                                } else if ("2".equals(menuMealType)) {
                                    c.setMealCode(CuisineDTO.MEALCODE_LAUNCH);
                                } else {
                                    c.setMealCode(CuisineDTO.MEALCODE_BREAKFAST);
                                }
                                c.setCampusCode(CuisineDTO.CAMPUSCODE_2);
                                c.setCafeteriaCode(MenuItemIconResource.getMenuIcon( obj.getString("course_txt")));
                                c.setCafeteriaCode(0);
                                c.setTitle(obj.getString("menu_name"));
                                c.setContent("");
                                c.setCalorie(obj.getString("tot_kcal"));
                                e.addCuisine(id, c);
                            }
                        }
                        for (int i = 0; i < receivedJson.length(); i++) {
                            JSONObject obj = receivedJson.getJSONObject(i);

                            String typicalMenu = obj.getString("typical_menu");
                            String menuMealType = obj.getString("menu_meal_type");
                            String menuCourseType = obj.getString("menu_course_type");
                            String hallNo = obj.getString("hall_no");
                            String id = menuMealType + menuCourseType + hallNo;

                            Log.d(TAG, "menu id = " + id);
                            if (!"Y".equals(typicalMenu)) {
                                CuisineDTO c = e.getCuisineById(id);

                                if (c != null) {
                                    c.setContent(c.getContent() + obj.getString("menu_name") + ",");
                                }
                            }
                        }
                    }

                    isSuccess = true;

                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }

                if (!isSuccess) {
                    registerServerResponse();
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onEvent(e);
                    }
                });
            }
        });


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        menuOption = new MenuOptionControl();
        menuOption.init(MenuActivity.this);

        menuOption.setShowLicense(this, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LicenseDialog licenseDialog = new LicenseDialog(MenuActivity.this);

                licenseDialog.show();
            }
        });

        RecyclerView gridMenuItems = findViewById(R.id.grid_menu_items);
        gridMenuItems.setLayoutManager(new LayoutManager(this));

        adapter = new MenuItemAdapter(this);
        gridMenuItems.setAdapter(adapter);

        tabBreakfast = findViewById(R.id.menu_tab_breakfast);
        tabLunch = findViewById(R.id.menu_tab_lunch);
        tabDinner = findViewById(R.id.menu_tab_dinner);
        tabOption = findViewById(R.id.menu_tab_setting);

        tabBreakfast.setOnClickListener(TabClickListener);
        tabLunch.setOnClickListener(TabClickListener);
        tabDinner.setOnClickListener(TabClickListener);
        tabOption.setOnClickListener(TabClickListener);

        tabBreakfastText = findViewById(R.id.menu_tab_breakfast_select);
        tabLunchText = findViewById(R.id.menu_tab_lunch_select);
        tabDinnerText = findViewById(R.id.menu_tab_dinner_select);

        setDate();

        onChangeDate(null);

        progress = ProgressDialog.show(MenuActivity.this, "", "Menu data loading....", true);

        registerServerResponse();
    }

    private void registerServerResponse() {

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

    private void setDate() {
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

    public void onEvent(DayCuisionsDTO e) {

        if (progress != null) {
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

//        // Start For Test
//        e = new DayCuisionsDTO();
//
//        CuisineDTO c = new CuisineDTO();
//        c.setMealCode(CuisineDTO.MEALCODE_BREAKFAST);
//        c.setCampusCode(CuisineDTO.CAMPUSCODE_1);
//        c.setCafeteriaUrl("about:blank");
//        c.setCafeteriaCode(0);
//        c.setTitle("Title 아침");
//        c.setContent("Title 아침 구성");
//        e.addCuisine(c);
//
//        c = new CuisineDTO();
//        c.setMealCode(CuisineDTO.MEALCODE_LAUNCH);
//        c.setCampusCode(CuisineDTO.CAMPUSCODE_2);
//        c.setCafeteriaUrl("about:blank");
//        c.setCafeteriaCode(0);
//        c.setTitle("Title 점심");
//        c.setContent("Title 점심 구성");
//        e.addCuisine(c);
//
//
//        c = new CuisineDTO();
//        c.setMealCode(CuisineDTO.MEALCODE_DINNER);
//        c.setCampusCode(CuisineDTO.CAMPUSCODE_1);
//        c.setCafeteriaUrl("about:blank");
//        c.setCafeteriaCode(0);
//        c.setTitle("Title 저녁");
//        c.setContent("Title 저녁 구성");
//        e.addCuisine(c);
//        // End For Test


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
    }

//    public void onEvent(VolleyError error) {
//
//        mTracker.send(new HitBuilders.EventBuilder()
//                .setCategory("MenuActivity")
//                .setAction("onEvent(error)")
//                .build());
//
//        if (progress != null) {
//            progress.dismiss();
//        }
//
//        Toast.makeText(getApplicationContext(), "Network 연결을 확인 하세요.\n Server 응답이 없습니다.", Toast.LENGTH_SHORT).show();
//    }
}

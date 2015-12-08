package net.rivergod.sec.seoulrnd.android.menu;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MenuOptionControl implements CustomPopup.ButtonClickEvent {

    protected static final String HOUR = "_hour";
    protected static final String MINUTE = "_minute";
    protected static final String SELECT = "_select";

    private LinearLayout lyMenu;

    private float maxWidth;
    private float closeViewWidth;

    private Timer timer;
    private float positionX;

    private final int FRAME = 10;
    private final int MOVE_PIXEL = 40;

    private boolean isOpen;

    private TextView tvAlarmUserSet;

    private ImageView ivAlarmCheck0;
    private ImageView ivAlarmCheck1;
    private ImageView ivAlarmCheck2;
    private ImageView ivAlarmCheck3;
    private ImageView ivAlarmCheck4;

    private ImageView prevAlarm;

    private int selectAlarmIndex = -1;

    private CustomPopup popUp;

    SharedPreferences.Editor prefsEditor;

    @Override
    public void onSetTime(String time) {
        if(popUp != null){
            popUp.dismiss();
        }

        int hour = -1;
        int minute = -1;

        if(time.length() == 3 || time.length() == 4){
            int[] convertTimeValues = convertTime(time);

            hour = convertTimeValues[0];
            minute = convertTimeValues[1];
        }else{
            if(time.length() == 0 || time.length() > 4){
                setAlarmSelect(-1, false);
            }

            if(!checkPrevUserAlarmSet()){
                setAlarmSelect(-1, false);
            }
        }

        setUserTime(hour, minute);


        setAlarm(hour, minute);


    }

    private int[] convertTime(String time){

        int[] returnValue = new int[2];

        int hour = -1;
        int minute = -1;
        if(time != null) {
            time = time.replaceAll(":","");
            if (time.length() == 3) {
                hour = Integer.parseInt(time.substring(0, 1));
                minute = Integer.parseInt(time.substring(1, 3));
            } else if (time.length() == 4) {
                hour = Integer.parseInt(time.substring(0, 2));
                minute = Integer.parseInt(time.substring(2, 4));
            }
        }

        returnValue[0] = hour;
        returnValue[1] = minute;

        return returnValue;

    }

    private void setAlarm(int hour, int minute){
        if(hour < 24) {
            prefsEditor.putInt(MenuActivity.ALARM_TAG + HOUR, hour);
            prefsEditor.putInt(MenuActivity.ALARM_TAG + MINUTE, minute);
            prefsEditor.commit();

            if(hour != -1){
                RegisterAlarm.register(tvAlarmUserSet.getContext(), hour, minute);
            }

        }
    }


    private boolean checkPrevUserAlarmSet(){

        boolean returnValue = true;
        if(tvAlarmUserSet != null){
            if(tvAlarmUserSet.getText().toString().length() == 0){
                returnValue = false;
            }
        }

        return returnValue;
    }

    @Override
    public void onSetTimeCancel() {
        if(popUp != null){
            popUp.dismiss();
        }

        if(!checkPrevUserAlarmSet()){
            setAlarmSelect(-1, false);
        }
    }

    @Override
    public void onMessageOk() {
        if(popUp != null){
            popUp.dismiss();
        }
    }

    public void init(MenuActivity activity){
        SharedPreferences prefs = activity.getSharedPreferences(MenuActivity.ALARM_TAG, Context.MODE_PRIVATE);

        prefsEditor = prefs.edit();

        popUp = new CustomPopup(activity, this);

        maxWidth = activity.getResources().getDimension(R.dimen.screen_max);
        closeViewWidth = activity.getResources().getDimension(R.dimen.close_menu);

        lyMenu = (LinearLayout) activity.findViewById(R.id.common_menu_layout);
        lyMenu.setX(maxWidth);

        activity.findViewById(R.id.menu_tab_setting).setOnClickListener(MenuShowClickListener);
        activity.findViewById(R.id.common_menu_close).setOnClickListener(MenuShowClickListener);

        activity.findViewById(R.id.menu_alarm_select_0_layout).setOnClickListener(AlarOptionClickListener);
        activity.findViewById(R.id.menu_alarm_select_1_layout).setOnClickListener(AlarOptionClickListener);
        activity.findViewById(R.id.menu_alarm_select_2_layout).setOnClickListener(AlarOptionClickListener);
        activity.findViewById(R.id.menu_alarm_select_3_layout).setOnClickListener(AlarOptionClickListener);
        activity.findViewById(R.id.menu_alarm_select_4_layout).setOnClickListener(AlarOptionClickListener);

        tvAlarmUserSet = (TextView) activity.findViewById(R.id.option_alarm_user_set_time);
        tvAlarmUserSet.setOnClickListener(AlarOptionClickListener);

        ivAlarmCheck0 = (ImageView) activity.findViewById(R.id.menu_alarm_select_0_check);
        ivAlarmCheck1 = (ImageView) activity.findViewById(R.id.menu_alarm_select_1_check);
        ivAlarmCheck2 = (ImageView) activity.findViewById(R.id.menu_alarm_select_2_check);
        ivAlarmCheck3 = (ImageView) activity.findViewById(R.id.menu_alarm_select_3_check);
        ivAlarmCheck4 = (ImageView) activity.findViewById(R.id.menu_alarm_select_4_check);

        activity.findViewById(R.id.option_license).setOnClickListener(ShowLicense);


        setAlarmSelect(prefs.getInt(MenuActivity.ALARM_TAG + SELECT, -1), true);
        setUserTime(prefs.getInt(MenuActivity.ALARM_TAG + HOUR, -1), prefs.getInt(MenuActivity.ALARM_TAG + MINUTE, -1));

    }

    private void setUserTime(int hour, int minute){

        String timeValue = "";

        if(hour != -1 && hour < 24) {
            timeValue = hour + ":" + (minute == 0 ? "00":minute);
        }

        tvAlarmUserSet.setText(timeValue);
    }

    private View.OnClickListener ShowLicense = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(popUp != null){
                popUp.setTitle("License", false);

                popUp.show();
            }
        }
    };

    public View.OnClickListener getShowLicense() {
        return ShowLicense;
    }

    public void setShowLicense(Activity mainClass, View.OnClickListener showLicense) {
        if (showLicense == null) {
            ShowLicense = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (popUp != null) {
                        popUp.setTitle("License", false);

                        popUp.show();
                    }
                }
            };
        } else {
            ShowLicense = showLicense;
        }

        mainClass.findViewById(R.id.option_license).setOnClickListener(ShowLicense);
    }

    private View.OnClickListener AlarOptionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.option_alarm_user_set_time){
                popUp.setTitle("Alarm Time Set", true);

                String setTime = tvAlarmUserSet.getText().toString();
                if(setTime.length() != 0){
                    setTime = setTime.replaceAll(":","");
                }

                popUp.setTimeText(setTime);
                popUp.show();
            }else {
                setAlarmSelect(Integer.parseInt((String) v.getTag()), false);
            }
        }
    };

    private void setAlarmSelect(int index, boolean isInit){

        if(prevAlarm != null){
            prevAlarm.setVisibility(View.GONE);
            prevAlarm = null;
        }

        if(selectAlarmIndex != index) {
            if (index == 0) {
                prevAlarm = ivAlarmCheck0;
                setAlarm(11, 30);
            } else if (index == 1) {
                prevAlarm = ivAlarmCheck1;
                setAlarm(12, 00);
            } else if (index == 2) {
                prevAlarm = ivAlarmCheck2;
                setAlarm(12, 20);
            } else if (index == 3) {
                prevAlarm = ivAlarmCheck3;
                setAlarm(12, 20);
            } else if (index == 4) {
                prevAlarm = ivAlarmCheck4;
                if(!isInit) {

                    String setTime = tvAlarmUserSet.getText().toString();
                    if(setTime.length() == 0){
                        popUp.setTitle("Alarm Time Set", true);
                        popUp.setTimeText("");
                        popUp.show();
                    }else{
                        int[] convertTimeValues = convertTime(setTime);
                        setAlarm(convertTimeValues[0], convertTimeValues[1]);
                    }
                }
            }
            if(prevAlarm != null) {
                prevAlarm.setVisibility(View.VISIBLE);
            }

            selectAlarmIndex = index;
        }else{
            selectAlarmIndex = -1;
        }

        if(prefsEditor != null && !isInit) {
            prefsEditor.putInt(MenuActivity.ALARM_TAG + SELECT, selectAlarmIndex);
            prefsEditor.commit();
        }

        if(selectAlarmIndex == -1 && !isInit){
            RegisterAlarm.unregister(tvAlarmUserSet.getContext());
        }
    }

    private View.OnClickListener MenuShowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.menu_tab_setting:
                    menuOpen();
                    break;

                case R.id.common_menu_close:
                    menuClose();
                    break;
            }
        }
    };

    public boolean isOpen(){
        return isOpen;
    }

    public void menuOpen(){
        timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                lyMenu.post(new Runnable() {
                    @Override
                    public void run() {
                        lyMenu.setX(positionX);

                        positionX -= MOVE_PIXEL;
                        if (positionX < MOVE_PIXEL) {
                            isOpen = true;
                            lyMenu.setX(0);
                            cancel();
                        }
                    }
                });
            }
        };

        positionX = maxWidth - closeViewWidth;

        timer.schedule(task, 0, FRAME);
    }

    public void menuClose(){
        timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                lyMenu.post(new Runnable() {
                    @Override
                    public void run() {
                        lyMenu.setX(positionX);

                        positionX += MOVE_PIXEL;
                        if (positionX > maxWidth - closeViewWidth) {
                            isOpen = false;
                            lyMenu.setX(maxWidth);
                            cancel();
                        }
                    }
                });
            }
        };

        positionX = 0;

        timer.schedule(task, 0, FRAME);
    }

}

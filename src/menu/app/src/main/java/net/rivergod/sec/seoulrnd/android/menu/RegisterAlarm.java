package net.rivergod.sec.seoulrnd.android.menu;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RegisterAlarm extends BroadcastReceiver {

    private static final String ALARM_TAG= "net.rivergod.sec.seoulrnd.android.menu.alarm";
    private static final long NEXT_DAY = 1000 * 60 * 60 * 24L;

    public static void register(Context context, int hour, int minute){

        if(hour == -1){
            return;
        }

        long nowTime = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long setTime = calendar.getTimeInMillis();

        if(setTime < nowTime ){
            setTime += NEXT_DAY;
        }

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(ALARM_TAG);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarm.cancel(pIntent);
//        alarm.setRepeating(AlarmManager.RTC_WAKEUP, setTime, NEXT_DAY, pIntent);
        alarm.setExact(AlarmManager.RTC_WAKEUP, setTime, pIntent);


    }

    public static void unregister(Context context){

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(ALARM_TAG);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarm.cancel(pIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getAction();
        if(name.equals(ALARM_TAG)){

            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            register(context, hour, minute);

            int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            if(day != Calendar.SUNDAY && day != Calendar.SATURDAY) {
                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MenuActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

                Notification.Builder mBuilder = new Notification.Builder(context);
                mBuilder.setSmallIcon(R.drawable.dishes);
                mBuilder.setTicker("식사 시간 입니다.");
                mBuilder.setWhen(System.currentTimeMillis());
                mBuilder.setContentTitle("오늘은 뭐먹지?");
                mBuilder.setContentText("맛있게 드세요 ^^");
                mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
                mBuilder.setContentIntent(pendingIntent);
                mBuilder.setAutoCancel(true);

                nm.notify(111, mBuilder.build());
            }

        }else if(name.equals(Intent.ACTION_BOOT_COMPLETED)){

            SharedPreferences prefs = context.getSharedPreferences(MenuActivity.ALARM_TAG, Context.MODE_PRIVATE);
            int select = prefs.getInt(MenuActivity.ALARM_TAG + MenuOptionControl.SELECT, -1);
            if(select != -1){
                int hour = prefs.getInt(MenuActivity.ALARM_TAG + MenuOptionControl.HOUR, -1);
                int minute = prefs.getInt(MenuActivity.ALARM_TAG + MenuOptionControl.MINUTE, -1);
                register(context, hour, minute);
            }

        }


    }
}

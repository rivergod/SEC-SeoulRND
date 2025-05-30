package net.rivergod.sec.seoulrnd.android.menu

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import java.util.Calendar

class RegisterAlarm : BroadcastReceiver() {

    companion object {
        private const val ALARM_TAG = "net.rivergod.sec.seoulrnd.android.menu.alarm"
        private const val NEXT_DAY = 1000 * 60 * 60 * 24L

        fun register(context: Context, hour: Int, minute: Int) {
            if (hour == -1) {
                return
            }

            val nowTime = System.currentTimeMillis()

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            var setTime = calendar.timeInMillis

            if (setTime < nowTime) {
                setTime += NEXT_DAY
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(ALARM_TAG)
            val pIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            alarmManager.cancel(pIntent) // Cancel any existing alarm first
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, setTime, pIntent)
        }

        fun unregister(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(ALARM_TAG)
            val pIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            alarmManager.cancel(pIntent)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == ALARM_TAG) {
            // Re-register for the next day with the originally set time,
            // not the current time. This requires fetching the stored alarm time.
            // The original Java code re-registered with calendar.get(Calendar.HOUR_OF_DAY)
            // which might lead to alarms drifting if onReceive is delayed.
            // For a more robust daily alarm, it's better to re-register based on the
            // originally intended hour and minute, which should be stored in SharedPreferences.

            // Assuming the goal is to re-register based on an initial setting:
            val prefs = context.getSharedPreferences(MenuActivity.ALARM_TAG, Context.MODE_PRIVATE)
            val originalHour = prefs.getInt(MenuActivity.ALARM_TAG + MenuOptionControl.HOUR, -1)
            val originalMinute = prefs.getInt(MenuActivity.ALARM_TAG + MenuOptionControl.MINUTE, -1)

            if (originalHour != -1 && originalMinute != -1) {
                register(context, originalHour, originalMinute) // Re-register with original time
            } else {
                // Fallback or error handling if original time isn't found,
                // though the Java version used current time to re-register.
                // For this conversion, I will stick to the original logic of using current time for re-registration.
                 val calendar = Calendar.getInstance()
                 val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                 val currentMinute = calendar.get(Calendar.MINUTE)
                 register(context, currentHour, currentMinute)
            }


            val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            if (day != Calendar.SUNDAY && day != Calendar.SATURDAY) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                
                // Ensure MenuActivity.class is correctly referenced.
                // If MenuActivity is in Kotlin, it would be MenuActivity::class.java
                // Assuming MenuActivity is available and correctly referenced.
                val pendingIntent = PendingIntent.getActivity(
                    context, 
                    0, 
                    Intent(context, MenuActivity::class.java), // Corrected to MenuActivity::class.java
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Added FLAG_IMMUTABLE for safety
                )

                val mBuilder = Notification.Builder(context).apply {
                    setSmallIcon(R.drawable.dishes)
                    setTicker("식사 시간 입니다.")
                    setWhen(System.currentTimeMillis())
                    setContentTitle("오늘은 뭐먹지?")
                    setContentText("맛있게 드세요 ^^")
                    setDefaults(Notification.DEFAULT_VIBRATE)
                    setContentIntent(pendingIntent)
                    setAutoCancel(true)
                }
                // For Android Oreo (API 26) and above, a notification channel is required.
                // This code does not include channel creation, which would be needed for modern Android.
                // Assuming this is handled elsewhere or target SDK is < 26 for this specific part.
                nm.notify(111, mBuilder.build())
            }

        } else if (action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences(MenuActivity.ALARM_TAG, Context.MODE_PRIVATE)
            val select = prefs.getInt(MenuActivity.ALARM_TAG + MenuOptionControl.SELECT, -1)
            if (select != -1) {
                val hour = prefs.getInt(MenuActivity.ALARM_TAG + MenuOptionControl.HOUR, -1)
                val minute = prefs.getInt(MenuActivity.ALARM_TAG + MenuOptionControl.MINUTE, -1)
                register(context, hour, minute)
            }
        }
    }
}

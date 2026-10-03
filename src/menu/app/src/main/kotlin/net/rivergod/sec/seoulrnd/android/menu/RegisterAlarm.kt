package net.rivergod.sec.seoulrnd.android.menu

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

/**
 * 식사 시작 시간 알람.
 * 0.9.14 와 같이 매일 같은 시각에 울리도록 다시 등록하고, 토/일요일에는 알림을 띄우지 않는다.
 */
class RegisterAlarm : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_ALARM -> {
                // 다음날 같은 시각으로 재등록 (0.9.14 는 '현재 시각'으로 재등록해서 조금씩 밀렸다)
                MenuPreferences(context).alarmTime?.let { register(context, it) }

                val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                if (day != Calendar.SATURDAY && day != Calendar.SUNDAY) {
                    showNotification(context)
                }
            }

            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                val prefs = MenuPreferences(context)
                val time = prefs.alarmTime
                if (prefs.selectedAlarm != null && time != null) {
                    register(context, time)
                }
            }
        }
    }

    companion object {
        private const val ACTION_ALARM = "net.rivergod.sec.seoulrnd.android.menu.alarm"
        private const val CHANNEL_ID = "meal_time"
        private const val NOTIFICATION_ID = 111

        fun register(context: Context, time: AlarmTime) {
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, time.hour)
                set(Calendar.MINUTE, time.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
            }

            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val pendingIntent = alarmPendingIntent(context)
            alarmManager.cancel(pendingIntent)

            // Android 12+ 에서 정확한 알람 권한이 없으면 근사 알람으로 대체한다.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        }

        fun unregister(context: Context) {
            context.getSystemService(AlarmManager::class.java).cancel(alarmPendingIntent(context))
        }

        private fun alarmPendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, RegisterAlarm::class.java).setAction(ACTION_ALARM),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        private fun showNotification(context: Context) {
            val manager = NotificationManagerCompat.from(context)
            if (!manager.areNotificationsEnabled()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "식사 시간 알림", NotificationManager.IMPORTANCE_HIGH).apply {
                        enableVibration(true)
                    }
                )
            }

            val contentIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MenuActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.dishes)
                .setTicker("식사 시간 입니다.")
                .setWhen(System.currentTimeMillis())
                .setContentTitle("오늘은 뭐먹지?")
                .setContentText("맛있게 드세요 ^^")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build()

            try {
                manager.notify(NOTIFICATION_ID, notification)
            } catch (e: SecurityException) {
                // POST_NOTIFICATIONS 권한이 회수된 경우
            }
        }
    }
}

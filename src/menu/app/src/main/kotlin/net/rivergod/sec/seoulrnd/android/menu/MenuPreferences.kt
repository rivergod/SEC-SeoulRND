package net.rivergod.sec.seoulrnd.android.menu

import android.content.Context
import androidx.core.content.edit
import net.rivergod.sec.seoulrnd.android.menu.dto.Cafeteria

/** 식사 시작 알람 선택지. 0.9.14 의 option_menu.xml 과 같은 순서/인덱스. */
enum class AlarmOption(val index: Int, val department: String, val hour: Int, val minute: Int) {
    DESIGN(0, "디자인경영", 11, 30),
    DMC(1, "DMC 연구소", 12, 0),
    SW_CENTER(2, "SW 센터", 12, 20),
    SUPPORT(3, "서울R&D지원센터\nIP센터\n무선(사) 서비스전략그룹\n창의 개발 센터", 12, 20),
    CUSTOM(4, "사용자 설정", -1, -1);

    companion object {
        fun fromIndex(index: Int): AlarmOption? = entries.firstOrNull { it.index == index }
    }
}

data class AlarmTime(val hour: Int, val minute: Int) {
    override fun toString() = "%02d:%02d".format(hour, minute)

    companion object {
        /**
         * 사용자 입력("1230", "930", "12:30")을 시간으로 바꾼다. 0.9.14 의 MenuOptionControl.convertTime 과 같은 규칙에
         * 분 범위 검사를 추가했다. 잘못된 입력이면 null.
         */
        fun parse(input: String): AlarmTime? {
            val digits = input.replace(":", "").trim()
            if (digits.length !in 3..4 || !digits.all { it.isDigit() }) return null
            val hour = digits.dropLast(2).toInt()
            val minute = digits.takeLast(2).toInt()
            return if (hour in 0..23 && minute in 0..59) AlarmTime(hour, minute) else null
        }
    }
}

/**
 * 설정 저장소. 알람 관련 키(Alarm_select / Alarm_hour / Alarm_minute)는 0.9.14 와 같은 이름을 써서
 * 업데이트 후에도 기존 알람 설정이 유지되도록 한다.
 */
class MenuPreferences(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /** 목록에서 먼저 보여줄 식당 ('보여지는 순서'). 0.9.14 와 같이 기본값은 2식당(2캠퍼스). */
    var firstCafeteria: Cafeteria
        get() = Cafeteria.entries.firstOrNull { it.name == prefs.getString(KEY_FIRST_CAFETERIA, null) }
            ?: Cafeteria.CAFETERIA_2
        set(value) = prefs.edit { putString(KEY_FIRST_CAFETERIA, value.name) }

    var selectedAlarm: AlarmOption?
        get() = AlarmOption.fromIndex(prefs.getInt(KEY_ALARM_SELECT, -1))
        set(value) = prefs.edit { putInt(KEY_ALARM_SELECT, value?.index ?: -1) }

    /** 현재 등록된 알람 시각 (부팅 시 재등록에 사용). */
    var alarmTime: AlarmTime?
        get() = readTime(KEY_ALARM_HOUR, KEY_ALARM_MINUTE)
        set(value) = writeTime(KEY_ALARM_HOUR, KEY_ALARM_MINUTE, value)

    /** "사용자 설정" 에 입력한 시각. */
    var customAlarmTime: AlarmTime?
        get() = readTime(KEY_CUSTOM_HOUR, KEY_CUSTOM_MINUTE)
            // 0.9.14 는 사용자 설정 시각을 Alarm_hour/minute 에 저장했다.
            ?: if (selectedAlarm == AlarmOption.CUSTOM) alarmTime else null
        set(value) = writeTime(KEY_CUSTOM_HOUR, KEY_CUSTOM_MINUTE, value)

    private fun readTime(hourKey: String, minuteKey: String): AlarmTime? {
        val hour = prefs.getInt(hourKey, -1)
        val minute = prefs.getInt(minuteKey, -1)
        return if (hour in 0..23 && minute in 0..59) AlarmTime(hour, minute) else null
    }

    private fun writeTime(hourKey: String, minuteKey: String, time: AlarmTime?) = prefs.edit {
        putInt(hourKey, time?.hour ?: -1)
        putInt(minuteKey, time?.minute ?: -1)
    }

    private companion object {
        const val PREF_NAME = "Alarm"
        const val KEY_FIRST_CAFETERIA = "Cafeteria_first"
        const val KEY_ALARM_SELECT = "Alarm_select"
        const val KEY_ALARM_HOUR = "Alarm_hour"
        const val KEY_ALARM_MINUTE = "Alarm_minute"
        const val KEY_CUSTOM_HOUR = "Alarm_custom_hour"
        const val KEY_CUSTOM_MINUTE = "Alarm_custom_minute"
    }
}

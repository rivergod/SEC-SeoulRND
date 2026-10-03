package net.rivergod.sec.seoulrnd.android.menu.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import net.rivergod.sec.seoulrnd.android.menu.AlarmOption
import net.rivergod.sec.seoulrnd.android.menu.AlarmTime
import net.rivergod.sec.seoulrnd.android.menu.dto.Cafeteria
import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO
import net.rivergod.sec.seoulrnd.android.menu.dto.MealType
import net.rivergod.sec.seoulrnd.android.menu.dto.MenuArea

/**
 * Firebase Analytics 이벤트 모음. 이벤트·파라미터 이름은 이 파일에서만 정한다.
 *
 * - 화면은 직접 screen_view 로 남긴다 (화면이 Activity 하나라 자동 화면 기록은 매니페스트에서 끔).
 * - debug 빌드는 기본으로 수집하지 않는다 (app/build.gradle.kts 의 seoulrnd.analyticsInDebug).
 * - 이름 규칙: 소문자 snake_case, 이벤트 40자 / 파라미터 값 100자 이하 (Firebase 제한).
 */
class MenuAnalytics(context: Context) {

    private val firebase = FirebaseAnalytics.getInstance(context.applicationContext)

    enum class Screen(val value: String) { MENU("menu"), SETTINGS("settings"), LICENSE("license") }

    fun screen(screen: Screen) = log(
        FirebaseAnalytics.Event.SCREEN_VIEW,
        FirebaseAnalytics.Param.SCREEN_NAME to screen.value,
        FirebaseAnalytics.Param.SCREEN_CLASS to "MenuActivity",
    )

    /** @param method "tab" | "swipe" */
    fun selectMeal(meal: MealType, method: String) = log("select_meal", "meal" to meal.value, "method" to method)

    /** @param direction "previous" | "next" | "today" | "midnight" */
    fun changeDate(direction: String, dayOffset: Long) =
        log("change_date", "direction" to direction, "day_offset" to dayOffset)

    /** @param result "success" | "partial" | "empty" | "error" */
    fun menuLoad(result: String, dayOffset: Long, fromCache: Boolean, failed: Set<Cafeteria>, error: Throwable?) = log(
        "menu_load",
        "result" to result,
        "day_offset" to dayOffset,
        "from_cache" to fromCache.toString(),
        "failed_cafeteria" to failed.joinToString(",") { it.value }.ifEmpty { null },
        "error_type" to error?.let { errorType(it) },
    )

    /** @param source "pull" (당겨서 새로고침) | "button" (다시 시도 버튼) */
    fun refresh(source: String) = log("refresh", "source" to source)

    fun viewCuisine(cuisine: CuisineDTO) = log(
        "view_cuisine",
        "cafeteria" to cuisine.cafeteria.value,
        "corner" to cuisine.courseName.take(100),
        "meal" to cuisine.mealType.value,
        "take_out" to (cuisine.area == MenuArea.TAKE_OUT).toString(),
    )

    fun toggleTakeOut(cafeteria: Cafeteria, expanded: Boolean) =
        log("toggle_take_out", "cafeteria" to cafeteria.value, "expanded" to expanded.toString())

    fun setDisplayOrder(first: Cafeteria) {
        log("set_display_order", "first_cafeteria" to first.value)
        firebase.setUserProperty("first_cafeteria", first.value)
    }

    /** @param option null 이면 알림 끔 */
    fun setAlarm(option: AlarmOption?, time: AlarmTime?) {
        val value = option?.name?.lowercase() ?: "off"
        log("set_alarm", "option" to value, "time" to time?.toString())
        firebase.setUserProperty("alarm_option", value)
    }

    fun notificationPermission(granted: Boolean) = log("notification_permission", "granted" to granted.toString())

    fun openProjectPage() = log("open_project_page")

    fun mealNotificationShown() = log("meal_notification_shown")

    fun openFromNotification() = log("open_from_notification")

    /** 앱 시작 시 현재 설정을 사용자 속성으로 맞춘다. */
    fun syncUserProperties(first: Cafeteria, alarm: AlarmOption?) {
        firebase.setUserProperty("first_cafeteria", first.value)
        firebase.setUserProperty("alarm_option", alarm?.name?.lowercase() ?: "off")
    }

    /** 숫자는 Long, 그 밖에는 문자열 파라미터로 넣는다. null 값은 뺀다. */
    private fun log(event: String, vararg params: Pair<String, Any?>) {
        val bundle = Bundle()
        for ((key, value) in params) {
            when (value) {
                null -> Unit
                is Long -> bundle.putLong(key, value)
                is Int -> bundle.putLong(key, value.toLong())
                else -> bundle.putString(key, value.toString())
            }
        }
        firebase.logEvent(event, bundle)
    }

    private fun errorType(e: Throwable): String = (e.cause ?: e)::class.java.simpleName.take(100)

    private val MealType.value get() = name.lowercase()
    private val Cafeteria.value get() = if (this == Cafeteria.CAFETERIA_1) "cafeteria_1" else "cafeteria_2"
}

package net.rivergod.sec.seoulrnd.android.menu

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.rivergod.sec.seoulrnd.android.menu.analytics.MenuAnalytics
import net.rivergod.sec.seoulrnd.android.menu.data.MenuLoadException
import net.rivergod.sec.seoulrnd.android.menu.data.MenuParseException
import net.rivergod.sec.seoulrnd.android.menu.data.MenuRepository
import net.rivergod.sec.seoulrnd.android.menu.dto.Cafeteria
import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO
import net.rivergod.sec.seoulrnd.android.menu.dto.DayCuisionsDTO
import net.rivergod.sec.seoulrnd.android.menu.dto.MealType
import net.rivergod.sec.seoulrnd.android.menu.dto.MenuArea
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.coroutines.cancellation.CancellationException

sealed interface MenuLoadState {
    data object Loading : MenuLoadState
    data class Loaded(val day: DayCuisionsDTO) : MenuLoadState
    data class Error(val message: String) : MenuLoadState
}

/**
 * 목록의 한 묶음 (식당 x 식당 코너/Take Out).
 * @param failed 이 식당의 메뉴를 불러오지 못함 (items 는 비어 있음)
 */
data class MenuSection(
    val cafeteria: Cafeteria,
    val area: MenuArea,
    val items: List<CuisineDTO>,
    val failed: Boolean = false,
) {
    val title: String
        get() = if (area == MenuArea.TAKE_OUT) "${cafeteria.shortLabel} Take Out" else cafeteria.label
}

/**
 * @param date 보고 있는 날짜. 오늘(today) 기준 ±[MAX_DAY_OFFSET] 일 안에서 옮길 수 있다.
 */
data class MenuUiState(
    val date: LocalDate,
    val today: LocalDate = date,
    val loadState: MenuLoadState = MenuLoadState.Loading,
    val isSampleData: Boolean = false,
    val selectedMeal: MealType = mealTypeFor(LocalTime.now()),
    val firstCafeteria: Cafeteria = Cafeteria.CAFETERIA_2,
    val selectedAlarm: AlarmOption? = null,
    val customAlarmTime: AlarmTime? = null,
    val isRefreshing: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val showTimeDialog: Boolean = false,
    val showLicenseDialog: Boolean = false,
) {
    val isToday: Boolean get() = date == today
    val canGoPrevious: Boolean get() = date > today.minusDays(MAX_DAY_OFFSET)
    val canGoNext: Boolean get() = date < today.plusDays(MAX_DAY_OFFSET)

    /** 오늘과의 차이: 0 오늘, -1 어제, 1 내일 … */
    val dayOffset: Long get() = ChronoUnit.DAYS.between(today, date)

    /** 선택된 끼니의 묶음. */
    val sections: List<MenuSection> get() = sectionsFor(selectedMeal)

    /**
     * 한 끼니의 메뉴를 식당별로 묶는다. '보여지는 순서' 설정의 식당이 먼저 오고,
     * 식당 안에서는 식당 코너 다음에 Take Out 이 온다.
     */
    fun sectionsFor(meal: MealType): List<MenuSection> {
        val day = (loadState as? MenuLoadState.Loaded)?.day ?: return emptyList()
        val cafeteriaOrder = listOf(firstCafeteria) + Cafeteria.entries.filter { it != firstCafeteria }
        val items = day.forMeal(meal)
        return cafeteriaOrder.flatMap { cafeteria ->
            if (cafeteria in day.failedCafeterias) {
                listOf(MenuSection(cafeteria, MenuArea.DINE_IN, emptyList(), failed = true))
            } else {
                MenuArea.entries.mapNotNull { area ->
                    items.filter { it.cafeteria == cafeteria && it.area == area }
                        .takeIf { it.isNotEmpty() }
                        ?.let { MenuSection(cafeteria, area, it) }
                }
            }
        }
    }
}

sealed interface MenuEvent {
    data class Toast(val message: String) : MenuEvent
    data object RequestNotificationPermission : MenuEvent
}

/** 오늘 기준으로 앞뒤로 볼 수 있는 날 수. */
const val MAX_DAY_OFFSET = 5L

/** 0.9.14 와 같이 10시 전은 조식, 14시 전은 중식, 그 이후는 석식을 먼저 보여준다. */
fun mealTypeFor(time: LocalTime): MealType = when {
    time.hour < 10 -> MealType.BREAKFAST
    time.hour < 14 -> MealType.LUNCH
    else -> MealType.DINNER
}

class MenuViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MenuRepository.create()
    private val prefs = MenuPreferences(application)
    private val analytics = MenuAnalytics(application)

    private val _uiState = MutableStateFlow(
        MenuUiState(
            date = LocalDate.now(),
            isSampleData = repository.isSample,
            firstCafeteria = prefs.firstCafeteria,
            selectedAlarm = prefs.selectedAlarm,
            customAlarmTime = prefs.customAlarmTime,
        )
    )
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private val _events = Channel<MenuEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var loadJob: Job? = null

    /** 이번 실행 동안 불러온 날짜별 메뉴. 날짜를 앞뒤로 옮길 때 다시 받지 않는다. (실패한 식당이 있으면 담지 않음) */
    private val cache = HashMap<LocalDate, DayCuisionsDTO>()

    init {
        analytics.syncUserProperties(prefs.firstCafeteria, prefs.selectedAlarm)
        analytics.screen(MenuAnalytics.Screen.MENU)
        load(_uiState.value.date)
    }

    /** 화면이 다시 보일 때 날짜가 바뀌었으면(자정을 넘김) 새 오늘로 옮긴다. */
    fun refreshIfDateChanged() {
        val now = LocalDate.now()
        if (_uiState.value.today == now) return
        cache.clear()
        _uiState.update { it.copy(today = now) }
        showDate(now, "midnight")
    }

    fun showPreviousDay() = showDate(_uiState.value.date.minusDays(1), "previous")

    fun showNextDay() = showDate(_uiState.value.date.plusDays(1), "next")

    fun showToday() = showDate(_uiState.value.today, "today")

    private fun showDate(target: LocalDate, direction: String) {
        val state = _uiState.value
        val date = target.coerceIn(state.today.minusDays(MAX_DAY_OFFSET), state.today.plusDays(MAX_DAY_OFFSET))
        if (date == state.date && state.loadState !is MenuLoadState.Error) return
        _uiState.update {
            // 오늘로 돌아오면 시간대에 맞는 끼니를, 다른 날은 보던 끼니를 그대로 보여준다
            it.copy(date = date, selectedMeal = if (date == it.today) mealTypeFor(LocalTime.now()) else it.selectedMeal)
        }
        analytics.changeDate(direction, _uiState.value.dayOffset)
        load(date)
    }

    fun retry() {
        analytics.refresh("button")
        load(_uiState.value.date, force = true)
    }

    /** 당겨서 새로고침. 불러오는 동안 지금 보이는 메뉴를 유지하고, 실패하면 알림만 띄운다. */
    fun refresh() {
        analytics.refresh("pull")
        val state = _uiState.value
        load(state.date, force = true, keepContent = state.loadState is MenuLoadState.Loaded)
    }

    private fun load(date: LocalDate, force: Boolean = false, keepContent: Boolean = false) {
        loadJob?.cancel()
        val dayOffset = ChronoUnit.DAYS.between(_uiState.value.today, date)
        val cached = cache[date]
        if (cached != null && !force) {
            logMenuLoad(cached, dayOffset, fromCache = true)
            _uiState.update { it.copy(loadState = MenuLoadState.Loaded(cached), isRefreshing = false) }
            return
        }
        _uiState.update {
            if (keepContent) it.copy(isRefreshing = true) else it.copy(loadState = MenuLoadState.Loading, isRefreshing = false)
        }
        loadJob = viewModelScope.launch {
            val newState = try {
                val day = repository.load(date)
                if (day.failedCafeterias.isEmpty()) cache[date] = day
                logMenuLoad(day, dayOffset, fromCache = false)
                MenuLoadState.Loaded(day)
            } catch (e: CancellationException) {
                throw e
            } catch (e: MenuLoadException) {
                // 두 식당 모두 실패한 경우. 원인별 로그는 MenuRepository 가 남긴다.
                analytics.menuLoad("error", dayOffset, fromCache = false, failed = Cafeteria.entries.toSet(), error = e)
                if (e.cause is MenuParseException) {
                    MenuLoadState.Error("식단 정보를 해석할 수 없습니다.")
                } else {
                    MenuLoadState.Error(NETWORK_ERROR_MESSAGE).also { _events.trySend(MenuEvent.Toast(NETWORK_ERROR_MESSAGE)) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "menu load failed", e)
                analytics.menuLoad("error", dayOffset, fromCache = false, failed = Cafeteria.entries.toSet(), error = e)
                MenuLoadState.Error(NETWORK_ERROR_MESSAGE).also { _events.trySend(MenuEvent.Toast(NETWORK_ERROR_MESSAGE)) }
            }
            _uiState.update {
                when {
                    // 그사이 다른 날짜로 옮겼으면 버린다
                    it.date != date -> it
                    keepContent && newState is MenuLoadState.Error -> it.copy(isRefreshing = false)
                    else -> it.copy(loadState = newState, isRefreshing = false)
                }
            }
        }
    }

    private fun logMenuLoad(day: DayCuisionsDTO, dayOffset: Long, fromCache: Boolean) {
        val result = when {
            day.failedCafeterias.isNotEmpty() -> "partial"
            day.cuisines.isEmpty() -> "empty"
            else -> "success"
        }
        analytics.menuLoad(result, dayOffset, fromCache, day.failedCafeterias, error = null)
    }

    /** @param method "tab" | "swipe" */
    fun selectMeal(mealType: MealType, method: String) {
        if (_uiState.value.selectedMeal == mealType) return
        _uiState.update { it.copy(selectedMeal = mealType) }
        analytics.selectMeal(mealType, method)
    }

    fun openSettings() {
        _uiState.update { it.copy(isSettingsOpen = true) }
        analytics.screen(MenuAnalytics.Screen.SETTINGS)
    }

    fun closeSettings() {
        _uiState.update { it.copy(isSettingsOpen = false) }
        analytics.screen(MenuAnalytics.Screen.MENU)
    }

    fun selectFirstCafeteria(cafeteria: Cafeteria) {
        if (_uiState.value.firstCafeteria == cafeteria) return
        prefs.firstCafeteria = cafeteria
        _uiState.update { it.copy(firstCafeteria = cafeteria) }
        analytics.setDisplayOrder(cafeteria)
    }

    fun onCuisineViewed(cuisine: CuisineDTO) = analytics.viewCuisine(cuisine)

    fun onTakeOutToggled(cafeteria: Cafeteria, expanded: Boolean) = analytics.toggleTakeOut(cafeteria, expanded)

    fun onNotificationPermissionResult(granted: Boolean) = analytics.notificationPermission(granted)

    fun onProjectPageOpened() = analytics.openProjectPage()

    fun onOpenedFromNotification() = analytics.openFromNotification()

    /** @param option null 이면 알림을 끈다. 시각이 정해지지 않은 '사용자 설정' 은 시간 선택을 먼저 띄운다. */
    fun selectAlarm(option: AlarmOption?) {
        when (option) {
            null -> clearAlarm()
            AlarmOption.CUSTOM -> {
                val time = _uiState.value.customAlarmTime
                if (time == null) showTimeDialog() else applyAlarm(option, time)
            }
            else -> applyAlarm(option, AlarmTime(option.hour, option.minute))
        }
    }

    fun showTimeDialog() = _uiState.update { it.copy(showTimeDialog = true) }

    fun dismissTimeDialog() = _uiState.update { it.copy(showTimeDialog = false) }

    fun onCustomTimeSet(time: AlarmTime) {
        prefs.customAlarmTime = time
        _uiState.update { it.copy(customAlarmTime = time, showTimeDialog = false) }
        applyAlarm(AlarmOption.CUSTOM, time)
    }

    fun showLicense(show: Boolean) {
        _uiState.update { it.copy(showLicenseDialog = show) }
        analytics.screen(if (show) MenuAnalytics.Screen.LICENSE else MenuAnalytics.Screen.SETTINGS)
    }

    private fun applyAlarm(option: AlarmOption, time: AlarmTime) {
        prefs.selectedAlarm = option
        prefs.alarmTime = time
        RegisterAlarm.register(getApplication(), time)
        _uiState.update { it.copy(selectedAlarm = option) }
        analytics.setAlarm(option, time)
        _events.trySend(MenuEvent.RequestNotificationPermission)
        _events.trySend(MenuEvent.Toast("평일 $time 에 알림이 울립니다."))
    }

    private fun clearAlarm() {
        if (_uiState.value.selectedAlarm == null) return
        prefs.selectedAlarm = null
        prefs.alarmTime = null
        RegisterAlarm.unregister(getApplication())
        _uiState.update { it.copy(selectedAlarm = null) }
        analytics.setAlarm(null, null)
    }

    private companion object {
        const val TAG = "MenuViewModel"
        const val NETWORK_ERROR_MESSAGE = "Network 연결을 확인 하세요.\n Server 응답이 없습니다."
    }
}

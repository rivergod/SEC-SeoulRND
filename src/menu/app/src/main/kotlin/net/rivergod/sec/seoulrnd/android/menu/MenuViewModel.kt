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

data class MenuUiState(
    val date: LocalDate,
    val loadState: MenuLoadState = MenuLoadState.Loading,
    val isSampleData: Boolean = false,
    val selectedMeal: MealType = mealTypeFor(LocalTime.now()),
    val firstCafeteria: Cafeteria = Cafeteria.CAFETERIA_2,
    val selectedAlarm: AlarmOption? = null,
    val customAlarmTime: AlarmTime? = null,
    val isOptionMenuOpen: Boolean = false,
    val showTimeDialog: Boolean = false,
    val showLicenseDialog: Boolean = false,
) {
    /**
     * 선택된 끼니의 메뉴를 식당별로 묶는다. '보여지는 순서' 설정의 식당이 먼저 오고,
     * 식당 안에서는 식당 코너 다음에 Take Out 이 온다.
     */
    val sections: List<MenuSection>
        get() {
            val day = (loadState as? MenuLoadState.Loaded)?.day ?: return emptyList()
            val cafeteriaOrder = listOf(firstCafeteria) + Cafeteria.entries.filter { it != firstCafeteria }
            val items = day.forMeal(selectedMeal)
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

/** 0.9.14 와 같이 10시 전은 조식, 14시 전은 중식, 그 이후는 석식을 먼저 보여준다. */
fun mealTypeFor(time: LocalTime): MealType = when {
    time.hour < 10 -> MealType.BREAKFAST
    time.hour < 14 -> MealType.LUNCH
    else -> MealType.DINNER
}

class MenuViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MenuRepository.create()
    private val prefs = MenuPreferences(application)

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

    init {
        load()
    }

    /** 화면이 다시 보일 때 날짜가 바뀌었으면 새로 불러온다. */
    fun refreshIfDateChanged() {
        if (_uiState.value.date != LocalDate.now()) load()
    }

    fun retry() = load()

    private fun load() {
        val date = LocalDate.now()
        loadJob?.cancel()
        _uiState.update {
            it.copy(date = date, loadState = MenuLoadState.Loading, selectedMeal = mealTypeFor(LocalTime.now()))
        }
        loadJob = viewModelScope.launch {
            val newState = try {
                MenuLoadState.Loaded(repository.load(date))
            } catch (e: CancellationException) {
                throw e
            } catch (e: MenuLoadException) {
                // 두 식당 모두 실패한 경우. 원인별 로그는 MenuRepository 가 남긴다.
                if (e.cause is MenuParseException) {
                    MenuLoadState.Error("식단 정보를 해석할 수 없습니다.")
                } else {
                    MenuLoadState.Error(NETWORK_ERROR_MESSAGE).also { _events.trySend(MenuEvent.Toast(NETWORK_ERROR_MESSAGE)) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "menu load failed", e)
                MenuLoadState.Error(NETWORK_ERROR_MESSAGE).also { _events.trySend(MenuEvent.Toast(NETWORK_ERROR_MESSAGE)) }
            }
            _uiState.update { it.copy(loadState = newState) }
        }
    }

    fun selectMeal(mealType: MealType) = _uiState.update { it.copy(selectedMeal = mealType) }

    fun toggleOptionMenu() = _uiState.update { it.copy(isOptionMenuOpen = !it.isOptionMenuOpen) }

    fun closeOptionMenu() = _uiState.update { it.copy(isOptionMenuOpen = false) }

    fun selectFirstCafeteria(cafeteria: Cafeteria) {
        prefs.firstCafeteria = cafeteria
        _uiState.update { it.copy(firstCafeteria = cafeteria) }
    }

    fun onAlarmOptionClick(option: AlarmOption) {
        val state = _uiState.value
        when {
            // 이미 선택된 항목을 다시 누르면 해제 (0.9.14 동작)
            option == state.selectedAlarm -> clearAlarm()
            option == AlarmOption.CUSTOM -> {
                val time = state.customAlarmTime
                if (time == null) showTimeDialog() else applyAlarm(option, time)
            }
            else -> applyAlarm(option, AlarmTime(option.hour, option.minute))
        }
    }

    fun showTimeDialog() = _uiState.update { it.copy(showTimeDialog = true) }

    fun dismissTimeDialog() = _uiState.update { it.copy(showTimeDialog = false) }

    /** @return 입력이 올바르면 true. 잘못된 입력이면 다이얼로그를 유지한다. */
    fun onCustomTimeEntered(input: String): Boolean {
        val time = AlarmTime.parse(input) ?: return false
        prefs.customAlarmTime = time
        _uiState.update { it.copy(customAlarmTime = time, showTimeDialog = false) }
        applyAlarm(AlarmOption.CUSTOM, time)
        return true
    }

    fun showLicense(show: Boolean) = _uiState.update { it.copy(showLicenseDialog = show) }

    private fun applyAlarm(option: AlarmOption, time: AlarmTime) {
        prefs.selectedAlarm = option
        prefs.alarmTime = time
        RegisterAlarm.register(getApplication(), time)
        _uiState.update { it.copy(selectedAlarm = option) }
        _events.trySend(MenuEvent.RequestNotificationPermission)
        _events.trySend(MenuEvent.Toast("매일 $time 에 알림이 울립니다. (주말 제외)"))
    }

    private fun clearAlarm() {
        prefs.selectedAlarm = null
        prefs.alarmTime = null
        RegisterAlarm.unregister(getApplication())
        _uiState.update { it.copy(selectedAlarm = null) }
    }

    private companion object {
        const val TAG = "MenuViewModel"
        const val NETWORK_ERROR_MESSAGE = "Network 연결을 확인 하세요.\n Server 응답이 없습니다."
    }
}

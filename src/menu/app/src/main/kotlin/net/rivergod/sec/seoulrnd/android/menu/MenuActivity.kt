package net.rivergod.sec.seoulrnd.android.menu

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO
import net.rivergod.sec.seoulrnd.android.menu.dto.MealType
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.SecSeoulRnDMenuTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class MenuActivity : ComponentActivity() {

    private val viewModel: MenuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SecSeoulRnDMenuTheme {
                MenuRoute(viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshIfDateChanged()
    }
}

@Composable
private fun MenuRoute(viewModel: MenuViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) {
            Toast.makeText(context, "알림 권한이 없어 식사 시간 알림을 표시할 수 없습니다.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is MenuEvent.Toast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                MenuEvent.RequestNotificationPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        }
    }

    BackHandler(enabled = state.isSettingsOpen) { viewModel.closeSettings() }

    // 설정은 메뉴 화면 위로 밀려 들어오는 별도 화면 (0.9.14 는 오른쪽에서 열리는 패널)
    AnimatedContent(
        targetState = state.isSettingsOpen,
        transitionSpec = {
            if (targetState) {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it / 4 }
            } else {
                slideInHorizontally { -it / 4 } togetherWith slideOutHorizontally { it }
            }
        },
        label = "settings",
    ) { settingsOpen ->
        if (settingsOpen) {
            SettingsScreen(
                firstCafeteria = state.firstCafeteria,
                selectedAlarm = state.selectedAlarm,
                customAlarmTime = state.customAlarmTime,
                onBack = viewModel::closeSettings,
                onCafeteriaSelected = viewModel::selectFirstCafeteria,
                onAlarmSelected = viewModel::selectAlarm,
                onChangeCustomTime = viewModel::showTimeDialog,
                onShowLicense = { viewModel.showLicense(true) },
            )
        } else {
            MenuScreen(
                state = state,
                onMealSelected = viewModel::selectMeal,
                onPreviousDay = viewModel::showPreviousDay,
                onNextDay = viewModel::showNextDay,
                onToday = viewModel::showToday,
                onSettingsClick = viewModel::openSettings,
                onRefresh = viewModel::refresh,
                onRetry = viewModel::retry,
            )
        }
    }

    if (state.showTimeDialog) {
        AlarmTimePickerDialog(
            initialTime = state.customAlarmTime,
            onConfirm = viewModel::onCustomTimeSet,
            onDismiss = viewModel::dismissTimeDialog,
        )
    }

    if (state.showLicenseDialog) {
        LicenseDialog(onDismissRequest = { viewModel.showLicense(false) })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    state: MenuUiState,
    onMealSelected: (MealType) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onSettingsClick: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
) {
    var detail by remember { mutableStateOf<CuisineDTO?>(null) }

    // 조식·중식·석식을 좌우로 넘기는 페이지. 탭과 같은 순서(MealType 선언 순서)
    val meals = MealType.entries
    val pagerState = rememberPagerState(initialPage = state.selectedMeal.ordinal) { meals.size }
    val scope = rememberCoroutineScope()

    // 손으로 넘겨서 멈춘 페이지를 선택된 끼니로 반영
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { onMealSelected(meals[it]) }
    }
    // ViewModel 이 끼니를 바꾼 경우(오늘로 돌아오며 시간대 끼니 선택 등) 페이지를 따라 옮김
    LaunchedEffect(state.selectedMeal) {
        if (pagerState.settledPage != state.selectedMeal.ordinal) {
            pagerState.animateScrollToPage(state.selectedMeal.ordinal)
        }
    }

    Scaffold(
        topBar = {
            Column {
                MenuTopBar(
                    isSampleData = state.isSampleData,
                    showTodayButton = !state.isToday,
                    onToday = onToday,
                    onSettingsClick = onSettingsClick,
                )
                DateBar(
                    date = state.date,
                    dayOffset = state.dayOffset,
                    canGoPrevious = state.canGoPrevious,
                    canGoNext = state.canGoNext,
                    onPrevious = onPreviousDay,
                    onNext = onNextDay,
                )
                MealTabs(
                    // 넘기는 중에도 탭 표시가 바로 따라오도록 currentPage 를 쓴다
                    selected = meals[pagerState.currentPage],
                    onSelected = { meal -> scope.launch { pagerState.animateScrollToPage(meal.ordinal) } },
                )
            }
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = { meals[it] },
            ) { page ->
                MealPage(
                    state = state,
                    meal = meals[page],
                    onCuisineClick = { detail = it },
                    onRetry = onRetry,
                )
            }
        }
    }

    detail?.let { CuisineDetailSheet(it, onDismiss = { detail = null }) }
}

/** 한 끼니 페이지. 불러오는 중·오류·빈 날 안내도 페이지 안에 두어 어디서든 좌우로 넘길 수 있게 한다. */
@Composable
private fun MealPage(
    state: MenuUiState,
    meal: MealType,
    onCuisineClick: (CuisineDTO) -> Unit,
    onRetry: () -> Unit,
) {
    when (val load = state.loadState) {
        MenuLoadState.Loading -> MenuMessage("메뉴를 불러오는 중입니다") {
            Spacer(Modifier.height(20.dp))
            CircularProgressIndicator()
        }

        is MenuLoadState.Error -> MenuMessage(load.message, icon = Icons.Filled.Warning) {
            Spacer(Modifier.height(20.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Text("다시 시도", modifier = Modifier.padding(start = 8.dp))
            }
        }

        is MenuLoadState.Loaded -> {
            val sections = state.sectionsFor(meal)
            when {
                // 두 식당 서버 모두 주말·공휴일에는 빈 목록을 준다
                load.day.cuisines.isEmpty() && load.day.failedCafeterias.isEmpty() ->
                    MenuMessage(
                        (if (state.isToday) "오늘은" else "이 날은") + " 식단 정보가 없습니다.\n" +
                            "주말·공휴일에는 식당을 운영하지 않습니다.",
                        icon = Icons.Filled.DateRange
                    )

                sections.isEmpty() -> MenuMessage("등록된 메뉴가 없습니다.", icon = Icons.Filled.DateRange)

                // 날짜나 보여지는 순서가 바뀌면 맨 위부터 보여준다.
                // (key 없이 두면 LazyGrid 가 이전 항목 위치를 따라가 스크롤이 유지된다)
                else -> key(state.date, state.firstCafeteria) {
                    MenuGrid(sections, onCuisineClick = onCuisineClick, onRetry = onRetry)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuTopBar(
    isSampleData: Boolean,
    showTodayButton: Boolean,
    onToday: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Column {
                Text(stringResource(R.string.menu_title), fontWeight = FontWeight.Bold)
                if (isSampleData) {
                    Text(
                        "예시 데이터",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                    )
                }
            }
        },
        actions = {
            if (showTodayButton) {
                TextButton(
                    onClick = onToday,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                ) { Text("오늘", fontWeight = FontWeight.Bold) }
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Filled.Settings, contentDescription = "설정")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealTabs(selected: MealType, onSelected: (MealType) -> Unit) {
    val meals = listOf(MealType.BREAKFAST to "조식", MealType.LUNCH to "중식", MealType.DINNER to "석식")
    PrimaryTabRow(
        selectedTabIndex = meals.indexOfFirst { it.first == selected },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        meals.forEach { (meal, label) ->
            Tab(
                selected = meal == selected,
                onClick = { onSelected(meal) },
                text = { Text(label, fontWeight = if (meal == selected) FontWeight.Bold else FontWeight.Normal) },
                unselectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
        }
    }
}

/** 날짜와 앞뒤 이동 버튼. 오늘 기준 ±[MAX_DAY_OFFSET] 일을 넘으면 버튼이 꺼진다. */
@Composable
private fun DateBar(
    date: LocalDate,
    dayOffset: Long,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious, enabled = canGoPrevious) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "이전 날",
                tint = if (canGoPrevious) contentColor else contentColor.copy(alpha = 0.3f)
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                formatHeaderDate(date),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
            )
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (dayOffset == 0L) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.12f),
                contentColor = if (dayOffset == 0L) MaterialTheme.colorScheme.onPrimary else contentColor,
            ) {
                Text(
                    relativeDayLabel(dayOffset),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "다음 날",
                tint = if (canGoNext) contentColor else contentColor.copy(alpha = 0.3f)
            )
        }
    }
}

/** "10월 3일 (토)" */
private fun formatHeaderDate(date: LocalDate): String =
    "${date.monthValue}월 ${date.dayOfMonth}일 (${date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)})"

/** 0 오늘, ±1 내일/어제, 그 밖에는 "n일 후" / "n일 전" */
internal fun relativeDayLabel(dayOffset: Long): String = when {
    dayOffset == 0L -> "오늘"
    dayOffset == 1L -> "내일"
    dayOffset == -1L -> "어제"
    dayOffset > 0 -> "${dayOffset}일 후"
    else -> "${-dayOffset}일 전"
}

@Preview(widthDp = 380, heightDp = 760)
@Composable
private fun MenuScreenPreview() {
    SecSeoulRnDMenuTheme {
        MenuScreen(
            state = MenuUiState(
                date = LocalDate.of(2026, 10, 2),
                today = LocalDate.of(2026, 10, 3),
                loadState = MenuLoadState.Loading,
                isSampleData = true
            ),
            onMealSelected = {},
            onPreviousDay = {},
            onNextDay = {},
            onToday = {},
            onSettingsClick = {},
            onRefresh = {},
            onRetry = {},
        )
    }
}

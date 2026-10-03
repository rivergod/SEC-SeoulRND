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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.rivergod.sec.seoulrnd.android.menu.dto.MealType
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.HeaderBlue
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.HeaderSubText
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.SecSeoulRnDMenuTheme
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.TabBarBackground
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.TabText
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

    BackHandler(enabled = state.isOptionMenuOpen) { viewModel.closeOptionMenu() }

    MenuScreen(
        state = state,
        onMealSelected = viewModel::selectMeal,
        onSettingsClick = viewModel::toggleOptionMenu,
        onCloseOptions = viewModel::closeOptionMenu,
        onRetry = viewModel::retry,
        optionsContent = {
            MenuOptionsScreen(
                firstArea = state.firstArea,
                selectedAlarm = state.selectedAlarm,
                customAlarmTime = state.customAlarmTime,
                onAreaSelected = viewModel::selectFirstArea,
                onAlarmOptionSelected = viewModel::onAlarmOptionClick,
                onCustomAlarmTimeClick = viewModel::showTimeDialog,
                onShowLicense = { viewModel.showLicense(true) },
            )
        },
    )

    if (state.showTimeDialog) {
        AlarmTimeSetPopup(
            initialTime = state.customAlarmTime,
            onSetTime = viewModel::onCustomTimeEntered,
            onCancel = viewModel::dismissTimeDialog,
        )
    }

    if (state.showLicenseDialog) {
        LicenseDialog(onDismissRequest = { viewModel.showLicense(false) })
    }
}

@Composable
fun MenuScreen(
    state: MenuUiState,
    onMealSelected: (MealType) -> Unit,
    onSettingsClick: () -> Unit,
    onCloseOptions: () -> Unit,
    onRetry: () -> Unit,
    optionsContent: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .background(HeaderBlue)
                .windowInsetsTopHeight(WindowInsets.statusBars)
        )

        // 0.9.14 와 같이 폭 360dp 의 화면을 가운데에 둔다.
        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            MenuHeader(date = state.date, isSampleData = state.isSampleData)

            Box(modifier = Modifier.weight(1f)) {
                when (val load = state.loadState) {
                    MenuLoadState.Loading -> MenuMessage("Menu data loading....") {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                    }

                    is MenuLoadState.Error -> MenuMessage(load.message) {
                        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) { Text("다시 시도") }
                    }

                    is MenuLoadState.Loaded -> {
                        val sections = state.sections
                        if (load.day.cuisines.isEmpty()) {
                            // 웰스토리 서버는 주말·공휴일에 빈 목록을 준다
                            MenuMessage("오늘은 식단 정보가 없습니다.\n(주말·공휴일)")
                        } else if (sections.isEmpty()) {
                            MenuMessage("등록된 메뉴가 없습니다.")
                        } else {
                            // 끼니나 보여지는 순서가 바뀌면 맨 위부터 보여준다.
                            // (key 없이 두면 LazyGrid 가 이전 항목 위치를 따라가 스크롤이 유지된다)
                            key(state.selectedMeal, state.firstArea) {
                                MenuGrid(sections)
                            }
                        }
                    }
                }

                OptionPanel(visible = state.isOptionMenuOpen, onClose = onCloseOptions, content = optionsContent)
            }

            BottomMenuBar(
                selectedMealType = state.selectedMeal,
                onMealTypeSelected = onMealSelected,
                onSettingsClick = onSettingsClick,
            )
        }

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

@Composable
private fun MenuHeader(date: LocalDate, isSampleData: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(HeaderBlue)
    ) {
        Text(
            text = stringResource(R.string.menu_sub_title),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(5.dp),
            color = HeaderSubText,
            fontSize = 11.sp
        )
        Text(
            text = stringResource(R.string.menu_title),
            modifier = Modifier.align(Alignment.Center),
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (isSampleData) {
            Text(
                text = "예시 데이터",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(5.dp),
                color = HeaderSubText,
                fontSize = 10.sp
            )
        }
        Text(
            text = formatHeaderDate(date),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(5.dp),
            color = HeaderSubText,
            fontSize = 12.sp
        )
    }
}

/** "10월 3일 (토)" */
private fun formatHeaderDate(date: LocalDate): String =
    "${date.monthValue}월 ${date.dayOfMonth}일 (${date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)})"

/** 오른쪽에서 밀려 들어오는 설정 패널. 왼쪽 빈 영역을 누르면 닫힌다. (0.9.14 common_menu_close) */
@Composable
private fun OptionPanel(visible: Boolean, onClose: () -> Unit, content: @Composable () -> Unit) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x33000000))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                )
        )
    }
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.End) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it },
        ) {
            Box(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    // 패널 안쪽 터치가 뒤의 닫기 영역으로 전달되지 않도록 막는다.
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            ) {
                content()
            }
        }
    }
}

@Composable
fun BottomMenuBar(
    selectedMealType: MealType,
    onMealTypeSelected: (MealType) -> Unit,
    onSettingsClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(HeaderBlue)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(TabBarBackground),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                MealType.BREAKFAST to "Breakfast",
                MealType.LUNCH to "Lunch",
                MealType.DINNER to "Dinner",
            ).forEach { (mealType, label) ->
                MealTab(
                    text = label,
                    isSelected = selectedMealType == mealType,
                    onClick = { onMealTypeSelected(mealType) },
                    modifier = Modifier.weight(1f)
                )
                Box(
                    Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(HeaderBlue)
                )
            }
            Box(
                modifier = Modifier
                    .width(57.dp)
                    .fillMaxHeight()
                    .clickable(onClick = onSettingsClick),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.icon_setting),
                    contentDescription = "설정",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
private fun MealTab(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(if (isSelected) HeaderBlue else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else TabText,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun MenuScreenPreview() {
    SecSeoulRnDMenuTheme {
        MenuScreen(
            state = MenuUiState(date = LocalDate.of(2024, 11, 1), loadState = MenuLoadState.Loading, isSampleData = true),
            onMealSelected = {},
            onSettingsClick = {},
            onCloseOptions = {},
            onRetry = {},
            optionsContent = {},
        )
    }
}

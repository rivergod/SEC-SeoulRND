package net.rivergod.sec.seoulrnd.android.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.rivergod.sec.seoulrnd.android.menu.dto.Cafeteria
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.LocalBrandColors
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.SecSeoulRnDMenuTheme

/** 설정 화면: 보여지는 순서, 식사 시간 알림, 2식당 운영 시간·지도, 정보. (0.9.14 option_menu.xml 의 내용) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    firstCafeteria: Cafeteria,
    selectedAlarm: AlarmOption?,
    customAlarmTime: AlarmTime?,
    onBack: () -> Unit,
    onCafeteriaSelected: (Cafeteria) -> Unit,
    onAlarmSelected: (AlarmOption?) -> Unit,
    onChangeCustomTime: () -> Unit,
    onShowLicense: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            SectionTitle("보여지는 순서", "먼저 보여줄 식당을 고릅니다.")
            SingleChoiceSegmentedButtonRow(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Cafeteria.entries.forEachIndexed { index, cafeteria ->
                    SegmentedButton(
                        selected = cafeteria == firstCafeteria,
                        onClick = { onCafeteriaSelected(cafeteria) },
                        shape = SegmentedButtonDefaults.itemShape(index, Cafeteria.entries.size),
                        label = { Text("${cafeteria.shortLabel} · ${cafeteria.vendor}", maxLines = 1) }
                    )
                }
            }

            SectionTitle("식사 시간 알림", "평일 식사 시작 시간에 알림을 보냅니다.")
            Column(Modifier.selectableGroup()) {
                AlarmRow(
                    title = "사용 안 함",
                    selected = selectedAlarm == null,
                    onClick = { onAlarmSelected(null) },
                )
                AlarmOption.entries.forEach { option ->
                    if (option == AlarmOption.CUSTOM) {
                        AlarmRow(
                            title = option.department,
                            time = customAlarmTime?.toString(),
                            selected = selectedAlarm == option,
                            onClick = { onAlarmSelected(option) },
                            trailing = {
                                TextButton(onClick = onChangeCustomTime) {
                                    Text(if (customAlarmTime == null) "시간 선택" else "변경")
                                }
                            }
                        )
                    } else {
                        AlarmRow(
                            title = option.department.replace("\n", ", "),
                            time = AlarmTime(option.hour, option.minute).toString(),
                            selected = selectedAlarm == option,
                            onClick = { onAlarmSelected(option) },
                        )
                    }
                }
            }

            SectionTitle("2식당 운영 시간", "식당 사정에 따라 바뀔 수 있습니다.")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = LocalBrandColors.current.card),
            ) {
                Column(Modifier.padding(16.dp)) {
                    HoursRow("조식", "07:00 ~ 08:20", "Take Out 09:20 까지")
                    HoursRow("중식", "11:30 ~ 13:00", null)
                    HoursRow("석식", "17:30 ~ 19:00", "Take Out 없음")
                    Spacer(Modifier.height(12.dp))
                    Image(
                        painter = painterResource(R.drawable.menu_position),
                        contentDescription = "2식당 코너 배치도",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            SectionTitle("정보", null)
            ListItem(
                headlineContent = { Text("About & License") },
                supportingContent = { Text("프로젝트 홈페이지와 오픈소스 라이선스") },
                leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                modifier = Modifier.selectable(selected = false, onClick = onShowLicense, role = Role.Button),
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
            )
            ListItem(
                headlineContent = { Text("버전") },
                trailingContent = { Text(BuildConfig.VERSION_NAME) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String, description: String?) {
    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (description != null) {
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AlarmRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    time: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    ListItem(
        headlineContent = { Text(title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        supportingContent = time?.let { { Text(it) } },
        leadingContent = { RadioButton(selected = selected, onClick = null) },
        trailingContent = trailing,
        modifier = Modifier.selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
    )
}

@Composable
private fun HoursRow(meal: String, hours: String, note: String?) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Text("$meal  $hours", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        if (note != null) {
            Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(heightDp = 1400)
@Composable
private fun SettingsScreenPreview() {
    SecSeoulRnDMenuTheme {
        SettingsScreen(
            firstCafeteria = Cafeteria.CAFETERIA_2,
            selectedAlarm = AlarmOption.DMC,
            customAlarmTime = AlarmTime(12, 10),
            onBack = {},
            onCafeteriaSelected = {},
            onAlarmSelected = {},
            onChangeCustomTime = {},
            onShowLicense = {},
        )
    }
}

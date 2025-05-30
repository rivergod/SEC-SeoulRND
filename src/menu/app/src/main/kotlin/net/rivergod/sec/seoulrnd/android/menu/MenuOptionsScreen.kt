package net.rivergod.sec.seoulrnd.android.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Assuming R class stubs are available or will be linked in the project
// object R { object drawable { const val menu_position = 0; const val alarm_check = 0 } }
// object R { object string { const val option_text = 0; const val menu_license = 0 } } // For preview

data class AlarmOption(
    val department: String,
    val time: String,
    val originalIndex: Int // To map back to original logic if needed
)

@Composable
fun MenuOptionsScreen(
    selectedCampusId: Int, // e.g., R.id.orderCampus1 or R.id.orderCampus2
    selectedAlarmIndex: Int, // 0-4, or -1 for none
    customAlarmHour: Int, // -1 if not set
    customAlarmMinute: Int, // -1 if not set
    onCampusSelected: (campusId: Int) -> Unit,
    onAlarmOptionSelected: (index: Int) -> Unit, // index 0-3 for preset, 4 for custom
    onCustomAlarmTimeClick: () -> Unit, // To trigger time picker/dialog
    onShowLicense: () -> Unit,
    onShowDeveloperInfo: () -> Unit // Added as per general drawer requirements
) {
    val darkBackground = Color(0xFF14181B)
    val lightTextColor = Color(0xFFE5E5E5)
    val midTextColor = Color(0xFFFFFFFF)
    val dividerColor = Color(0xFF33373A)

    // Alarm options data
    val alarmOptions = listOf(
        AlarmOption("디자인경영", "11:30", 0),
        AlarmOption("DMC 연구소", "12:00", 1),
        AlarmOption("SW 센터", "12:20", 2),
        // Assuming R.string.option_text resolves to something like "기타 연구소"
        AlarmOption("기타 연구소", "12:20", 3) // Using placeholder for R.string.option_text
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(bottom = 41.dp) // Original margin
    ) {
        // This Composable represents the content part of option_menu.xml
        // The outer LinearLayout with common_menu_close and width control is part of the drawer mechanism itself.

        // Static info: 식당 운영 시간 / MAP
        Text(
            text = "식당 운영 시간 / MAP",
            color = midTextColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 5.dp)
        )
        Text(
            text = "조식 [07:00 ~ 08:20 (Take Out: 09:20)]\n중식 [11:30 ~ 13:00]\n석식 [17:30 ~ 19:00 (Take Out X)]",
            color = midTextColor,
            fontSize = 9.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, start = 10.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.menu_position), // Replace with actual resource
            contentDescription = "Menu Position Map",
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(5.dp)
        )

        ListDivider(dividerColor)

        // Section: 보여지는 순서 (Campus Selection)
        Text(
            text = "보여지는 순서",
            color = lightTextColor,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 5.dp) // IncludeFontPadding is true by default
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .border(1.dp, midTextColor, shape = RoundedCornerShape(4.dp)) // Approximating round_rect_text_gray_line
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("1캠퍼스 (A,B,C Tower)", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = midTextColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                VerticalDivider(midTextColor)
                Text("2캠퍼스 (D,E,F Tower)", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = midTextColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = midTextColor)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clickable { onCampusSelected(R.id.orderCampus1) }, contentAlignment = Alignment.Center) { // Assuming R.id values
                    if (selectedCampusId == R.id.orderCampus1) {
                        Image(painter = painterResource(id = R.drawable.alarm_check), contentDescription = "Selected", modifier = Modifier.size(15.dp))
                    }
                }
                VerticalDivider(midTextColor)
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clickable { onCampusSelected(R.id.orderCampus2) }, contentAlignment = Alignment.Center) {
                    if (selectedCampusId == R.id.orderCampus2) {
                        Image(painter = painterResource(id = R.drawable.alarm_check), contentDescription = "Selected", modifier = Modifier.size(15.dp))
                    }
                }
            }
        }

        ListDivider(dividerColor)

        // Section: Alarm Setting
        Text(
            text = "Alarm Setting",
            color = lightTextColor,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 5.dp)
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .border(1.dp, midTextColor, shape = RoundedCornerShape(4.dp)) // Approximating round_rect_text_gray_line
        ) {
            // Header Row
            Row(modifier = Modifier.fillMaxWidth().height(25.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("사업부", modifier = Modifier.width(110.dp), textAlign = TextAlign.Center, color = midTextColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                VerticalDivider(midTextColor)
                Text("식사 시작", modifier = Modifier.width(110.dp), textAlign = TextAlign.Center, color = midTextColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                VerticalDivider(midTextColor)
                Text("선택", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = midTextColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = midTextColor)

            // Alarm options
            alarmOptions.forEach { option ->
                AlarmSettingRow(
                    department = option.department,
                    time = option.time,
                    isSelected = selectedAlarmIndex == option.originalIndex,
                    onClick = { onAlarmOptionSelected(option.originalIndex) }
                )
                HorizontalDivider(color = midTextColor)
            }

            // Custom Alarm Row
            val customAlarmTimeText = if (customAlarmHour != -1 && customAlarmMinute != -1) {
                String.format("%02d:%02d", customAlarmHour, customAlarmMinute)
            } else {
                ""
            }
            AlarmSettingRow(
                department = "사용자 설정",
                time = customAlarmTimeText,
                isCustom = true,
                isSelected = selectedAlarmIndex == 4,
                onClick = {
                    if (selectedAlarmIndex == 4 && customAlarmTimeText.isNotEmpty()) {
                        // If already selected and has time, allow re-click to change time
                        onCustomAlarmTimeClick()
                    } else {
                        onAlarmOptionSelected(4) // This might then trigger onCustomAlarmTimeClick if time is empty
                    }
                },
                onTimeTextClick = onCustomAlarmTimeClick
            )
        }
        Text(
            text = "선택 하시면 식사 시작 시간에 알림이 울립니다.",
            color = midTextColor,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        )

        ListDivider(dividerColor)
        ClickableTextRow(text = "라이센스 정보", onClick = onShowLicense, textColor = lightTextColor) // Assuming R.string.menu_license
        ListDivider(dividerColor)
        ClickableTextRow(text = "개발자 정보 보기", onClick = onShowDeveloperInfo, textColor = lightTextColor) // Added this
        ListDivider(dividerColor)
    }
}

@Composable
private fun AlarmSettingRow(
    department: String,
    time: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isCustom: Boolean = false,
    onTimeTextClick: (() -> Unit)? = null // Only for custom time text click
) {
    val midTextColor = Color(0xFFFFFFFF)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (department.length > 10)IntrinsicSize.Min else 20.dp) // Crude way to handle R.string.option_text if it's long
            .clickable(enabled = !isCustom, onClick = onClick), // Row clickable only for preset alarms
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(department, modifier = Modifier.width(110.dp).padding(vertical = if (department.length > 10) 2.dp else 0.dp), textAlign = TextAlign.Center, color = midTextColor, fontSize = if (department.length > 10) 9.sp else 10.sp)
        VerticalDivider(midTextColor)
        Text(
            time,
            modifier = Modifier
                .width(110.dp)
                .then(if (isCustom && onTimeTextClick != null) Modifier.clickable(onClick = onTimeTextClick) else Modifier),
            textAlign = TextAlign.Center, color = midTextColor, fontSize = 10.sp
        )
        VerticalDivider(midTextColor)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .then(if (isCustom) Modifier.clickable(onClick = onClick) else Modifier), // Make only check area clickable for custom
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Image(painter = painterResource(id = R.drawable.alarm_check), contentDescription = "Selected", modifier = Modifier.size(15.dp))
            }
        }
    }
}

@Composable
private fun ClickableTextRow(text: String, onClick: () -> Unit, textColor: Color) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp), // Original height 36dp, adjusted padding
        textAlign = TextAlign.Center,
        color = textColor,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
    )
}

@Composable
private fun ListDivider(color: Color) {
    HorizontalDivider(thickness = 0.75.dp, color = Color.Black)
    HorizontalDivider(thickness = 0.75.dp, color = color, modifier = Modifier.padding(top = 0.75.dp))
}

@Composable
private fun VerticalDivider(color: Color) {
    Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(color))
}

// Placeholder for R.id values, actual values would be different
object R {
    object id {
        const val orderCampus1 = 1001
        const val orderCampus2 = 1002
    }
    object drawable {
        const val menu_position = android.R.drawable.ic_menu_mapmode // Placeholder
        const val alarm_check = android.R.drawable.checkbox_on_background // Placeholder
    }
    object string {
        // These would be actual string resources
        const val option_text = "기타 연구소 등등등" // Example of a potentially long string
        const val menu_license = "라이센스 정보"
    }
}

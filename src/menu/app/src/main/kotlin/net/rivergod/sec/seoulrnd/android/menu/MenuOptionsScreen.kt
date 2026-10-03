package net.rivergod.sec.seoulrnd.android.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.rivergod.sec.seoulrnd.android.menu.dto.Cafeteria
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.OptionBackground
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.OptionBoxBackground
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.OptionBoxStroke
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.OptionDividerLight
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.OptionTitleText

/** 설정 버튼으로 여는 오른쪽 패널 (0.9.14 option_menu.xml). */
@Composable
fun MenuOptionsScreen(
    firstCafeteria: Cafeteria,
    selectedAlarm: AlarmOption?,
    customAlarmTime: AlarmTime?,
    onCafeteriaSelected: (Cafeteria) -> Unit,
    onAlarmOptionSelected: (AlarmOption) -> Unit,
    onCustomAlarmTimeClick: () -> Unit,
    onShowLicense: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OptionBackground)
            .verticalScroll(rememberScrollState())
    ) {
        OptionDivider()

        Text(
            text = "식당 운영 시간 / MAP",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 5.dp),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "조식 [07:00 ~ 08:20 (Take Out: 09:20)]\n중식 [11:30 ~ 13:00]\n석식 [17:30 ~ 19:00 (Take Out X)]",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, start = 10.dp),
            color = Color.White,
            fontSize = 9.sp,
            lineHeight = 12.sp
        )
        Image(
            painter = painterResource(R.drawable.menu_position),
            contentDescription = "식당 위치",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(start = 5.dp, top = 5.dp, end = 5.dp, bottom = 1.dp)
        )

        OptionDivider()

        SectionTitle("보여지는 순서")
        OptionBox {
            TableRow(height = 20.dp) {
                Cafeteria.entries.forEachIndexed { index, cafeteria ->
                    if (index > 0) CellDivider()
                    Cell(Modifier.weight(1f)) { CellText(cafeteria.label, fontSize = 12.sp, bold = true) }
                }
            }
            RowDivider()
            TableRow(height = 20.dp) {
                Cafeteria.entries.forEachIndexed { index, cafeteria ->
                    if (index > 0) CellDivider()
                    Cell(
                        Modifier
                            .weight(1f)
                            .clickable { onCafeteriaSelected(cafeteria) }
                    ) { CheckMark(cafeteria == firstCafeteria) }
                }
            }
        }

        OptionDivider()

        SectionTitle("Alarm Setting")
        OptionBox {
            TableRow(height = 25.dp) {
                Cell(Modifier.width(110.dp)) { CellText("사업부", fontSize = 12.sp, bold = true) }
                CellDivider()
                Cell(Modifier.width(110.dp)) { CellText("식사 시작", fontSize = 12.sp, bold = true) }
                CellDivider()
                Cell(Modifier.weight(1f)) { CellText("선택", fontSize = 12.sp, bold = true) }
            }
            AlarmOption.entries.forEach { option ->
                RowDivider()
                val isCustom = option == AlarmOption.CUSTOM
                val isMultiLine = option.department.contains('\n')
                TableRow(height = if (isMultiLine) null else 20.dp) {
                    Cell(
                        Modifier
                            .width(110.dp)
                            .then(if (isMultiLine) Modifier.padding(vertical = 5.dp) else Modifier)
                    ) {
                        CellText(option.department, fontSize = if (isMultiLine) 9.sp else 10.sp)
                    }
                    CellDivider()
                    Cell(
                        Modifier
                            .width(110.dp)
                            .then(if (isCustom) Modifier.clickable(onClick = onCustomAlarmTimeClick) else Modifier)
                    ) {
                        val time = if (isCustom) customAlarmTime?.toString() ?: "시간 입력" else AlarmTime(option.hour, option.minute).toString()
                        CellText(time, fontSize = 10.sp)
                    }
                    CellDivider()
                    Cell(
                        Modifier
                            .weight(1f)
                            .clickable { onAlarmOptionSelected(option) }
                    ) { CheckMark(option == selectedAlarm) }
                }
            }
        }
        Text(
            text = "선택 하시면 식사 시작 시간에 알림이 울립니다.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            color = Color.White,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )

        OptionDivider()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clickable(onClick = onShowLicense),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "About & License",
                color = OptionTitleText,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        }
        OptionDivider()
    }
}

@Composable
private fun ColumnScope.SectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(vertical = 2.dp),
        color = OptionTitleText,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp
    )
}

@Composable
private fun OptionBox(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(2.5.dp)
    Column(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp, bottom = 5.dp)
            .fillMaxWidth()
            .background(OptionBoxBackground, shape)
            .border(1.dp, OptionBoxStroke, shape),
        content = content
    )
}

/** height 가 null 이면 내용 높이에 맞춘다. 셀 구분선이 행 높이를 채우도록 IntrinsicSize 를 쓴다. */
@Composable
private fun TableRow(height: Dp?, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (height != null) Modifier.height(height) else Modifier.height(IntrinsicSize.Min)),
        content = content
    )
}

@Composable
private fun Cell(modifier: Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxHeight(), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun CellText(text: String, fontSize: TextUnit, bold: Boolean = false) {
    Text(
        text = text,
        color = Color.White,
        fontSize = fontSize,
        lineHeight = fontSize * 1.25f,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun CheckMark(checked: Boolean) {
    if (checked) {
        Image(
            painter = painterResource(R.drawable.alarm_check),
            contentDescription = "선택됨",
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun CellDivider() {
    Box(
        Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(Color.White)
    )
}

@Composable
private fun RowDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White)
    )
}

@Composable
private fun OptionDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(0.75.dp)
            .background(Color.Black)
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(0.75.dp)
            .background(OptionDividerLight)
    )
}

@Preview(widthDp = 300, heightDp = 640)
@Composable
private fun MenuOptionsScreenPreview() {
    MenuOptionsScreen(
        firstCafeteria = Cafeteria.CAFETERIA_2,
        selectedAlarm = AlarmOption.DMC,
        customAlarmTime = AlarmTime(12, 10),
        onCafeteriaSelected = {},
        onAlarmOptionSelected = {},
        onCustomAlarmTimeClick = {},
        onShowLicense = {},
    )
}

package net.rivergod.sec.seoulrnd.android.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.MenuCalorieText
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.OptionBoxStroke
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.PopupBackground
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.PopupDarkButton
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.PopupTitleText

/** 0.9.14 custom_popup.xml / license_dialog.xml 공통 틀: 250dp 흰 박스 + 큰 제목 + 2줄 구분선. */
@Composable
fun PopupFrame(title: String, onDismissRequest: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        val shape = RoundedCornerShape(15.dp)
        Column(
            modifier = Modifier
                .width(250.dp)
                .background(PopupBackground, shape)
                .border(1.dp, OptionBoxStroke, shape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = title, color = PopupTitleText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(PopupTitleText)
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color.Black)
            )
            Column(modifier = Modifier.padding(10.dp), content = content)
        }
    }
}

@Composable
fun PopupButton(text: String, dark: Boolean = false, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .size(width = 100.dp, height = 30.dp)
            .then(
                if (dark) Modifier.background(PopupDarkButton, shape).border(1.dp, Color.White, shape)
                else Modifier.background(Color.White, shape).border(1.dp, OptionBoxStroke, shape)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = if (dark) Color.White else Color.Black)
    }
}

/**
 * 알람 '사용자 설정' 시각 입력 팝업.
 * @param onSetTime 입력값을 받아 올바르면 true 를 돌려준다. false 면 안내 문구를 보이고 팝업을 유지한다.
 */
@Composable
fun AlarmTimeSetPopup(
    initialTime: AlarmTime?,
    onSetTime: (String) -> Boolean,
    onCancel: () -> Unit,
) {
    val initialText = initialTime?.toString()?.replace(":", "").orEmpty()
    var input by remember { mutableStateOf(TextFieldValue(initialText, TextRange(initialText.length))) }
    var showError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val submit = { showError = !onSetTime(input.text) }

    PopupFrame(title = "Alarm Time Set", onDismissRequest = onCancel) {
        OutlinedTextField(
            value = input,
            onValueChange = { value ->
                input = value.copy(text = value.text.filter { it.isDigit() }.take(4))
                showError = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .focusRequester(focusRequester),
            placeholder = {
                Text(" 시간 분을 4자리로 표시", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 13.sp)
            },
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 15.sp, color = Color.Black),
            singleLine = true,
            isError = showError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
        )
        if (showError) {
            Text(
                text = "예) 1230 → 12:30 (00:00 ~ 23:59)",
                modifier = Modifier.fillMaxWidth(),
                color = MenuCalorieText,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PopupButton("Cancel", dark = true, onClick = onCancel)
            PopupButton("OK", onClick = submit)
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

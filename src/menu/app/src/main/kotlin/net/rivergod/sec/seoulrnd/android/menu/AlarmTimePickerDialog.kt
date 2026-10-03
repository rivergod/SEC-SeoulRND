package net.rivergod.sec.seoulrnd.android.menu

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable

/** 알람 '사용자 설정' 시각 선택. (0.9.14 는 시·분 4자리를 직접 입력했다) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmTimePickerDialog(
    initialTime: AlarmTime?,
    onConfirm: (AlarmTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialTime?.hour ?: 12,
        initialMinute = initialTime?.minute ?: 0,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("알림 시간") },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(AlarmTime(state.hour, state.minute)) }) { Text("확인") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
    )
}

package net.rivergod.sec.seoulrnd.android.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun CustomPopupComposable(
    showDialog: Boolean,
    onDismissRequest: () -> Unit, // Generic dismiss, can be tied to cancel or specific actions
    title: String?,
    message: String? = null,
    isTimeSetMode: Boolean = false,
    initialTime: String = "",
    onMessageOk: (() -> Unit)? = null,
    onSetTime: ((String) -> Unit)? = null,
    onSetTimeCancel: (() -> Unit)? = null
) {
    if (showDialog) {
        var timeInput by remember(initialTime) { mutableStateOf(initialTime) }

        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000)), // Semi-transparent background
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(250.dp)
                        .background(Color.White, shape = RoundedCornerShape(8.dp)) // Approximating round_rect_white_box
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!title.isNullOrEmpty()) {
                        Text(
                            text = title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp), // Original height 50dp
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 25.sp,
                            color = Color(0xFF666666)
                        )
                    }

                    Divider(color = Color(0xFF666666), thickness = 0.5.dp)
                    Divider(color = Color.Black, thickness = 0.5.dp, modifier = Modifier.padding(top = 0.5.dp))

                    Box(modifier = Modifier.padding(all = 10.dp)) {
                        if (isTimeSetMode) {
                            // Time Set Mode UI
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                OutlinedTextField( // Changed to OutlinedTextField for better styling
                                    value = timeInput,
                                    onValueChange = { timeInput = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp), // Original margin 10dp
                                    label = { Text("시간 분을 4자리로 표시") }, // Hint
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 15.sp, color = Color.Black),
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Cancel Button
                                    Text(
                                        text = "Cancel",
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(30.dp)
                                            .background(Color.DarkGray, shape = RoundedCornerShape(4.dp)) // Approximating round_rect_blick_line
                                            .clickable { onSetTimeCancel?.invoke(); onDismissRequest() }
                                            .padding(vertical = 6.dp),
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    // OK Button
                                    Text(
                                        text = "OK",
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(30.dp)
                                            .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp)) // Approximating round_rect_line
                                            .clickable { onSetTime?.invoke(timeInput); onDismissRequest() }
                                            .padding(vertical = 6.dp),
                                        color = Color.Black,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // Message Mode UI
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (message != null) {
                                    Text(
                                        text = message,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 350.dp)
                                            .verticalScroll(rememberScrollState()),
                                        color = Color.Black,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "OK",
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(30.dp)
                                        .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp)) // Approximating round_rect_line
                                        .clickable { onMessageOk?.invoke(); onDismissRequest() }
                                        .padding(vertical = 6.dp),
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Stubs for drawables if needed for previews, assuming simple shapes
// object R { object drawable { const val round_rect_white_box = 0; const val round_rect_line = 0; const val round_rect_blick_line = 0}}

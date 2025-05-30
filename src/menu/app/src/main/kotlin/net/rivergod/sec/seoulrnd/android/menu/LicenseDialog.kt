package net.rivergod.sec.seoulrnd.android.menu

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

private const val TAG = "LicenseDialogComposable"

@Composable
fun LicenseDialogComposable(
    showDialog: Boolean,
    onDismissRequest: () -> Unit
) {
    if (showDialog) {
        val context = LocalContext.current
        val licenseString = remember { loadLicenseText(context) }

        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(usePlatformDefaultWidth = false) // Allows custom width
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
                        .background(Color.White, shape = RoundedCornerShape(8.dp)) // Assuming round_rect_white_box is a rounded rect
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "About & License",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp), // Original height 50dp, adjusted padding
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        color = Color(0xFF666666)
                    )

                    Divider(color = Color(0xFF666666), thickness = 0.5.dp)
                    Divider(color = Color.Black, thickness = 0.5.dp, modifier = Modifier.padding(top = 0.5.dp))


                    Column(
                        modifier = Modifier
                            .padding(all = 10.dp)
                    ) {
                        Text(
                            text = "이 프로젝트는 다음 홈페이지에서 관리되고 있습니다.\nUI/UX, 런처아이콘, 개발, 아이디어 도움을 주실 분은 Issues를 통해 참여 하실 수 있습니다.",
                            color = Color.Black,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/rivergod/SEC-SeoulRND"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)) // Default button color, adjust as needed
                        ) {
                            Text("바로가기", color = Color.Black)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = licenseString,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 250.dp) // maxHeight="250dp"
                                .verticalScroll(rememberScrollState())
                                .border(0.5.dp, Color.Gray), // Added a light border for scroll area visibility
                            color = Color.Black,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "OK",
                            modifier = Modifier
                                .width(100.dp)
                                .align(Alignment.CenterHorizontally)
                                .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp)) // Assuming round_rect_line
                                .padding(vertical = 6.dp) // Original height 30dp
                                .clickable { onDismissRequest() },
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private fun loadLicenseText(context: Context): String {
    return try {
        val inputStream: InputStream = context.resources.openRawResource(R.raw.txt_license)
        val byteArrayOutputStream = ByteArrayOutputStream()
        var i: Int = inputStream.read()
        while (i != -1) {
            byteArrayOutputStream.write(i)
            i = inputStream.read()
        }
        inputStream.close()
        byteArrayOutputStream.toString("UTF-8") // Assuming UTF-8, adjust if different
    } catch (e: IOException) {
        Log.e(TAG, "Cannot open license resource.", e)
        "Error loading license."
    }
}

// Helper R class stub if not available in current context (e.g. pure Kotlin module for preview)
// In a real Android app, this R class would be generated.
object R {
    object raw {
        const val txt_license = 0 // Replace with actual resource ID if testing in isolation
    }
    object drawable {
        // These would be actual drawable resources
        // const val round_rect_white_box = 0 
        // const val round_rect_line = 0
    }
}

package net.rivergod.sec.seoulrnd.android.menu

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import java.io.IOException

private const val TAG = "LicenseDialog"
private const val PROJECT_URL = "https://github.com/rivergod/SEC-SeoulRND"

@Composable
fun LicenseDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val licenseText = remember { loadLicenseText(context) }

    PopupFrame(title = "About & License", onDismissRequest = onDismissRequest) {
        Text(
            text = "이 프로젝트는 다음 홈페이지에서 관리되고 있습니다.\nUI/UX, 런처아이콘, 개발, 아이디어 도움을 주실 분은 Issues를 통해 참여 하실 수 있습니다.",
            color = Color.Black,
            fontSize = 12.sp
        )
        Button(
            onClick = {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, PROJECT_URL.toUri()))
                } catch (e: ActivityNotFoundException) {
                    Log.w(TAG, "No browser to open $PROJECT_URL", e)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("바로가기")
        }
        Text(
            text = licenseText,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 250.dp)
                .verticalScroll(rememberScrollState()),
            color = Color.Black,
            fontSize = 10.sp
        )
        Spacer(Modifier.height(10.dp))
        Box(Modifier.align(Alignment.CenterHorizontally)) {
            PopupButton("OK", onClick = onDismissRequest)
        }
    }
}

private fun loadLicenseText(context: Context): String = try {
    context.resources.openRawResource(R.raw.txt_license).use { it.readBytes().decodeToString() }
} catch (e: IOException) {
    Log.d(TAG, "Cannot open license resource.", e)
    ""
}

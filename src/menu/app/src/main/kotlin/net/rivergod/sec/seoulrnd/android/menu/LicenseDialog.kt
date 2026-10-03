package net.rivergod.sec.seoulrnd.android.menu

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import java.io.IOException

private const val TAG = "LicenseDialog"
private const val PROJECT_URL = "https://github.com/rivergod/SEC-SeoulRND"

@Composable
fun LicenseDialog(onDismissRequest: () -> Unit, onOpenProjectPage: () -> Unit = {}) {
    val context = LocalContext.current
    val licenseText = remember { loadLicenseText(context) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("About & License") },
        text = {
            Column {
                Text(
                    "이 프로젝트는 다음 홈페이지에서 관리되고 있습니다.\n" +
                        "UI/UX, 런처아이콘, 개발, 아이디어 도움을 주실 분은 Issues를 통해 참여 하실 수 있습니다.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        onOpenProjectPage()
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, PROJECT_URL.toUri()))
                        } catch (e: ActivityNotFoundException) {
                            Log.w(TAG, "No browser to open $PROJECT_URL", e)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("GitHub 바로가기")
                }
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = licenseText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text("확인") }
        },
    )
}

private fun loadLicenseText(context: Context): String = try {
    context.resources.openRawResource(R.raw.txt_license).use { it.readBytes().decodeToString() }
} catch (e: IOException) {
    Log.d(TAG, "Cannot open license resource.", e)
    ""
}

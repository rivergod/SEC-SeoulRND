package net.rivergod.sec.seoulrnd.android.menu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 0.9.14 는 고정된 밝은 색 디자인만 있었으므로 다크/다이나믹 컬러를 쓰지 않는다.
private val LightColorScheme = lightColorScheme(
    primary = HeaderSubText,
    onPrimary = Color.White,
    secondary = TabText,
    background = Color.White,
    surface = Color.White,
)

@Composable
fun SecSeoulRnDMenuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

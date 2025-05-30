package net.rivergod.sec.seoulrnd.android.menu

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RestaurantMenu 
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.res.stringResource // Will be used if R object is fully integrated
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Assuming R class stubs for string resources are available
// object R { object string { const val action_settings = 0; const val app_name = 0 } }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Assuming SecSeoulRnDMenuTheme is defined elsewhere and wraps this
            // If not, a default MaterialTheme can be used.
            // For now, let's assume it exists as per the original overwritten file.
            net.rivergod.sec.seoulrnd.android.menu.ui.theme.SecSeoulRnDMenuTheme {
                 MainAppContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent() {
    val context = LocalContext.current
    var showOptionsMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("서울 R&D 식단") }, // Or stringResource(R.string.app_name)
                actions = {
                    IconButton(onClick = { showOptionsMenu = !showOptionsMenu }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings") }, // stringResource(R.string.action_settings)
                            onClick = {
                                // TODO: Handle settings click, e.g., open a settings screen or navigate
                                // For example, could show a Toast or log for now
                                android.widget.Toast.makeText(context, "Settings Clicked", android.widget.Toast.LENGTH_SHORT).show()
                                showOptionsMenu = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                context.startActivity(Intent(context, MenuActivity::class.java))
            }) {
                Icon(Icons.Filled.RestaurantMenu, contentDescription = "View Menu")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp), // Standard padding for content
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "서울 R&D 캠퍼스 주간식단",
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Button(onClick = {
                context.startActivity(Intent(context, MenuActivity::class.java))
            }) {
                Text("메뉴보기")
            }
        }
    }
}

// Placeholder for R.string.action_settings and R.string.app_name for compilation
// In a real app, these would be in strings.xml
// This object should ideally be removed if the real R class is generated and accessible.
object R {
    object string {
        const val action_settings = "Settings" // Example placeholder
        // const val app_name = "서울 R&D 식단" // Example placeholder, title is hardcoded above
    }
     object dimen { // Required by activity_main.xml's FAB margin, not directly by Compose code here but good to keep for context
        val fab_margin = 16.dp
    }
}

// Assuming SecSeoulRnDMenuTheme is defined, for example:
// package net.rivergod.sec.seoulrnd.android.menu.ui.theme
// @Composable
// fun SecSeoulRnDMenuTheme(content: @Composable () -> Unit) {
//     MaterialTheme( // or a custom theme
//         colorScheme = lightColorScheme(), // or darkColorScheme()
//         typography = Typography(),
//         content = content
//     )
// }

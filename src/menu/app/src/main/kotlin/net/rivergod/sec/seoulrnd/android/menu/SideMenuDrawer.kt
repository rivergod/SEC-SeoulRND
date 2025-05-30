package net.rivergod.sec.seoulrnd.android.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.HorizontalDivider

@Composable
fun AppDrawerContent(
    onCampusSettingsClick: () -> Unit,
    onAlarmSettingsClick: () -> Unit,
    onLicenseClick: () -> Unit,
    onDeveloperInfoClick: () -> Unit,
    onCloseDrawer: () -> Unit // Callback to close the drawer
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Menu",
            fontSize = 24.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        DrawerMenuItem(
            text = "Campus Setting",
            onItemClick = {
                onCampusSettingsClick()
                onCloseDrawer()
            }
        )
        DrawerMenuItem(
            text = "Alarm Setting",
            onItemClick = {
                onAlarmSettingsClick()
                onCloseDrawer()
            }
        )
        DrawerMenuItem(
            text = "License",
            onItemClick = {
                onLicenseClick()
                onCloseDrawer()
            }
        )
        DrawerMenuItem(
            text = "Developer Info",
            onItemClick = {
                onDeveloperInfoClick()
                onCloseDrawer()
            }
        )
        // Add other menu items if needed
    }
}

@Composable
private fun DrawerMenuItem(
    text: String,
    onItemClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onItemClick)) {
        Text(
            text = text,
            fontSize = 18.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
        HorizontalDivider()
    }
}

// Example usage (typically in your main screen Composable with ModalNavigationDrawer)
/*
@Composable
fun MainScreenWithDrawer() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet { // Recommended for Material 3
                AppDrawerContent(
                    onCampusSettingsClick = { /* Handle click */ },
                    onAlarmSettingsClick = { /* Handle click */ },
                    onLicenseClick = { /* Handle click */ },
                    onDeveloperInfoClick = { /* Handle click */ },
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        // Main screen content
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("App Name") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open Drawer")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                Text("Main Content Area")
            }
        }
    }
}
*/

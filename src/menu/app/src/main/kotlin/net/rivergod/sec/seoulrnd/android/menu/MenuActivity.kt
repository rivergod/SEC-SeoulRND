package net.rivergod.sec.seoulrnd.android.menu

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO // Assuming this DTO is available
import java.text.SimpleDateFormat
import java.util.*

// Assuming R class stubs are available or will be linked in the project for drawables and strings
// object R { object drawable { const val icon_setting = android.R.drawable.ic_menu_manage } }
// object R { object string { const val menu_sub_title = "Seoul R&D Campus"; const val menu_title = "Today's Menu" } }
// object R { object id { const val orderCampus1 = 1; const val orderCampus2 = 2; } } // For MenuOptionsScreen

class MenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MenuAppScreen()
        }
    }
}

@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuAppScreen() {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // --- State Management (Simplified for now) ---
    // SharedPreferences loading would happen here or in a ViewModel
    var selectedCampusIdState by remember { mutableStateOf(R.id.orderCampus2) } // Default, load from prefs
    var selectedAlarmIndexState by remember { mutableStateOf(-1) } // Default, load from prefs
    var customAlarmHourState by remember { mutableStateOf(-1) }
    var customAlarmMinuteState by remember { mutableStateOf(-1) }

    // Menu data state
    val menuItemsList = remember { mutableStateListOf<Any>() } // Will hold CuisineDTO or Header strings
    var currentMealType by remember { mutableStateOf(MealType.LUNCH) } // Default to Lunch

    // Dialog states
    var showLicenseDialog by remember { mutableStateOf(false) }
    // var showDeveloperInfoDialog by remember { mutableStateOf(false) } // If using CustomPopup for this
    var showCustomTimeDialog by remember { mutableStateOf(false) }


    // --- Data Fetching (Placeholder) ---
    LaunchedEffect(key1 = currentMealType, key2 = selectedCampusIdState) {
        // TODO: Implement actual data fetching logic based on currentMealType and selectedCampusIdState
        // e.g., call a suspend function that fetches and parses menu data
        // For now, populate with dummy data or clear
        menuItemsList.clear()
        // menuItemsList.addAll(fetchMenuData(currentMealType, selectedCampusIdState))
    }

    // --- UI ---
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet { // Material 3 recommendation
                MenuOptionsScreen(
                    selectedCampusId = selectedCampusIdState,
                    selectedAlarmIndex = selectedAlarmIndexState,
                    customAlarmHour = customAlarmHourState,
                    customAlarmMinute = customAlarmMinuteState,
                    onCampusSelected = { campusId ->
                        selectedCampusIdState = campusId
                        // TODO: Save to SharedPreferences
                        // TODO: Trigger menu data refresh
                        scope.launch { drawerState.close() }
                    },
                    onAlarmOptionSelected = { index ->
                        selectedAlarmIndexState = index
                        // TODO: Save to SharedPreferences & update alarm via RegisterAlarm
                        if (index == 4 && (customAlarmHourState == -1 || customAlarmMinuteState == -1)) {
                           showCustomTimeDialog = true // Open time picker if custom is selected and no time set
                        }
                         scope.launch { drawerState.close() }
                    },
                    onCustomAlarmTimeClick = {
                        showCustomTimeDialog = true
                        // Actual time picking dialog will provide data back via another callback
                        // scope.launch { drawerState.close() } // Keep drawer open for time picker
                    },
                    onShowLicense = {
                        showLicenseDialog = true
                        scope.launch { drawerState.close() }
                    },
                    onShowDeveloperInfo = {
                        // TODO: showDeveloperInfoDialog = true (if using CustomPopup)
                        // Or navigate to a different composable/screen
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                MenuTopAppBar(
                    onNavigationIconClick = { scope.launch { drawerState.open() } },
                    onSyncClick = {
                        // TODO: Trigger menu data refresh for current meal type and campus
                    }
                )
            },
            bottomBar = {
                BottomMenuBar(
                    selectedMealType = currentMealType,
                    onMealTypeSelected = { mealType -> currentMealType = mealType },
                    onSettingsClick = { scope.launch { drawerState.open() } }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply padding from Scaffold
            ) {
                // TODO: CampusMenuBar Composable (from previous step)
                // CampusMenuBar(campuses = listOf("Campus 1", "Campus 2"), selectedCampus = "Campus 1", onCampusSelected = {})

                // TODO: LazyColumn for menu items
                if (menuItemsList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Loading menu or no items...")
                    }
                } else {
                    // LazyColumn { items(menuItemsList) { item -> /* MenuRowItem or MenuHeaderItem */ } }
                }
            }
        }
    }

    if (showLicenseDialog) {
        LicenseDialogComposable(showDialog = true, onDismissRequest = { showLicenseDialog = false })
    }
    
    if (showCustomTimeDialog) {
        // Placeholder for custom time picker dialog
        // This would typically be a more complex dialog or a new screen
        AlertDialog(
            onDismissRequest = { showCustomTimeDialog = false },
            title = { Text("Set Custom Alarm Time") },
            text = { Text("Time picker would be shown here.") },
            confirmButton = {
                TextButton(onClick = {
                    // In a real scenario, you'd get time from picker
                    // customAlarmHourState = ... 
                    // customAlarmMinuteState = ...
                    // TODO: Save to SharedPreferences & update alarm via RegisterAlarm
                    selectedAlarmIndexState = 4 // Ensure custom is marked as selected
                    showCustomTimeDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTimeDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuTopAppBar(
    onNavigationIconClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val dayOfWeekStr = SimpleDateFormat("E", Locale.getDefault()).format(calendar.time)
    val dateText = "${month}월 ${day}일 (${dayOfWeekStr})"

    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { // Fill and center title
                 Column(horizontalAlignment = Alignment.CenterHorizontally){
                    Text(
                        text = "Today's Menu", // stringResource(id = R.string.menu_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black //Color(0xFF000000)
                    )
                    Text(
                        text = "Seoul R&D Campus", // stringResource(id = R.string.menu_sub_title),
                        fontSize = 11.sp,
                        color = Color(0xFF0073A3) // Color(0xFF0073A3)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(Icons.Filled.Menu, contentDescription = "Open Menu")
            }
        },
        actions = {
            Text(
                text = dateText,
                fontSize = 12.sp,
                color = Color(0xFF0073A3), //Color(0xFF0073A3)
                modifier = Modifier.align(Alignment.CenterVertically).padding(end = 8.dp)
            )
            IconButton(onClick = onSyncClick) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh Menu")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFA6E5FF)) // #a6e5ff
    )
}

enum class MealType { BREAKFAST, LUNCH, DINNER }

@Composable
fun BottomMenuBar(
    selectedMealType: MealType,
    onMealTypeSelected: (MealType) -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = Color(0xFFA6E5FF), thickness = 1.dp) // #a6e5ff
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color(0x99D9DADC)), // #99d9dadc (semi-transparent gray)
            verticalAlignment = Alignment.CenterVertically
        ) {
            MealTab(
                text = "Breakfast",
                isSelected = selectedMealType == MealType.BREAKFAST,
                onClick = { onMealTypeSelected(MealType.BREAKFAST) },
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(color = Color(0xFFA6E5FF), thickness = 1.dp) // #a6e5ff
            MealTab(
                text = "Lunch",
                isSelected = selectedMealType == MealType.LUNCH,
                onClick = { onMealTypeSelected(MealType.LUNCH) },
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(color = Color(0xFFA6E5FF), thickness = 1.dp)
            MealTab(
                text = "Dinner",
                isSelected = selectedMealType == MealType.DINNER,
                onClick = { onMealTypeSelected(MealType.DINNER) },
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(color = Color(0xFFA6E5FF), thickness = 1.dp)

            Image(
                painter = painterResource(id = R.drawable.icon_setting), // Replace with actual resource
                contentDescription = "Settings",
                modifier = Modifier
                    .size(40.dp) // container size
                    .padding(5.dp) // icon padding
                    .clickable(onClick = onSettingsClick)
            )
        }
    }
}

@Composable
fun MealTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(if (isSelected) Color(0xFFA6E5FF) else Color.Transparent) // #a6e5ff
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color(0xFF022A65), // #022a65
            fontWeight = FontWeight.Bold
        )
    }
}

// Minimal R class stub for compilation
object R {
    object drawable {
        const val icon_setting = android.R.drawable.ic_menu_manage // Example placeholder
    }
    object id { // Used by MenuOptionsScreen for campus IDs
        const val orderCampus1 = 1 
        const val orderCampus2 = 2
    }
    // string resources would go here if needed by MenuTopAppBar directly
}

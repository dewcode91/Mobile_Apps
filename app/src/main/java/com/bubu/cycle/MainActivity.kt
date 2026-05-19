package com.bubu.cycle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bubu.cycle.notifications.ReminderScheduler
import com.bubu.cycle.ui.screens.CalendarScreen
import com.bubu.cycle.ui.screens.DashboardScreen
import com.bubu.cycle.ui.screens.LogPeriodScreen
import com.bubu.cycle.ui.screens.SettingsScreen
import com.bubu.cycle.ui.theme.CycleTheme

private data class NavItem(val label: String, val icon: ImageVector)

private val NAV_ITEMS = listOf(
    NavItem("Dashboard", Icons.Filled.Home),
    NavItem("Calendar", Icons.Filled.DateRange),
    NavItem("Log", Icons.Filled.Add),
    NavItem("Settings", Icons.Filled.Settings)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReminderScheduler.ensureDailyReminder(this)
        setContent {
            CycleTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    CycleApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CycleApp() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Cycle Tracker") })
        },
        bottomBar = {
            NavigationBar {
                NAV_ITEMS.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(item.label) },
                        icon = { Icon(imageVector = item.icon, contentDescription = item.label) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen()
                1 -> CalendarScreen()
                2 -> LogPeriodScreen()
                3 -> SettingsScreen()
            }
        }
    }
}
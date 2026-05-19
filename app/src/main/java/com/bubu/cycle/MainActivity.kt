package com.bubu.cycle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bubu.cycle.notifications.ReminderScheduler
import com.bubu.cycle.ui.screens.CalendarScreen
import com.bubu.cycle.ui.screens.DashboardScreen
import com.bubu.cycle.ui.screens.LogPeriodScreen
import com.bubu.cycle.ui.screens.SettingsScreen
import com.bubu.cycle.ui.theme.CycleTheme

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
    val tabs = listOf("Dashboard", "Calendar", "Log", "Settings")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Cycle Tracker") })
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(label) },
                        icon = { }
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
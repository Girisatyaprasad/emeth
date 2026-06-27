package com.emeth.kernel.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.emeth.kernel.planner.Planner
import com.emeth.kernel.ui.theme.EmethTheme
import com.emeth.kernel.ui.screens.HomeScreen
import com.emeth.kernel.ui.screens.ActivityScreen
import com.emeth.kernel.ui.screens.PermissionScreen

enum class Screen {
    HOME, ACTIVITY, PERMISSIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmethScreen(planner: Planner) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    val haptics = LocalHapticFeedback.current

    EmethTheme {
        Scaffold(
            bottomBar = {
                Surface(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                    shadowElevation = 12.dp
                ) {
                NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.HOME,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentScreen = Screen.HOME
                        },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        alwaysShowLabel = false
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.ACTIVITY,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentScreen = Screen.ACTIVITY
                        },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Automations") },
                        alwaysShowLabel = false
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.PERMISSIONS,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentScreen = Screen.PERMISSIONS
                        },
                        icon = { Icon(Icons.Filled.Lock, contentDescription = "Access") },
                        alwaysShowLabel = false
                    )
                }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (currentScreen) {
                    Screen.HOME -> HomeScreen(planner)
                    Screen.ACTIVITY -> ActivityScreen()
                    Screen.PERMISSIONS -> PermissionScreen()
                }
            }
        }
    }
}

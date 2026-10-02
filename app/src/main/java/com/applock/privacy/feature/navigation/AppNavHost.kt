package com.applock.privacy.feature.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.applock.privacy.feature.home.HomeScreen
import com.applock.privacy.feature.settings.SettingsScreen
import com.applock.privacy.feature.themes.ThemesScreen
import com.applock.privacy.feature.tools.ToolsScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    Scaffold(
        containerColor = com.applock.privacy.core.ui.theme.BackgroundDeep,
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.Tools.route) {
                ToolsScreen()
            }
            composable(Screen.Themes.route) {
                ThemesScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

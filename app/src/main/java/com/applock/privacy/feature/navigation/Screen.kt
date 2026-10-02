package com.applock.privacy.feature.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.applock.privacy.core.common.AppConstants

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : Screen(
        route = AppConstants.ROUTE_HOME,
        title = "Home",
        icon = Icons.Default.Home
    )

    data object Tools : Screen(
        route = AppConstants.ROUTE_TOOLS,
        title = "Tools",
        icon = Icons.Default.Security
    )

    data object Themes : Screen(
        route = AppConstants.ROUTE_THEMES,
        title = "Themes",
        icon = Icons.Default.Palette
    )

    data object Settings : Screen(
        route = AppConstants.ROUTE_SETTINGS,
        title = "Settings",
        icon = Icons.Default.Settings
    )
}

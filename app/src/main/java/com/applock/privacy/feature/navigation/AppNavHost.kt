package com.applock.privacy.feature.navigation

import androidx.compose.animation.AnimatedContentTransitionScope

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.applock.privacy.core.common.AppConstants
import com.applock.privacy.data.local.AppPreferencesDataSource
import com.applock.privacy.feature.disguise.DisguiseCoverScreen
import com.applock.privacy.feature.home.HomeScreen
import com.applock.privacy.feature.onboarding.OnboardingCompleteScreen
import com.applock.privacy.feature.onboarding.OnboardingReasonScreen
import com.applock.privacy.feature.onboarding.SplashScreen
import com.applock.privacy.feature.permissions.PermissionScreen
import com.applock.privacy.feature.pro.ProSubscriptionScreen
import com.applock.privacy.feature.security.UninstallProtectionScreen
import com.applock.privacy.feature.settings.SettingsScreen
import com.applock.privacy.feature.themes.ThemesScreen
import com.applock.privacy.feature.tools.ToolsScreen
import com.applock.privacy.feature.vault.MediaVaultScreen


@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val preferencesDataSource = remember { AppPreferencesDataSource(context) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainBottomNavRoutes = listOf(
        Screen.Home.route,
        Screen.Tools.route,
        Screen.Themes.route,
        Screen.Settings.route
    )

    Scaffold(
        containerColor = com.applock.privacy.core.ui.theme.BackgroundDeep,
        bottomBar = {
            if (currentRoute in mainBottomNavRoutes) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppConstants.ROUTE_SPLASH,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(250)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(250)) },
            exitTransition = { fadeOut(tween(250)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(250)) },
            popEnterTransition = { fadeIn(tween(250)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(250)) },
            popExitTransition = { fadeOut(tween(250)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(250)) }
        ) {
            // Splash & First-Launch Onboarding Routes
            composable(AppConstants.ROUTE_SPLASH) {
                SplashScreen(
                    preferencesDataSource = preferencesDataSource,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(AppConstants.ROUTE_SPLASH) { inclusive = true }

                        }
                    },
                    onNavigateToOnboarding = {
                        navController.navigate(AppConstants.ROUTE_ONBOARDING_REASON) {
                            popUpTo(AppConstants.ROUTE_SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(AppConstants.ROUTE_ONBOARDING_REASON) {
                OnboardingReasonScreen(
                    preferencesDataSource = preferencesDataSource,
                    onNavigateNext = {
                        navController.navigate(AppConstants.ROUTE_ONBOARDING_COMPLETE)
                    }
                )
            }

            composable(AppConstants.ROUTE_ONBOARDING_COMPLETE) {
                OnboardingCompleteScreen(
                    preferencesDataSource = preferencesDataSource,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(AppConstants.ROUTE_ONBOARDING_REASON) { inclusive = true }
                        }
                    }
                )
            }

            // Permissions Flow
            composable(AppConstants.ROUTE_PERMISSIONS) {
                PermissionScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Intruder Logs Screen
            composable(AppConstants.ROUTE_INTRUDER_LOGS) {
                com.applock.privacy.feature.intruder.IntruderLogsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Notification Shield Screen (Phase 18)
            composable(AppConstants.ROUTE_NOTIFICATION_SHIELD) {
                com.applock.privacy.feature.notification.NotificationShieldScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Website Blocker Screen (Phase 19)
            composable(AppConstants.ROUTE_WEBSITE_BLOCKER) {
                com.applock.privacy.feature.blocker.WebsiteBlockerScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Private Browser Screen (Phase 20)
            composable(AppConstants.ROUTE_PRIVATE_BROWSER) {
                com.applock.privacy.feature.browser.PrivateBrowserScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Disguise Cover Screen (Phase 21)
            composable(AppConstants.ROUTE_DISGUISE_COVER) {
                DisguiseCoverScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Pro Subscription Screen (Phase 22)
            composable(AppConstants.ROUTE_PRO_SUBSCRIPTION) {
                ProSubscriptionScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Private Media Vault Screen (Phase 22)
            composable(AppConstants.ROUTE_MEDIA_VAULT) {
                MediaVaultScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Uninstall Protection Screen (Phase 25)
            composable(AppConstants.ROUTE_UNINSTALL_PROTECTION) {
                UninstallProtectionScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Main App Shell Routes
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToPermissions = {
                        navController.navigate(AppConstants.ROUTE_PERMISSIONS)
                    }
                )
            }

            composable(Screen.Tools.route) {
                ToolsScreen(
                    onNavigateToIntruderLogs = {
                        navController.navigate(AppConstants.ROUTE_INTRUDER_LOGS)
                    },
                    onNavigateToNotificationShield = {
                        navController.navigate(AppConstants.ROUTE_NOTIFICATION_SHIELD)
                    },
                    onNavigateToWebsiteBlocker = {
                        navController.navigate(AppConstants.ROUTE_WEBSITE_BLOCKER)
                    },
                    onNavigateToPrivateBrowser = {
                        navController.navigate(AppConstants.ROUTE_PRIVATE_BROWSER)
                    },
                    onNavigateToDisguiseCover = {
                        navController.navigate(AppConstants.ROUTE_DISGUISE_COVER)
                    },
                    onNavigateToMediaVault = {
                        navController.navigate(AppConstants.ROUTE_MEDIA_VAULT)
                    },
                    onNavigateToUninstallProtection = {
                        navController.navigate(AppConstants.ROUTE_UNINSTALL_PROTECTION)
                    },
                    onNavigateToProSubscription = {
                        navController.navigate(AppConstants.ROUTE_PRO_SUBSCRIPTION)
                    }
                )
            }


            composable(Screen.Themes.route) {
                ThemesScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToPermissions = {
                        navController.navigate(AppConstants.ROUTE_PERMISSIONS)
                    },
                    onNavigateToIntruderLogs = {
                        navController.navigate(AppConstants.ROUTE_INTRUDER_LOGS)
                    },
                    onNavigateToNotificationShield = {
                        navController.navigate(AppConstants.ROUTE_NOTIFICATION_SHIELD)
                    },
                    onNavigateToWebsiteBlocker = {
                        navController.navigate(AppConstants.ROUTE_WEBSITE_BLOCKER)
                    },
                    onNavigateToDisguiseCover = {
                        navController.navigate(AppConstants.ROUTE_DISGUISE_COVER)
                    },
                    onNavigateToProSubscription = {
                        navController.navigate(AppConstants.ROUTE_PRO_SUBSCRIPTION)
                    },
                    onResetOnboarding = {
                        navController.navigate(AppConstants.ROUTE_SPLASH) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}


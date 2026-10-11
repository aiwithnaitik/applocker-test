package com.applock.privacy

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import com.applock.privacy.core.monitoring.AppLockSession
import com.applock.privacy.core.ui.theme.AppLockTheme
import com.applock.privacy.data.local.AppPreferencesDataSource
import com.applock.privacy.feature.auth.AppLockMasterLockScreen
import com.applock.privacy.feature.navigation.AppNavHost

class MainActivity : FragmentActivity() {

    private lateinit var preferencesDataSource: AppPreferencesDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        preferencesDataSource = AppPreferencesDataSource(this)

        setContent {
            AppLockTheme {
                val hasSecurityConfigured by preferencesDataSource.hasSecurityConfiguredFlow.collectAsState(initial = false)
                val isOnboardingCompleted by preferencesDataSource.isOnboardingCompletedFlow.collectAsState(initial = false)
                var isAuthenticated by remember { mutableStateOf(AppLockSession.isAppLockAuthenticated) }

                if (isOnboardingCompleted && hasSecurityConfigured && !isAuthenticated) {
                    AppLockMasterLockScreen(
                        onUnlockSuccess = {
                            AppLockSession.isAppLockAuthenticated = true
                            isAuthenticated = true
                        }
                    )
                } else {
                    AppNavHost()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        AppLockSession.isAppInForeground = true
    }

    override fun onResume() {
        super.onResume()
        AppLockSession.isAppInForeground = true
    }

    override fun onPause() {
        super.onPause()
        // Do not mark foreground false immediately on pause to allow transient pickers/permission dialogs
    }

    override fun onStop() {
        super.onStop()
        AppLockSession.isAppInForeground = false
        // Re-lock AppLock when leaving the app unless a transient system dialog/picker was launched
        if (!AppLockSession.isTransientSystemAction) {
            AppLockSession.isAppLockAuthenticated = false
        }
    }
}

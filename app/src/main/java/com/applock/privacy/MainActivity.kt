package com.applock.privacy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.applock.privacy.core.monitoring.AppLockSession
import com.applock.privacy.core.ui.theme.AppLockTheme
import com.applock.privacy.feature.navigation.AppNavHost

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppLockTheme {
                AppNavHost()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppLockSession.isAppInForeground = true
    }

    override fun onPause() {
        super.onPause()
        AppLockSession.isAppInForeground = false
    }
}

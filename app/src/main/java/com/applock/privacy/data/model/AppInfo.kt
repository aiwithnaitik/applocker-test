package com.applock.privacy.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Model representing an application installed on the Android device.
 * Marked @Immutable to allow Compose compiler smart skipping during LazyColumn scrolling.
 */
@Immutable
data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val isLocked: Boolean = false,
    val iconBitmap: ImageBitmap? = null
)

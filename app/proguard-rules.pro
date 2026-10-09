# =======================================================================
# AppLock Privacy Guard — ProGuard & R8 Optimization Rules (Phase 27)
# =======================================================================

# 1. Jetpack Compose & Kotlin Coroutines
-keepclassmembers class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# 2. Keep Data Classes & Models Serialized to JSON
-keep class com.applock.privacy.core.theme.CustomThemeConfig { *; }
-keep class com.applock.privacy.feature.vault.VaultMediaItem { *; }
-keep class com.applock.privacy.feature.intruder.IntruderLog { *; }
-keep class com.applock.privacy.feature.pro.ProPlan { *; }
-keep class com.applock.privacy.data.model.** { *; }

# 3. Android Components & Lifecycle Services
-keep class com.applock.privacy.core.monitoring.AppMonitorService { *; }
-keep class com.applock.privacy.core.monitoring.BootReceiver { *; }
-keep class com.applock.privacy.core.notification.AppNotificationListenerService { *; }
-keep class com.applock.privacy.feature.blocker.WebsiteBlockerAccessibilityService { *; }
-keep class com.applock.privacy.feature.lock.LockActivity { *; }
-keep class com.applock.privacy.feature.blocker.BlockedSiteActivity { *; }

# 4. AndroidX Biometric & Security
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# 5. DataStore Preferences
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# 6. Keep Line Numbers for Stack Traces in Crash Reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

package com.applock.privacy.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.applock.privacy.core.theme.AppThemeCatalog
import com.applock.privacy.core.theme.CustomThemeConfig
import com.applock.privacy.feature.intruder.IntruderLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "applock_user_settings")

/**
 * DataStore implementation for persisting user preferences, locked package lists,
 * security credentials (PIN & Pattern), themes, failed attempt lockout, and intruder logs.
 */
class AppPreferencesDataSource(private val context: Context) {

    companion object {
        val KEY_LOCKED_PACKAGES = stringSetPreferencesKey("locked_packages")
        val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_ONBOARDING_REASON = stringPreferencesKey("onboarding_reason")
        val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        val KEY_PIN_SALT = stringPreferencesKey("pin_salt")
        val KEY_PIN_LENGTH = intPreferencesKey("pin_length")
        val KEY_PATTERN_HASH = stringPreferencesKey("pattern_hash")
        val KEY_PATTERN_SALT = stringPreferencesKey("pattern_salt")
        val KEY_LOCK_TYPE = stringPreferencesKey("lock_type") // "pin" or "pattern"
        val KEY_SELECTED_THEME_ID = stringPreferencesKey("selected_theme_id")
        val KEY_APP_MONITOR_ACTIVE = booleanPreferencesKey("app_monitor_active")

        // Phase 12 & 13: Themes & Monetization
        val KEY_CUSTOM_THEMES_JSON = stringPreferencesKey("custom_themes_json")
        val KEY_UNLOCKED_THEMES = stringSetPreferencesKey("unlocked_themes")
        val KEY_SMART_THEME_ENABLED = booleanPreferencesKey("smart_theme_enabled")

        // Phase 14: Core Customization
        val KEY_IS_PATTERN_VISIBLE = booleanPreferencesKey("is_pattern_visible")
        val KEY_IS_HAPTIC_ENABLED = booleanPreferencesKey("is_haptic_enabled")
        val KEY_RELOCK_IMMEDIATELY = booleanPreferencesKey("relock_immediately")

        // Phase 15: Failed Attempt Protection
        val KEY_FAILED_ATTEMPTS = intPreferencesKey("failed_attempts")
        val KEY_LOCKOUT_UNTIL_TIMESTAMP = longPreferencesKey("lockout_until_timestamp")
        val KEY_FAILED_ATTEMPT_THRESHOLD = intPreferencesKey("failed_attempt_threshold")
        val KEY_LOCKOUT_DURATION_SECONDS = intPreferencesKey("lockout_duration_seconds")

        // Phase 16: Intruder Detection
        val KEY_INTRUDER_DETECTION_ENABLED = booleanPreferencesKey("intruder_detection_enabled")
        val KEY_INTRUDER_THRESHOLD = intPreferencesKey("intruder_threshold")
        val KEY_INTRUDER_LOGS_JSON = stringPreferencesKey("intruder_logs_json")

        // Phase 17: Intruder Alarm
        val KEY_ALARM_ENABLED = booleanPreferencesKey("alarm_enabled")
        val KEY_ALARM_THRESHOLD = intPreferencesKey("alarm_threshold")
        val KEY_ALARM_DURATION = intPreferencesKey("alarm_duration")

        // Phase 18: Notification Security
        val KEY_NOTIFICATION_SHIELD_ENABLED = booleanPreferencesKey("notification_shield_enabled")

        // Phase 19: Website Blocker
        val KEY_WEBSITE_BLOCKER_ENABLED = booleanPreferencesKey("website_blocker_enabled")
        val KEY_BLOCKED_WEBSITES_JSON = stringPreferencesKey("blocked_websites_json")

        // Phase 21: Disguise Cover (Fake App Appearance)
        val KEY_DISGUISE_MODE = stringPreferencesKey("disguise_mode") // "NONE", "CRASH_DIALOG", "CALCULATOR"
        val KEY_DISGUISE_APPLOCK_ONLY = booleanPreferencesKey("disguise_applock_only")
        val KEY_LAUNCHER_ALIAS = stringPreferencesKey("launcher_alias") // "default", "calculator", "notes"

        // Phase 22: Pro Subscription & Media Vault
        val KEY_IS_PRO_USER = booleanPreferencesKey("is_pro_user")
        val KEY_PRO_PLAN_ID = stringPreferencesKey("pro_plan_id")
        val KEY_PRO_EXPIRY_TIMESTAMP = longPreferencesKey("pro_expiry_timestamp")
        val KEY_VAULT_MEDIA_JSON = stringPreferencesKey("vault_media_json")
    }



    val lockedPackagesFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[KEY_LOCKED_PACKAGES] ?: emptySet()
    }

    val isOnboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val onboardingReasonFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_REASON]
    }

    val hasPinConfiguredFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        !preferences[KEY_PIN_HASH].isNullOrEmpty()
    }

    val hasPatternConfiguredFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        !preferences[KEY_PATTERN_HASH].isNullOrEmpty()
    }

    val hasSecurityConfiguredFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        !preferences[KEY_PIN_HASH].isNullOrEmpty() || !preferences[KEY_PATTERN_HASH].isNullOrEmpty()
    }

    val lockTypeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LOCK_TYPE] ?: "pin"
    }

    val pinLengthFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_PIN_LENGTH] ?: 4
    }

    val selectedThemeIdFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_THEME_ID] ?: "pure_light"
    }

    val isBiometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_BIOMETRIC_ENABLED] ?: true
    }

    val isAppMonitorActiveFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_APP_MONITOR_ACTIVE] ?: true
    }

    // Phase 12: Custom Themes
    val customThemesFlow: Flow<List<CustomThemeConfig>> = context.dataStore.data.map { preferences ->
        AppThemeCatalog.parseCustomThemesJson(preferences[KEY_CUSTOM_THEMES_JSON])
    }

    // Phase 13: Unlocked Themes
    val unlockedThemeIdsFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[KEY_UNLOCKED_THEMES] ?: setOf("pure_light", "sapphire_glass", "cyber_neon", "emerald_matrix", "obsidian_dark")
    }

    val isSmartThemeEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_SMART_THEME_ENABLED] ?: false
    }

    // Phase 14: Settings Customization
    val isPatternVisibleFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_PATTERN_VISIBLE] ?: true
    }

    val isHapticEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_HAPTIC_ENABLED] ?: true
    }

    val relockImmediatelyFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_RELOCK_IMMEDIATELY] ?: true
    }

    // Phase 15: Failed Attempt Lockout
    val failedAttemptsFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_FAILED_ATTEMPTS] ?: 0
    }

    val lockoutUntilTimestampFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[KEY_LOCKOUT_UNTIL_TIMESTAMP] ?: 0L
    }

    val failedAttemptThresholdFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_FAILED_ATTEMPT_THRESHOLD] ?: 3
    }

    val lockoutDurationSecondsFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_LOCKOUT_DURATION_SECONDS] ?: 60
    }

    // Phase 16: Intruder Detection
    val isIntruderDetectionEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_INTRUDER_DETECTION_ENABLED] ?: false
    }

    val intruderThresholdFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_INTRUDER_THRESHOLD] ?: 3
    }

    val intruderLogsFlow: Flow<List<IntruderLog>> = context.dataStore.data.map { preferences ->
        IntruderLog.parseLogsJson(preferences[KEY_INTRUDER_LOGS_JSON])
    }

    // Phase 17: Intruder Alarm
    val isAlarmEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ALARM_ENABLED] ?: false
    }

    val alarmThresholdFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_ALARM_THRESHOLD] ?: 3
    }

    val alarmDurationFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_ALARM_DURATION] ?: 30
    }

    // Phase 18: Notification Security
    val isNotificationShieldEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATION_SHIELD_ENABLED] ?: false
    }

    // Phase 19: Website Blocker
    val isWebsiteBlockerEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_WEBSITE_BLOCKER_ENABLED] ?: true
    }

    val blockedWebsitesFlow: Flow<List<com.applock.privacy.feature.blocker.BlockedWebsite>> = context.dataStore.data.map { preferences ->
        com.applock.privacy.feature.blocker.BlockedWebsite.parseWebsitesJson(preferences[KEY_BLOCKED_WEBSITES_JSON])
    }

    // Phase 21: Disguise Cover (Fake App Appearance)
    val disguiseModeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_DISGUISE_MODE] ?: "NONE"
    }

    val isDisguiseAppLockOnlyFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DISGUISE_APPLOCK_ONLY] ?: false
    }

    val launcherAliasFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAUNCHER_ALIAS] ?: "default"
    }

    // Phase 22: Pro Subscription
    val isProUserFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_PRO_USER] ?: false
    }

    val proPlanIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_PRO_PLAN_ID]
    }

    val proExpiryTimestampFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[KEY_PRO_EXPIRY_TIMESTAMP] ?: 0L
    }

    val vaultMediaFlow: Flow<List<com.applock.privacy.feature.vault.VaultMediaItem>> = context.dataStore.data.map { preferences ->
        com.applock.privacy.feature.vault.VaultMediaItem.parseMediaJson(preferences[KEY_VAULT_MEDIA_JSON])
    }



    // Mutators
    suspend fun setPackageLocked(packageName: String, isLocked: Boolean) {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_LOCKED_PACKAGES]?.toMutableSet() ?: mutableSetOf()
            if (isLocked) {
                current.add(packageName)
            } else {
                current.remove(packageName)
            }
            preferences[KEY_LOCKED_PACKAGES] = current
        }
    }

    suspend fun savePin(hash: String, salt: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PIN_HASH] = hash
            preferences[KEY_PIN_SALT] = salt
        }
    }

    suspend fun getPinHash(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[KEY_PIN_HASH]
    }

    suspend fun getPinSalt(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[KEY_PIN_SALT]
    }

    suspend fun clearPin() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_PIN_HASH)
            preferences.remove(KEY_PIN_SALT)
        }
    }

    suspend fun savePattern(hash: String, salt: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PATTERN_HASH] = hash
            preferences[KEY_PATTERN_SALT] = salt
        }
    }

    suspend fun getPatternHash(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[KEY_PATTERN_HASH]
    }

    suspend fun getPatternSalt(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[KEY_PATTERN_SALT]
    }

    suspend fun clearPattern() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_PATTERN_HASH)
            preferences.remove(KEY_PATTERN_SALT)
        }
    }

    suspend fun setLockType(lockType: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LOCK_TYPE] = lockType
        }
    }

    suspend fun setSelectedTheme(themeId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_THEME_ID] = themeId
        }
    }

    suspend fun setSmartThemeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SMART_THEME_ENABLED] = enabled
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setAppMonitorActive(active: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_APP_MONITOR_ACTIVE] = active
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun saveOnboardingReason(reason: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_REASON] = reason
        }
    }

    suspend fun setPinLength(length: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PIN_LENGTH] = length
        }
    }

    suspend fun clearAllLockedPackages() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_LOCKED_PACKAGES)
        }
    }

    suspend fun resetOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = false
            preferences.remove(KEY_ONBOARDING_REASON)
            preferences.remove(KEY_LOCKED_PACKAGES)
            preferences.remove(KEY_PIN_HASH)
            preferences.remove(KEY_PIN_SALT)
            preferences.remove(KEY_PATTERN_HASH)
            preferences.remove(KEY_PATTERN_SALT)
            preferences[KEY_PIN_LENGTH] = 4
            preferences[KEY_APP_MONITOR_ACTIVE] = true
        }
    }

    // Phase 12 Mutators
    suspend fun saveCustomTheme(theme: CustomThemeConfig) {
        context.dataStore.edit { preferences ->
            val list = AppThemeCatalog.parseCustomThemesJson(preferences[KEY_CUSTOM_THEMES_JSON]).toMutableList()
            list.removeAll { it.id == theme.id }
            list.add(0, theme)
            preferences[KEY_CUSTOM_THEMES_JSON] = AppThemeCatalog.serializeCustomThemes(list)
            // Automatically select created theme
            preferences[KEY_SELECTED_THEME_ID] = theme.id
        }
    }

    suspend fun deleteCustomTheme(themeId: String) {
        context.dataStore.edit { preferences ->
            val list = AppThemeCatalog.parseCustomThemesJson(preferences[KEY_CUSTOM_THEMES_JSON]).toMutableList()
            list.removeAll { it.id == themeId }
            preferences[KEY_CUSTOM_THEMES_JSON] = AppThemeCatalog.serializeCustomThemes(list)
            if (preferences[KEY_SELECTED_THEME_ID] == themeId) {
                preferences[KEY_SELECTED_THEME_ID] = "pure_light"
            }
        }
    }

    // Phase 13 Mutators
    suspend fun unlockTheme(themeId: String) {
        context.dataStore.edit { preferences ->
            val set = (preferences[KEY_UNLOCKED_THEMES] ?: setOf("pure_light", "sapphire_glass", "cyber_neon", "emerald_matrix", "obsidian_dark")).toMutableSet()
            set.add(themeId)
            preferences[KEY_UNLOCKED_THEMES] = set
        }
    }

    // Phase 14 Mutators
    suspend fun setPatternVisible(visible: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_PATTERN_VISIBLE] = visible
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun setRelockImmediately(immediate: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_RELOCK_IMMEDIATELY] = immediate
        }
    }

    // Phase 15 Mutators
    suspend fun setFailedAttemptThreshold(threshold: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FAILED_ATTEMPT_THRESHOLD] = threshold
        }
    }

    suspend fun setLockoutDurationSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LOCKOUT_DURATION_SECONDS] = seconds
        }
    }

    suspend fun recordFailedAttempt(): Pair<Int, Boolean> {
        var newCount = 1
        var isLockedOut = false
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_FAILED_ATTEMPTS] ?: 0
            newCount = current + 1
            preferences[KEY_FAILED_ATTEMPTS] = newCount

            val threshold = preferences[KEY_FAILED_ATTEMPT_THRESHOLD] ?: 3
            if (newCount >= threshold) {
                val durationSec = preferences[KEY_LOCKOUT_DURATION_SECONDS] ?: 60
                val lockoutUntil = System.currentTimeMillis() + (durationSec * 1000L)
                preferences[KEY_LOCKOUT_UNTIL_TIMESTAMP] = lockoutUntil
                isLockedOut = true
            }
        }
        return Pair(newCount, isLockedOut)
    }

    suspend fun resetFailedAttempts() {
        context.dataStore.edit { preferences ->
            preferences[KEY_FAILED_ATTEMPTS] = 0
            preferences[KEY_LOCKOUT_UNTIL_TIMESTAMP] = 0L
        }
    }

    // Phase 16 Mutators
    suspend fun setIntruderDetectionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_INTRUDER_DETECTION_ENABLED] = enabled
        }
    }

    suspend fun setIntruderThreshold(threshold: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_INTRUDER_THRESHOLD] = threshold
        }
    }

    suspend fun saveIntruderLog(log: IntruderLog) {
        context.dataStore.edit { preferences ->
            val list = IntruderLog.parseLogsJson(preferences[KEY_INTRUDER_LOGS_JSON]).toMutableList()
            list.add(0, log)
            preferences[KEY_INTRUDER_LOGS_JSON] = IntruderLog.serializeLogs(list.take(50))
        }
    }

    suspend fun deleteIntruderLog(logId: String) {
        context.dataStore.edit { preferences ->
            val list = IntruderLog.parseLogsJson(preferences[KEY_INTRUDER_LOGS_JSON]).toMutableList()
            list.removeAll { it.id == logId }
            preferences[KEY_INTRUDER_LOGS_JSON] = IntruderLog.serializeLogs(list)
        }
    }

    suspend fun clearIntruderLogs() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_INTRUDER_LOGS_JSON)
        }
    }

    // Phase 17: Intruder Alarm Mutators
    suspend fun setAlarmEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ALARM_ENABLED] = enabled
        }
    }

    suspend fun setAlarmThreshold(threshold: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ALARM_THRESHOLD] = threshold
        }
    }

    suspend fun setAlarmDuration(durationSeconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ALARM_DURATION] = durationSeconds
        }
    }

    // Phase 18: Notification Security Mutators
    suspend fun setNotificationShieldEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_SHIELD_ENABLED] = enabled
        }
    }

    // Phase 19: Website Blocker Mutators
    suspend fun setWebsiteBlockerEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_WEBSITE_BLOCKER_ENABLED] = enabled
        }
    }

    suspend fun addBlockedWebsite(website: com.applock.privacy.feature.blocker.BlockedWebsite) {
        context.dataStore.edit { preferences ->
            val list = com.applock.privacy.feature.blocker.BlockedWebsite.parseWebsitesJson(preferences[KEY_BLOCKED_WEBSITES_JSON]).toMutableList()
            // Check if domain already exists
            val existingIndex = list.indexOfFirst { it.domain.equals(website.domain, ignoreCase = true) }
            if (existingIndex >= 0) {
                list[existingIndex] = website
            } else {
                list.add(0, website)
            }
            preferences[KEY_BLOCKED_WEBSITES_JSON] = com.applock.privacy.feature.blocker.BlockedWebsite.serializeWebsites(list)
        }
    }

    suspend fun toggleBlockedWebsite(id: String, isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            val list = com.applock.privacy.feature.blocker.BlockedWebsite.parseWebsitesJson(preferences[KEY_BLOCKED_WEBSITES_JSON]).map {
                if (it.id == id) it.copy(isEnabled = isEnabled) else it
            }
            preferences[KEY_BLOCKED_WEBSITES_JSON] = com.applock.privacy.feature.blocker.BlockedWebsite.serializeWebsites(list)
        }
    }

    suspend fun deleteBlockedWebsite(id: String) {
        context.dataStore.edit { preferences ->
            val list = com.applock.privacy.feature.blocker.BlockedWebsite.parseWebsitesJson(preferences[KEY_BLOCKED_WEBSITES_JSON]).filterNot { it.id == id }
            preferences[KEY_BLOCKED_WEBSITES_JSON] = com.applock.privacy.feature.blocker.BlockedWebsite.serializeWebsites(list)
        }
    }

    // Phase 21: Disguise Cover Mutators
    suspend fun setDisguiseMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DISGUISE_MODE] = mode
        }
    }

    suspend fun setDisguiseAppLockOnly(appLockOnly: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DISGUISE_APPLOCK_ONLY] = appLockOnly
        }
    }

    suspend fun setLauncherAlias(alias: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAUNCHER_ALIAS] = alias
        }
    }

    // Phase 22: Pro Subscription Mutators
    suspend fun setProSubscription(isPro: Boolean, planId: String? = null, expiryTimestamp: Long = 0L) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_PRO_USER] = isPro
            if (planId != null) {
                preferences[KEY_PRO_PLAN_ID] = planId
            } else {
                preferences.remove(KEY_PRO_PLAN_ID)
            }
            preferences[KEY_PRO_EXPIRY_TIMESTAMP] = expiryTimestamp
        }
    }

    suspend fun clearProSubscription() {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_PRO_USER] = false
            preferences.remove(KEY_PRO_PLAN_ID)
            preferences.remove(KEY_PRO_EXPIRY_TIMESTAMP)
        }
    }

    // Media Vault Mutators
    suspend fun addVaultMediaItem(item: com.applock.privacy.feature.vault.VaultMediaItem) {
        context.dataStore.edit { preferences ->
            val list = com.applock.privacy.feature.vault.VaultMediaItem.parseMediaJson(preferences[KEY_VAULT_MEDIA_JSON]).toMutableList()
            list.add(0, item)
            preferences[KEY_VAULT_MEDIA_JSON] = com.applock.privacy.feature.vault.VaultMediaItem.serializeMedia(list)
        }
    }

    suspend fun deleteVaultMediaItem(id: String) {
        context.dataStore.edit { preferences ->
            val list = com.applock.privacy.feature.vault.VaultMediaItem.parseMediaJson(preferences[KEY_VAULT_MEDIA_JSON]).filterNot { it.id == id }
            preferences[KEY_VAULT_MEDIA_JSON] = com.applock.privacy.feature.vault.VaultMediaItem.serializeMedia(list)
        }
    }
}



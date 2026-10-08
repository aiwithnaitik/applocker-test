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
        val KEY_PATTERN_HASH = stringPreferencesKey("pattern_hash")
        val KEY_PATTERN_SALT = stringPreferencesKey("pattern_salt")
        val KEY_LOCK_TYPE = stringPreferencesKey("lock_type") // "pin" or "pattern"
        val KEY_SELECTED_THEME_ID = stringPreferencesKey("selected_theme_id")
        val KEY_APP_MONITOR_ACTIVE = booleanPreferencesKey("app_monitor_active")

        // Phase 12 & 13: Themes & Monetization
        val KEY_CUSTOM_THEMES_JSON = stringPreferencesKey("custom_themes_json")
        val KEY_UNLOCKED_THEMES = stringSetPreferencesKey("unlocked_themes")

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

    val lockTypeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LOCK_TYPE] ?: "pin"
    }

    val selectedThemeIdFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_THEME_ID] ?: "sapphire_glass"
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
        preferences[KEY_UNLOCKED_THEMES] ?: setOf("sapphire_glass", "cyber_neon", "emerald_matrix", "obsidian_dark")
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

    suspend fun resetOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = false
            preferences.remove(KEY_ONBOARDING_REASON)
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
                preferences[KEY_SELECTED_THEME_ID] = "sapphire_glass"
            }
        }
    }

    // Phase 13 Mutators
    suspend fun unlockTheme(themeId: String) {
        context.dataStore.edit { preferences ->
            val set = (preferences[KEY_UNLOCKED_THEMES] ?: setOf("sapphire_glass", "cyber_neon", "emerald_matrix", "obsidian_dark")).toMutableSet()
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
}

package com.applock.privacy.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "applock_user_settings")

/**
 * DataStore implementation for persisting user preferences, locked package lists,
 * security credentials (PIN & Pattern), and themes.
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
}

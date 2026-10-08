package com.applock.privacy.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "applock_user_settings")

/**
 * DataStore implementation for persisting user preferences, locked package lists, and onboarding state.
 */
class AppPreferencesDataSource(private val context: Context) {

    companion object {
        val KEY_LOCKED_PACKAGES = stringSetPreferencesKey("locked_packages")
        val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_ONBOARDING_REASON = stringPreferencesKey("onboarding_reason")
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

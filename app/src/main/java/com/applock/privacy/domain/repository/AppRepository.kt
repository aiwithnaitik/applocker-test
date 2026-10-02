package com.applock.privacy.domain.repository

import com.applock.privacy.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

/**
 * Interface contract for application locking and discovery.
 */
interface AppRepository {
    fun getLockedApps(): Flow<List<AppInfo>>
    suspend fun setAppLocked(packageName: String, isLocked: Boolean)
    suspend fun isAppLocked(packageName: String): Boolean
}

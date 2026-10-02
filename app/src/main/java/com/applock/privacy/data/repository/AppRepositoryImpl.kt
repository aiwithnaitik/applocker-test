package com.applock.privacy.data.repository

import com.applock.privacy.data.local.AppPreferencesDataSource
import com.applock.privacy.domain.model.AppInfo
import com.applock.privacy.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Concrete implementation of AppRepository backed by local DataStore.
 */
class AppRepositoryImpl(
    private val preferencesDataSource: AppPreferencesDataSource
) : AppRepository {

    override fun getLockedApps(): Flow<List<AppInfo>> {
        return preferencesDataSource.lockedPackagesFlow.map { packageSet ->
            packageSet.map { pkg ->
                AppInfo(
                    packageName = pkg,
                    appName = pkg.substringAfterLast("."),
                    isLocked = true
                )
            }
        }
    }

    override suspend fun setAppLocked(packageName: String, isLocked: Boolean) {
        preferencesDataSource.setPackageLocked(packageName, isLocked)
    }

    override suspend fun isAppLocked(packageName: String): Boolean {
        val lockedSet = preferencesDataSource.lockedPackagesFlow.first()
        return lockedSet.contains(packageName)
    }
}

package com.applock.privacy.domain.usecase

import com.applock.privacy.domain.model.AppInfo
import com.applock.privacy.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe the stream of currently locked applications.
 */
class GetLockedAppsUseCase(private val repository: AppRepository) {
    operator fun invoke(): Flow<List<AppInfo>> {
        return repository.getLockedApps()
    }
}

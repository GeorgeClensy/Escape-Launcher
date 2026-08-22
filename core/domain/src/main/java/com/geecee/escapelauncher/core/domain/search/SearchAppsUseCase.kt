package com.geecee.escapelauncher.core.domain.search

import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import jakarta.inject.Inject

/***
 * Use case to return all installed apps filtered with a query
 */
class SearchAppsUseCase @Inject constructor(
    private val appsRepository: AppsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository
) {
    operator fun invoke(queryFlow: Flow<String>, showHiddenFlow: Flow<Boolean>): Flow<List<InstalledApp>> {
        return combine(
            appsRepository.mainUserApps,
            modifiedAppsRepository.getHiddenPackageIdsFlow(),
            queryFlow,
            showHiddenFlow
        ) { allApps, hiddenIds, rawQuery, showHidden ->
            val query = rawQuery.trim()
            val hiddenSet = hiddenIds.toSet()

            val filtered = if (query.isBlank()) {
                allApps.filter { !hiddenSet.contains(it.packageName) }
            } else {
                allApps.filter { app ->
                    val isHidden = hiddenSet.contains(app.packageName)
                    val matchesQuery = fuzzyMatch(app.displayName, query)
                    matchesQuery && (!isHidden || showHidden)
                }
            }

            if (query.isNotBlank()) {
                sortAppsByRelevance(filtered, query)
            } else {
                filtered
            }
        }
    }
}

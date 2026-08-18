package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.model.AppShortcut
import jakarta.inject.Inject

/**
 * Use case to retrieve dynamic and manifest shortcuts for a given package name.
 *
 * This use case encapsulates the logic for querying shortcuts from the [AppsRepository].
 * It should be used to populate long-press menus or other UI elements that display
 * app-specific quick actions.
 */
class GetAppShortcutsUseCase @Inject constructor(
    private val appsRepository: AppsRepository
) {
    /**
     * Executes the shortcut retrieval.
     *
     * @param packageName The package name of the app to get shortcuts for.
     * @return A list of [AppShortcut] available for the app.
     */
    operator fun invoke(packageName: String): List<AppShortcut> {
        return appsRepository.getShortcuts(packageName)
    }
}

package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import jakarta.inject.Inject

/**
 * Use case to launch an app shortcut.
 *
 * This use case provides a clean entry point for starting a shortcut action
 * given its owner's package name and the unique shortcut identifier.
 */
class StartShortcutUseCase @Inject constructor(
    private val appsRepository: AppsRepository
) {
    /**
     * Launches the specified shortcut.
     *
     * @param packageName The package name of the app owning the shortcut.
     * @param shortcutId The ID of the shortcut to launch.
     */
    operator fun invoke(packageName: String, shortcutId: String) {
        appsRepository.startShortcut(packageName, shortcutId)
    }
}

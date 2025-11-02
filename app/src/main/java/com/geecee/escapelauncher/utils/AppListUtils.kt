package com.geecee.escapelauncher.utils

import com.geecee.escapelauncher.HomeScreenModel
import com.geecee.escapelauncher.MainAppViewModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.utils.AppUtils.resetHome

fun handleSearch(
    mainAppModel: MainAppViewModel,
    homeScreenModel: HomeScreenModel,
    autoOpenEnabled: Boolean
) {
    // Get the list of installed apps with the results filtered using fuzzy matching
    var filteredApps = homeScreenModel.installedApps.filter { appInfo ->
        AppUtils.fuzzyMatch(
            appInfo.displayName,
            homeScreenModel.searchText.value
        )
    }

    // Remove the launcher if present
    filteredApps = filteredApps.filter { appInfo ->
        !appInfo.packageName.contains("com.geecee.escapelauncher")
    }

    if (autoOpenEnabled && filteredApps.size == 1) {
        val appInfo = filteredApps.first()

        var shouldShowHiddenApps =
            !mainAppModel.hiddenAppsManager.isAppHidden(appInfo.packageName)

        if (!homeScreenModel.searchText.value.isBlank() && getBooleanSetting(
                mainAppModel.getContext(),
                mainAppModel.getContext().getString(R.string.showHiddenAppsInSearch),
                false
            )
        ) {
            shouldShowHiddenApps = true
        }

        if (shouldShowHiddenApps) {
            homeScreenModel.updateSelectedApp(appInfo)

            AppUtils.openApp(
                app = appInfo,
                overrideOpenChallenge = false,
                openChallengeShow = homeScreenModel.showOpenChallenge,
                mainAppModel = mainAppModel,
                homeScreenModel = homeScreenModel
            )

            resetHome(homeScreenModel)
        }
    }
}
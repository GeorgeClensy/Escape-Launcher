package com.geecee.escapelauncher

import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("escapelauncher.android.library")
                apply("escapelauncher.android.compose.ui")
                apply("escapelauncher.android.hilt")
                apply("escapelauncher.android.testing")
            }
        }
    }
}

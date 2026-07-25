package com.geecee.escapelauncher

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

@Suppress("unused")
class AndroidComposeUiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Reuse the existing compose plugin to set up the BOM and buildFeatures
            pluginManager.apply("escapelauncher.android.compose")

            dependencies {
                add("implementation", libs.findBundle("compose-ui").get())
                add("debugImplementation", libs.findLibrary("androidx-ui-tooling").get())
            }
        }
    }
}
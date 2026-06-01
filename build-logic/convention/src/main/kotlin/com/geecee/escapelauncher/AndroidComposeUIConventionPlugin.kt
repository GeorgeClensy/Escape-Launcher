package com.geecee.escapelauncher

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType

@Suppress("unused")
class AndroidComposeUiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Reuse the existing compose plugin to set up the BOM and buildFeatures
            pluginManager.apply("escapelauncher.android.compose")

            val extension = extensions.findByType<ApplicationExtension>()
                ?: extensions.findByType<LibraryExtension>()
                ?: return

            configureAndroidCompose(extension)

            dependencies {
                add("implementation", libs.findLibrary("androidx-compose-material3").get())
                add("implementation", libs.findLibrary("androidx-compose-material-icons-core").get())
                add("implementation", libs.findLibrary("androidx-compose-material-icons-extended").get())
                add("debugImplementation", libs.findLibrary("androidx-ui-tooling").get())
                add("implementation", libs.findLibrary("androidx-ui-tooling-preview").get())
                add("implementation", libs.findLibrary("androidx-activity-compose").get())
            }
        }
    }
}
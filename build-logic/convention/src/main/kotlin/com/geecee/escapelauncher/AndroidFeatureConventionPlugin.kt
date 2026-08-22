package com.geecee.escapelauncher

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

@Suppress("unused")
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("escapelauncher.android.library")
                apply("escapelauncher.android.composeui")
                apply("escapelauncher.android.hilt")
                apply("escapelauncher.android.testing")
            }

            dependencies {
                add("implementation", libs.findBundle("hilt").get())
            }
        }
    }
}

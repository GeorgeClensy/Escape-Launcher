package com.geecee.escapelauncher

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

@Suppress("unused")
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("escapelauncher.android.flavours")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                lint {
                    targetSdk = libs.findVersion("targetSdk").get().toString().toInt()
                }
            }
        }
    }
}

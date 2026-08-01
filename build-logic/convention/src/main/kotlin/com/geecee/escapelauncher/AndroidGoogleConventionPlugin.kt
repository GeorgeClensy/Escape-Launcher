package com.geecee.escapelauncher

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

@Suppress("unused")
class AndroidGoogleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val fire = "fire"
            val base = "base"
            val google = "google"
            val firebase = "${fire}${base}"

            dependencies {
                add("${google}Implementation", libs.findLibrary("androidx-ui-text-google-fonts").get())

                if (pluginManager.hasPlugin("com.android.application") || pluginManager.hasPlugin("com.android.library")) {
                    add("${google}Implementation", platform(libs.findLibrary("fb-bom").get()))
                    add("${google}Implementation", libs.findLibrary("fb-analytics").get())
                    add("${google}Implementation", libs.findLibrary("fb-crashlytics").get())
                    add("${google}Implementation", libs.findLibrary("fb-perf").get())
                    add("${google}Implementation", libs.findLibrary("fb-messaging").get())
                    add("${google}Implementation", libs.findLibrary("gms-play-services-location").get())
                    add("${google}Implementation", libs.findLibrary("okhttp").get())
                }
            }

            val taskNames = gradle.startParameter.taskNames
            val isGoogleVariant = taskNames.any { it.lowercase().contains(google) }
            val isIdeSync = System.getProperty("idea.sync.active") == "true"
            val isApp = pluginManager.hasPlugin("com.android.application")

            if ((isGoogleVariant || isIdeSync) && isApp) {
                pluginManager.apply("com.$google.gms.$google-services")
                pluginManager.apply("com.$google.$firebase.crashlytics")
            }
        }
    }
}
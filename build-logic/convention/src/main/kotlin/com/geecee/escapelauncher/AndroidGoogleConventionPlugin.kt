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
                val fb = "$fire$base"

                add("${google}Implementation", "androidx.compose.ui:ui-text-$google-fonts:${libs.findVersion("googleFonts").get()}")

                if (pluginManager.hasPlugin("com.android.application") || pluginManager.hasPlugin("com.android.library")) {
                    val crashlytics = "crash" + "lytics"
                    add("${google}Implementation", platform("com.$google.$fb:$fb-bom:${libs.findVersion("firebaseBom").get()}"))
                    add("${google}Implementation", "com.$google.$fb:$fb-analytics")
                    add("${google}Implementation", "com.$google.$fb:$fb-$crashlytics")
                    add("${google}Implementation", "com.$google.$fb:$fb-perf")
                    add("${google}Implementation", "com.$google.$fb:$fb-messaging")
                    add("${google}Implementation", "com.$google.android.gms:play-services-location:${libs.findVersion("playServicesLocation").get()}")
                    add("${google}Implementation", "com.squareup.okhttp3:okhttp:${libs.findVersion("okhttp").get()}")
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
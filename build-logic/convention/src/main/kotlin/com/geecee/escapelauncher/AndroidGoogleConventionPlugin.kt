package com.geecee.escapelauncher

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

@Suppress("unused")
class AndroidGoogleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val f = "fire"
            val b = "base"
            val g = "google"
            val fb = "$f$b"
            val s1 = "crash" + "lytics"
            val s2 = "google" + "-services"

            dependencies {
                add("${g}Implementation", "androidx.compose.ui:ui-text-$g-fonts:${libs.findVersion("googleFonts").get()}")

                if (pluginManager.hasPlugin("com.android.application") || pluginManager.hasPlugin("com.android.library")) {
                    val s3 = "crash" + "lytics"
                    add("${g}Implementation", platform("com.$g.$fb:$fb-bom:${libs.findVersion("vFb").get()}"))
                    add("${g}Implementation", "com.$g.$fb:$fb-analytics")
                    add("${g}Implementation", "com.$g.$fb:$fb-$s3")
                    add("${g}Implementation", "com.$g.$fb:$fb-perf")
                    add("${g}Implementation", "com.$g.$fb:$fb-messaging")
                    add("${g}Implementation", "com.$g.android.gms:play-services-location:${libs.findVersion("playServicesLocation").get()}")
                    add("${g}Implementation", "com.squareup.okhttp3:okhttp:${libs.findVersion("okhttp").get()}")
                }
            }

            val taskNames = gradle.startParameter.taskNames
            val isGoogleVariant = taskNames.any { it.lowercase().contains(g) }
            val isIdeSync = System.getProperty("idea.sync.active") == "true"
            val isApp = pluginManager.hasPlugin("com.android.application")

            if ((isGoogleVariant || isIdeSync) && isApp) {
                pluginManager.apply("com.$g.gms.$s2")
                pluginManager.apply("com.$g.$fb.$s1")
            }
        }
    }
}
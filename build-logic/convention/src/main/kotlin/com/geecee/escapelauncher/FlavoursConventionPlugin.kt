package com.geecee.escapelauncher

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class FlavorsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.withPlugin("com.android.application") {
                applyFlavors(target)
            }
            pluginManager.withPlugin("com.android.library") {
                applyFlavors(target)
            }
        }
    }
}

private fun applyFlavors(target: Project) {
    with(target) {
        val android = extensions.findByType(BaseExtension::class.java) ?: return

        android.apply {
            flavorDimensions("distribution")

            productFlavors {
                create("google") {
                    dimension = "distribution"
                    buildConfigField("boolean", "IS_FOSS", "false")
                }
                create("foss") {
                    dimension = "distribution"
                    buildConfigField("boolean", "IS_FOSS", "true")
                }
            }

            sourceSets {
                getByName("foss") {
                    res.srcDirs("src/foss/res")
                    java.srcDirs("src/foss/java")
                }
                getByName("google") {
                    res.srcDirs("src/google/res")
                    java.srcDirs("src/google/java")
                }
            }
        }

        // Apply a Google-only script if one exists alongside this module's build file
        val isFoss = isFossBuild(gradle)
        if (!isFoss) {
            val scriptFile = rootProject.file("google.gradle")
            if (scriptFile.exists()) {
                apply(mapOf("from" to scriptFile))
                println(">>> [FlavorsPlugin] Added the google.gradle script to this build because it's a google build. :D (Or it could be a sync, for some reason its fine that if the google stuff is in a foss sync cuz its not in the build.)")
            }
        } else {
            println(">>> [FlavorsPlugin] Did not add the google.gradle build script to this build because it's not a google build D:")
        }
    }
}

private fun isFossBuild(gradle: org.gradle.api.invocation.Gradle): Boolean =
    gradle.startParameter.taskNames.any { it.contains("foss", ignoreCase = true) }
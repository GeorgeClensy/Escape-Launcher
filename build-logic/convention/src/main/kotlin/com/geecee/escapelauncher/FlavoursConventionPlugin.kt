package com.geecee.escapelauncher

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke

@Suppress("unused")
class FlavoursConventionPlugin : Plugin<Project> {
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
        val android = extensions.findByType(CommonExtension::class.java) ?: return

        android.apply {
            flavorDimensions += "distribution"

            buildFeatures.buildConfig = true

            productFlavors {
                create("google") {
                    dimension = "distribution"
                    buildConfigField("boolean", "IS_FOSS", "false")
                    buildConfigField("String", "APP_FLAVOUR", "\"Google APIs\"")
                    if(target.plugins.hasPlugin("com.android.application")) {
                        resValue("string", "app_flavour", "Google APIs")
                    }
                }
                create("foss") {
                    dimension = "distribution"
                    buildConfigField("boolean", "IS_FOSS", "true")
                    buildConfigField("String", "APP_FLAVOUR", "\"FOSS\"")
                    if(target.plugins.hasPlugin("com.android.application")) {
                        resValue("string", "app_flavour", "FOSS")
                    }
                }
            }

            sourceSets {
                getByName("foss") {
                    res.directories.add("src/foss/res")
                    java.directories.add("src/foss/java")
                }
                getByName("google") {
                    res.directories.add("src/google/res")
                    java.directories.add("src/google/java")
                }
            }
        }

        // Apply Google-specific logic via the new convention plugin
        pluginManager.apply("escapelauncher.android.google")
    }
}
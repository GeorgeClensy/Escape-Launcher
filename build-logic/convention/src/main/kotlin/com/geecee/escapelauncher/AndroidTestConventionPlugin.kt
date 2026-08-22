package com.geecee.escapelauncher

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

@Suppress("Unused")
class AndroidTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                add("testImplementation", libs.findBundle("unit-testing").get())
                add("androidTestImplementation", libs.findBundle("android-testing").get())
            }
        }
    }
}
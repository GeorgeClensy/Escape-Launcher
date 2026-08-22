package com.geecee.escapelauncher

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

@Suppress("unused")
class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
            }

            dependencies {
                add("implementation", libs.findBundle("room").get())
                add("ksp", libs.findLibrary("androidx-room-compiler").get())
            }
        }
    }
}
// Top-level build file where you can add configuration options common to all subprojects/modules.
buildscript {
    val taskNames = gradle.startParameter.taskNames
    val isFoss = taskNames.any { it.contains("foss", ignoreCase = true) }
    if (!isFoss) {
        repositories {
            google()
            mavenCentral()
        }
        dependencies {
            //noinspection UseTomlInstead
            classpath("com.google.gms:google-services:4.4.4")
            classpath("com.google.fire" + "base:fire" + "base-crash" + "lytics-gradle:3.0.6")
        }
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.compose.compiler) apply false
}

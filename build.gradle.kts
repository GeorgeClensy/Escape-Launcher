// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val isFoss = gradle.startParameter.taskNames.any { it.contains("foss", ignoreCase = true) }
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
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.google.devtools.ksp") version "2.3.2" apply false
    alias(libs.plugins.compose.compiler) apply false
}

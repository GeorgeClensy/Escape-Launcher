package com.geecee.escapelauncher

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: ApplicationExtension,
) {
    commonExtension.compileSdk = libs.findVersion("compileSdk").get().toString().toInt()

    commonExtension.defaultConfig {
        minSdk = libs.findVersion("minSdk").get().toString().toInt()
    }

    commonExtension.compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    configureKotlin()
}

internal fun Project.configureKotlinAndroid(
    commonExtension: LibraryExtension,
) {
    commonExtension.compileSdk = libs.findVersion("compileSdk").get().toString().toInt()

    commonExtension.defaultConfig {
        minSdk = libs.findVersion("minSdk").get().toString().toInt()
    }

    commonExtension.compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    configureKotlin()
}

/**
 * Configure base Kotlin options
 */
private fun Project.configureKotlin() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)

            // Precise annotations control the runtime behaviour of the Kotlin compiler.
            val warningsAsErrors = project.findProperty("warningsAsErrors")?.toString()
            allWarningsAsErrors.set(warningsAsErrors.toBoolean())

            freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
        }
    }
}
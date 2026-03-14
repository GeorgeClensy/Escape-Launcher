import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.geecee.escapelauncher.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.compose.compiler.gradlePlugin)
    implementation(libs.hilt.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "escapelauncher.android.application"
            implementationClass = "com.geecee.escapelauncher.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "escapelauncher.android.library"
            implementationClass = "com.geecee.escapelauncher.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "escapelauncher.android.compose"
            implementationClass = "com.geecee.escapelauncher.AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "escapelauncher.android.hilt"
            implementationClass = "com.geecee.escapelauncher.AndroidHiltConventionPlugin"
        }
    }
}

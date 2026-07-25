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
            id = libs.plugins.escapelauncher.android.application.get().pluginId
            implementationClass = "com.geecee.escapelauncher.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.escapelauncher.android.library.get().pluginId
            implementationClass = "com.geecee.escapelauncher.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = libs.plugins.escapelauncher.android.compose.get().pluginId
            implementationClass = "com.geecee.escapelauncher.AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = libs.plugins.escapelauncher.android.hilt.get().pluginId
            implementationClass = "com.geecee.escapelauncher.AndroidHiltConventionPlugin"
        }
        register("escapeLauncherFlavours") {
            id = libs.plugins.escapelauncher.android.flavours.get().pluginId
            implementationClass = "com.geecee.escapelauncher.FlavoursConventionPlugin"
        }
        register("escapeLauncherTests") {
            id = libs.plugins.escapelauncher.android.testing.get().pluginId
            implementationClass = "com.geecee.escapelauncher.AndroidTestConventionPlugin"
        }
        register("androidRoom") {
            id = libs.plugins.escapelauncher.android.room.get().pluginId
            implementationClass = "com.geecee.escapelauncher.AndroidRoomConventionPlugin"
        }
        register("androidComposeUi") {
            id = libs.plugins.escapelauncher.android.composeui.get().pluginId
            implementationClass = "com.geecee.escapelauncher.AndroidComposeUiConventionPlugin"
        }
        register("androidFeature") {
            id = libs.plugins.escapelauncher.android.feature.get().pluginId
            implementationClass = "com.geecee.escapelauncher.AndroidFeatureConventionPlugin"
        }
        register("androidGoogle") {
            id = libs.plugins.escapelauncher.android.google.get().pluginId
            implementationClass = "com.geecee.escapelauncher.AndroidGoogleConventionPlugin"
        }
    }
}

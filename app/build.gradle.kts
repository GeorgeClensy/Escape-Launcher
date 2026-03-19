plugins {
    id("escapelauncher.android.application")
    id("escapelauncher.android.compose")
    id("escapelauncher.android.hilt")
    id("escapelauncher.android.flavours")
    id("escapelauncher.android.testing")
    id("escapelauncher.android.room")
    alias(libs.plugins.compose.compiler)
}

val baseVersionCode = "2.3.1"

android {
    namespace = "com.geecee.escapelauncher"

    defaultConfig {
        applicationId = "com.geecee.escapelauncher"
        targetSdk = 36
        versionCode = 2
        versionName = baseVersionCode
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        resValue("string", "app_version", baseVersionCode)
        resValue("string", "app_name", "Escape Launcher")
        resValue("string", "app_flavour", "Unknown Flavor")
        resValue("string", "empty", "")
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "Escape Launcher Dev")
        }
        release {
            resValue("string", "app_name", "Escape Launcher")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "../proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.splashscreen)

    // Material Design and UI Libraries
    implementation(libs.androidx.compose.material3)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)


    // Lifecycle and Activity Libraries
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // JSON Parsing
    implementation(libs.gson)

    // Testing Libraries

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debugging Tools
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Modules
    implementation(project(":core:ui"))
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":feature:homescreen"))
    implementation(project(":feature:workapps"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
}

tasks.register("testClasses") {
    group = "verification"
    description = "Test claasses for all variants."
    dependsOn(
        tasks.matching { it.name.startsWith("compile") && it.name.endsWith("UnitTestSources") } )}

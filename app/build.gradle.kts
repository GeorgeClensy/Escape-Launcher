plugins {
    id("escapelauncher.android.application")
    id("escapelauncher.android.compose")
    id("escapelauncher.android.compose.ui")
    id("escapelauncher.android.hilt")
    id("escapelauncher.android.flavours")
    id("escapelauncher.android.testing")
    id("escapelauncher.android.room")
}

val baseVersionCode = "2.4"

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
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.graphics.shapes)


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
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debugging Tools
    debugImplementation(libs.androidx.ui.test.manifest)

    // Modules
    implementation(project(":core:ui"))
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":feature:homescreen"))
    implementation(project(":feature:workapps"))

    implementation(libs.androidx.hilt.navigation.compose)
}
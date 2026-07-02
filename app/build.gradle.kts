plugins {
    alias(libs.plugins.escapelauncher.android.application)
    alias(libs.plugins.escapelauncher.android.compose)
    alias(libs.plugins.escapelauncher.android.composeui)
    alias(libs.plugins.escapelauncher.android.hilt)
    alias(libs.plugins.escapelauncher.android.flavours)
    alias(libs.plugins.escapelauncher.android.testing)
    alias(libs.plugins.escapelauncher.android.room)
    alias(libs.plugins.kotlin.serialization)
}

val baseVersionCode = "3.0"

android {
    namespace = "com.geecee.escapelauncher"

    defaultConfig {
        applicationId = "com.geecee.escapelauncher"
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 3
        versionName = baseVersionCode
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        resValue("string", "app_version", baseVersionCode)
        resValue("string", "app_name", "Escape Launcher")
        resValue("string", "app_flavour", "Unknown Flavor")

        buildConfigField("String", "APP_VERSION", "\"$baseVersionCode\"")
        buildConfigField("String", "APP_NAME", "\"Escape Launcher\"")
        buildConfigField("String", "APP_FLAVOUR", "\"Unknown Flavor\"")
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "Escape Launcher Dev")
            buildConfigField("String", "APP_NAME", "\"Escape Launcher Dev\"")
        }
        release {
            resValue("string", "app_name", "Escape Launcher")
            isMinifyEnabled = true
            isShrinkResources = true
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
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.json)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // JSON Parsing
    implementation(libs.gson)

    // Testing Libraries
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debugging Tools
    debugImplementation(libs.androidx.ui.test.manifest)

    // Modules
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:theme"))
    implementation(project(":core:analytics"))
    implementation(project(":core:cloudmessaging"))
    implementation(project(":feature:homescreen"))
    implementation(project(":feature:workapps"))
    implementation(project(":feature:privatespace"))
    implementation(project(":feature:securefolder"))
    implementation(project(":feature:weather"))
    implementation(project(":feature:screentime"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:appslist"))
    implementation(project(":feature:newwidgets"))
    implementation(project(":feature:onboarding"))

    implementation(libs.androidx.hilt.navigation.compose)
}
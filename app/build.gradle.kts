import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.geecee.escapelauncher"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.geecee.escapelauncher"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "2.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    flavorDimensions += listOf("version", "distribution")
    productFlavors{
        create("dev"){
            applicationIdSuffix = ".dev"
            dimension = "version"
            versionNameSuffix = "-dev"
        }
        create("prod"){
            dimension = "version"
            applicationIdSuffix = ""
        }
        create("google") {
            dimension = "distribution"
            buildConfigField("boolean", "IS_FOSS", "false")
        }
        create("foss") {
            dimension = "distribution"
            versionNameSuffix = "-foss"
            buildConfigField("boolean", "IS_FOSS", "true")
        }
    }

    sourceSets {
        getByName("foss") {
            res.srcDirs("src/foss/res")
            java.srcDirs("src/foss/java")
        }
        getByName("google") {
            res.srcDirs("src/google/res")
            java.srcDirs("src/google/java")
        }
    }
    
    androidComponents.beforeVariants { variantBuilder ->
        val flavorVersion = variantBuilder.productFlavors.find { it.first == "version" }?.second
        val buildType = variantBuilder.buildType

        if ((flavorVersion == "prod" && buildType == "debug") ||
            (flavorVersion == "dev" && buildType == "release")) {
            variantBuilder.enable = false
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
            freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    @Suppress("UnstableApiUsage")
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Apply Google-specific configurations from secondary file
val taskNames = gradle.startParameter.taskNames
val isFoss = taskNames.any { it.contains("foss", ignoreCase = true) }
if (!isFoss) {
    apply(from = "google.gradle")
}

dependencies {
    // Core Android Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.splashscreen)

    // Material Design and UI Libraries
    implementation(libs.material)
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

    // Room Database
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // JSON Parsing
    implementation(libs.gson)

    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debugging Tools
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

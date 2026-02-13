import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp")
}

val baseVersionCode = "2.3.1"

android {
    namespace = "com.geecee.escapelauncher"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.geecee.escapelauncher"
        minSdk = 26
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
            resValue("string", "app_name", "Escape Launcher Dev")
        }
        create("prod"){
            dimension = "version"
            applicationIdSuffix = ""
        }
        create("google") {
            dimension = "distribution"
            buildConfigField("boolean", "IS_FOSS", "false")
            resValue("string", "app_flavour", "Google API")
        }
        create("foss") {
            dimension = "distribution"
            versionNameSuffix = "-foss"
            buildConfigField("boolean", "IS_FOSS", "true")
            resValue("string", "app_flavour", "FOSS")
        }
    }

    sourceSets {
        getByName("foss") {
            res.directories.add("src/foss/res")
            java.directories.add("src/foss/java")
        }
        getByName("google") {
            res.directories.add("src/google/res")
            java.directories.add("src/google/java")
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

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
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

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

// Apply Google-specific configurations from secondary file
val taskNames: List<String?>? = gradle.startParameter.taskNames
val isFoss = taskNames?.any { it?.contains("foss", ignoreCase = true) ?: false  }
if (!isFoss!!) {
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

    // Modules
    implementation(project(":core:ui"))
    implementation(project(":core:model"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.register("testClasses") {
    group = "verification"
    description = "Test claasses for all variants."
    dependsOn(
        tasks.matching { it.name.startsWith("compile") && it.name.endsWith("UnitTestSources") } )}

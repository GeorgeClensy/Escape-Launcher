plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.geecee.escapelauncher.core.ui"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    flavorDimensions += listOf("distribution")
    productFlavors {
        create("google") {
            dimension = "distribution"
            buildConfigField("boolean", "IS_FOSS", "false")
        }
        create("foss") {
            dimension = "distribution"
            buildConfigField("boolean", "IS_FOSS", "true")
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
}

// Get the current Gradle tasks
val taskNames: List<String> = gradle.startParameter.taskNames

// Determine if the build is a FOSS variant
val isFoss = taskNames.any { it.lowercase().contains("foss") }

// Apply fonts configuration only if not FOSS
if (!isFoss) {
    apply(from = "gfonts.gradle")
}

dependencies {
    // Add the compose BOM
    val bom = platform(libs.androidx.compose.bom)
    implementation(bom)
    androidTestImplementation(bom)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    // Test stuff
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
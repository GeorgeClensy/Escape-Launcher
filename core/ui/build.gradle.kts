plugins {
    id("escapelauncher.android.library")
    id("escapelauncher.android.compose")
}

android {
    namespace = "com.geecee.escapelauncher.core.ui"

    buildFeatures {
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

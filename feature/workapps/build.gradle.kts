plugins {
    id("escapelauncher.android.library")
    id("escapelauncher.android.compose")
    id("escapelauncher.android.hilt")
}

android {
    namespace = "com.geecee.escapelauncher.feature.workapps"

    defaultConfig {
        // Specify which flavor to use from :core:ui since this module is flavorless
        missingDimensionStrategy("distribution", "foss")
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:common"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.hilt.navigation.compose)

    // Testing stuff
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

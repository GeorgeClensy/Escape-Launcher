plugins {
    id("escapelauncher.android.library")
    id("escapelauncher.android.compose")
    id("escapelauncher.android.hilt")
    id("escapelauncher.android.testing")
}

android {
    namespace = "com.geecee.escapelauncher.feature.workapps"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:common"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.hilt.navigation.compose)
}

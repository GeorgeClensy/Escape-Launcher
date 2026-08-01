plugins {
    alias(libs.plugins.escapelauncher.android.library)
    alias(libs.plugins.escapelauncher.android.compose)
    alias(libs.plugins.escapelauncher.android.composeui)
    alias(libs.plugins.escapelauncher.android.hilt)
    alias(libs.plugins.escapelauncher.android.testing)
}

android {
    namespace = "com.geecee.escapelauncher.core.theme"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.google.android.material)
    implementation(project(":core:domain"))
}

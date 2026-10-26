plugins {
    alias(libs.plugins.escapelauncher.android.library)
    alias(libs.plugins.escapelauncher.android.compose)
    alias(libs.plugins.escapelauncher.android.composeui)
    alias(libs.plugins.escapelauncher.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.geecee.escapelauncher.core.navigation"
}

dependencies {
    implementation(libs.bundles.navigation3)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}

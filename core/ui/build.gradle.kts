plugins {
    alias(libs.plugins.escapelauncher.android.library)
    alias(libs.plugins.escapelauncher.android.compose)
    alias(libs.plugins.escapelauncher.android.composeui)
    alias(libs.plugins.escapelauncher.android.testing)
}

android {
    namespace = "com.geecee.escapelauncher.core.ui"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(project(":core:theme"))
    implementation(project(":core:model"))
}

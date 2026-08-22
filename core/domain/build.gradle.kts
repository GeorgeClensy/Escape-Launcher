plugins {
    alias(libs.plugins.escapelauncher.android.library)
    alias(libs.plugins.escapelauncher.android.hilt)
    alias(libs.plugins.escapelauncher.android.testing)
}

android {
    namespace = "com.geecee.escapelauncher.core.domain"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:analytics"))
}
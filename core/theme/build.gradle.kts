plugins {
    id("escapelauncher.android.library")
    id("escapelauncher.android.compose")
    id("escapelauncher.android.compose.ui")
    id("escapelauncher.android.hilt")
    id("escapelauncher.android.flavours")
    id("escapelauncher.android.testing")
}

android {
    namespace = "com.geecee.escapelauncher.core.theme"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(project(":core:domain"))
    implementation(libs.androidx.hilt.navigation.compose)
}

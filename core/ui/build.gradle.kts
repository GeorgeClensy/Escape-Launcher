plugins {
    id("escapelauncher.android.library")
    id("escapelauncher.android.compose")
    id("escapelauncher.android.compose.ui")
    id("escapelauncher.android.flavours")
    id("escapelauncher.android.testing")
}

android {
    namespace = "com.geecee.escapelauncher.core.ui"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(project(":core:theme"))
}

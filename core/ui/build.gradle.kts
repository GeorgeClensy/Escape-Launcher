plugins {
    id("escapelauncher.android.library")
    id("escapelauncher.android.compose")
    id("escapelauncher.android.flavours")
    id("escapelauncher.android.testing")
    id("escapelauncher.android.compose.ui")
}

android {
    namespace = "com.geecee.escapelauncher.core.ui"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
}

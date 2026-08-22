plugins {
    alias(libs.plugins.escapelauncher.android.feature)
}

android {
    namespace = "com.geecee.escapelauncher.feature.weather"
}

dependencies {
    implementation(project(":core:domain"))
}

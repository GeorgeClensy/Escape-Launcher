plugins {
    alias(libs.plugins.escapelauncher.android.feature)
    alias(libs.plugins.escapelauncher.android.flavours)
}

android {
    namespace = "com.geecee.escapelauncher.feature.weather"
}

dependencies {
    implementation(project(":core:domain"))
}

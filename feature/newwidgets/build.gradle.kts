plugins {
    alias(libs.plugins.escapelauncher.android.feature)
}

android {
    namespace = "com.geecee.escapelauncher.feature.newwidgets"
}

dependencies {
    implementation(project(":core:theme"))
    implementation(project(":core:analytics"))
}
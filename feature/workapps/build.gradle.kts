plugins {
    id("escapelauncher.android.feature")
}

android {
    namespace = "com.geecee.escapelauncher.feature.workapps"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:common"))

    implementation(libs.androidx.hilt.navigation.compose)
}

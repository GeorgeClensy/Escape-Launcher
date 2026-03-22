plugins {
    id("escapelauncher.android.feature")
}

android {
    namespace = "com.geecee.escapelauncher.feature.workapps"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:common"))
}

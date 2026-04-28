plugins {
    id("escapelauncher.android.feature")
}

android {
    namespace = "com.geecee.escapelauncher.feature.securefolder"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:common"))
    implementation(project(":core:theme"))
}

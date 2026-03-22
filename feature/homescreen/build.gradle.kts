plugins {
    id("escapelauncher.android.feature")
}

android {
    namespace = "com.geecee.escapelauncher.feature.homescreen"
}

dependencies {
    implementation(project(":domain:time"))

    implementation(libs.androidx.appcompat)
}

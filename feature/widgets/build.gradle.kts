plugins {
    id("escapelauncher.android.feature")
}

android {
    namespace = "com.geecee.escapelauncher.feature.widgets"
}

dependencies {
    implementation(project(":core:theme"))
    implementation(project(":core:analytics"))
}

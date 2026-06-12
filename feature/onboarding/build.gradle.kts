plugins {
    id("escapelauncher.android.feature")
}
android {
    namespace = "com.geecee.escapelauncher.feature.onboarding"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":core:theme"))
    implementation(project(":core:model"))
    implementation(project(":core:analytics"))

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
}

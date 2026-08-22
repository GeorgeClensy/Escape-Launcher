plugins {
    alias(libs.plugins.escapelauncher.android.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.geecee.escapelauncher.feature.settings"
}

dependencies {
    implementation(project(":feature:newwidgets"))
    implementation(project(":feature:screentime"))
    implementation(project(":feature:weather"))
    implementation(project(":core:ui"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(project(":core:theme"))

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.json)
}
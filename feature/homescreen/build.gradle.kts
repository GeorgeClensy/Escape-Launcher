plugins {
    alias(libs.plugins.escapelauncher.android.feature)
    alias(libs.plugins.escapelauncher.android.compose)
    alias(libs.plugins.escapelauncher.android.composeui)
}

android {
    namespace = "com.geecee.escapelauncher.feature.homescreen"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":core:model"))
    implementation(project(":core:analytics"))
    implementation(project(":domain:time"))
    implementation(project(":feature:newwidgets"))
    implementation(project(":feature:screentime"))
    implementation(project(":feature:weather"))

    implementation(libs.androidx.appcompat)
}

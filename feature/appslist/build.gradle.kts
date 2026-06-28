plugins {
    alias(libs.plugins.escapelauncher.android.feature)
}

android {
    namespace = "com.geecee.escapelauncher.appslist"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:theme"))
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":feature:screentime"))
}
plugins {
    id("escapelauncher.android.library")
    id("escapelauncher.android.testing")
}

android {
    namespace = "com.geecee.escapelauncher.core.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)

    implementation(project(":core:model"))
}
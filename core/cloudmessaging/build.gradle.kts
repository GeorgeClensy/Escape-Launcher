plugins {
    alias(libs.plugins.escapelauncher.android.library)
}

android {
    namespace = "com.geecee.escapelauncher.core.cloudmessaging"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
}

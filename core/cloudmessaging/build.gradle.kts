plugins {
    id("escapelauncher.android.library")
    id("escapelauncher.android.flavours")
}

android {
    namespace = "com.geecee.escapelauncher.core.cloudmessaging"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
}

plugins {
    id("escapelauncher.android.library")
    id("escapelauncher.android.hilt")
    id("escapelauncher.android.testing")
    id("escapelauncher.android.room")
}

android {
    namespace = "com.geecee.escapelauncher.core.data"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

}

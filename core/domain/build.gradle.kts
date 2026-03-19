plugins {
    id("escapelauncher.android.library")
    id("escapelauncher.android.hilt")
    id("escapelauncher.android.testing")
}

android {
    namespace = "com.geecee.escapelauncher.domain"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

}

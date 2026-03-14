plugins {
    id("escapelauncher.android.library")
    id("escapelauncher.android.hilt")
}

android {
    namespace = "com.geecee.escapelauncher.core.data"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

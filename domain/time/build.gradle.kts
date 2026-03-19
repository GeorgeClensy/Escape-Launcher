plugins {
    id("escapelauncher.android.library")
    id("escapelauncher.android.hilt")
    id("escapelauncher.android.testing")
}

android {
    namespace = "com.geecee.escapelauncher.domain.time"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

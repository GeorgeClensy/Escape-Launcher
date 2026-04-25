plugins {
    id("escapelauncher.android.library")
    id("escapelauncher.android.hilt")
    id("escapelauncher.android.testing")
    id("escapelauncher.android.room")
}

android {
    namespace = "com.geecee.escapelauncher.core.data"
}

//Add DataStore deps, for know I'm gonna leave this out of a convention just because it's only gonna
//be needed in this module
dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:di"))

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.common)
    ksp(libs.hilt.compiler)
}
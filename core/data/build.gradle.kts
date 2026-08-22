plugins {
    alias(libs.plugins.escapelauncher.android.library)
    alias(libs.plugins.escapelauncher.android.hilt)
    alias(libs.plugins.escapelauncher.android.testing)
    alias(libs.plugins.escapelauncher.android.room)
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
    implementation(project(":core:common"))

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.common)
    ksp(libs.hilt.compiler)
}
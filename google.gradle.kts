dependencies {
    // Google version uses Google fonts, otherwise bundle fonts
    add("googleImplementation", "androidx.compose.ui:ui-text-google-fonts:1.10.5")

    // These plugins are only meant to be on an :app module so we just check if the thing its being applied to has android.application or android.library
    if (project.plugins.hasPlugin("com.android.application") || project.plugins.hasPlugin("com.android.library")) {
        add("googleImplementation", platform("com.google.fire" + "base:fire" + "base-bom:34.8.0"))
        add("googleImplementation", "com.google.fire" + "base:fire" + "base-analytics")
        add("googleImplementation", "com.google.fire" + "base:fire" + "base-crash" + "lytics")
        add("googleImplementation", "com.google.fire" + "base:fire" + "base-perf")
        add("googleImplementation", "com.google.fire" + "base:fire" + "base-messaging:25.0.1")
        add("googleImplementation", "com.google.android.gms:play-services-location:21.3.0")
        add("googleImplementation", "com.squareup.okhttp3:okhttp:5.3.2")
    }
    println(">>> [Google.gradle] Imported the google deps because this is a google build. (Or it could be a sync, for some reason its fine that if the google stuff is in a foss sync cuz its not in the build.)")
}

// This is the stuff that should only be added to the :app module so we check it first
// It also checks if we are in the Google variant before adding it
// We allow it if it's an IDE sync also because android studio gives syntax errors and this is just the easiest way to combat that
val taskNames = gradle.startParameter.taskNames
val isGoogleVariant = taskNames.any { it.lowercase().contains("google") }
val isIdeSync = System.getProperty("idea.sync.active") == "true"
val isApp = project.plugins.hasPlugin("com.android.application")
if ((isGoogleVariant || isIdeSync) && isApp) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}
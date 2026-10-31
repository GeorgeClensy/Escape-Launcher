package com.geecee.escapelauncher.core.domain.repository

/**
 * Read-only, app-wide configuration values exposed to the domain layer.
 *
 * Implemented at the application boundary so use cases can depend on plain values
 * instead of the Android framework, which keeps them unit-testable on the JVM.
 */
interface AppConfiguration {
    /** Whether the current build is the FOSS (analytics-free) flavour. */
    val isFoss: Boolean

    /** The application version name, for example `3.0.1`. */
    val appVersion: String

    /** The application name. */
    val appName: String

    /** The build flavour identifier, for example `foss` or `google`. */
    val appFlavour: String

    /**
     * The Android SDK level of the device, captured once when the configuration is created.
     *
     * Mirrors [android.os.Build.VERSION.SDK_INT], but lets use cases be tested without
     * stubbing static framework state.
     */
    val sdkInt: Int
}

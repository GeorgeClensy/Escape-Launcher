package com.geecee.escapelauncher.core.common

import android.os.Process
import com.geecee.escapelauncher.core.model.InstalledApp

/**
 * Checks if the app belongs to the main user.
 */
fun InstalledApp.isMainUserApp(): Boolean = this.user == Process.myUserHandle()

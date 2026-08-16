package com.geecee.escapelauncher.core.domain.repository

interface AppConfiguration {
    val isFoss: Boolean
    val appVersion: String
    val appName: String
    val appFlavour: String
}
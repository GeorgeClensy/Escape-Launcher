package com.geecee.escapelauncher.feature.screentime

data class AppUsageUiModel(
    val packageName: String,
    val appName: String,
    val totalTime: Long,
    val usageIncreased: Boolean
)

package com.geecee.escapelauncher.core.data.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "app_usage",
    primaryKeys = ["packageName", "date"],
    indices = [Index(value = ["date"])]
)
data class AppUsageEntity(
    val packageName: String,
    val date: String,
    val totalTime: Long
)

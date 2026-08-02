package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val userEmail: String? = null,
    val displayName: String? = null,
    val colorTheme: String = "SOUL_PINK",
    val darkMode: Boolean = false,
    val periodTrackerEnabled: Boolean = true
)

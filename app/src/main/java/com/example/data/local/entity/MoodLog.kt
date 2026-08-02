package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_logs")
data class MoodLog(
    @PrimaryKey val id: String,
    val date: String,
    val mood: String,
    val createdAt: Long
)

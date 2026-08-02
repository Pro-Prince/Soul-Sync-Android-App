package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey val id: String,
    val date: String,
    val title: String?,
    val content: String,
    val contentPlain: String = "",
    val mood: String,
    val voiceNotePath: String?,
    val imageUris: String?,
    val videoPath: String? = null,
    val hashtags: String?,
    val aiSummary: String?,
    val aiPattern: String?,
    val aiNextStep: String?,
    val createdAt: Long,
    val updatedAt: Long
)

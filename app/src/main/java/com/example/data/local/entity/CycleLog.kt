package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycle_logs")
data class CycleLog(
    @PrimaryKey val id: String,
    val date: String,
    val flow: String?,
    val painLevel: Int?,
    val mood: String?,
    val symptoms: String?,
    val emotionalScore: Int?,
    val stressScore: Int?,
    val supportedScore: Int?,
    val anxietyScore: Int?,
    val lovedScore: Int?,
    val confidenceScore: Int?,
    val energyScore: Int?,
    val waterMl: Int?,
    val exerciseMin: Int?,
    val medication: String?,
    val notes: String?,
    val createdAt: Long
)

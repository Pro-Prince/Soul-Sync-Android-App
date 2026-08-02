package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycle_periods")
data class CyclePeriod(
    @PrimaryKey val id: String,
    val startDate: String,
    val endDate: String?,
    val averageCycleLength: Int = 28
)

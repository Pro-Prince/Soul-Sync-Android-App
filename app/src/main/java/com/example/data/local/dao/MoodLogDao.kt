package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.MoodLog
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(moodLog: MoodLog)

    @Query("SELECT * FROM mood_logs ORDER BY date DESC")
    fun getAllOrderedByDate(): Flow<List<MoodLog>>

    @Query("SELECT * FROM mood_logs WHERE date >= date('now', '-14 days') ORDER BY date DESC")
    fun getLast14Days(): Flow<List<MoodLog>>

    @Query("SELECT mood, COUNT(*) as count FROM mood_logs GROUP BY mood ORDER BY count DESC")
    fun getMoodCounts(): Flow<List<MoodCount>>

    @Query("SELECT mood, COUNT(*) as count FROM mood_logs WHERE date >= :cutoff GROUP BY mood")
    fun getMoodCountsSince(cutoff: String): Flow<List<MoodCount>>
}

data class MoodCount(
    val mood: String,
    val count: Int
)

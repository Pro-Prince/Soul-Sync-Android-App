package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.CyclePeriod
import kotlinx.coroutines.flow.Flow

@Dao
interface CyclePeriodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(period: CyclePeriod)

    @Update
    suspend fun update(period: CyclePeriod)

    @Query("SELECT * FROM cycle_periods ORDER BY startDate DESC")
    fun getAll(): Flow<List<CyclePeriod>>

    @Query("SELECT * FROM cycle_periods ORDER BY startDate DESC LIMIT 1")
    fun getLatest(): Flow<CyclePeriod?>
}

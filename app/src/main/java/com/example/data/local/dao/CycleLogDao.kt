package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.CycleLog
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: CycleLog)

    @Update
    suspend fun update(log: CycleLog)

    @Delete
    suspend fun delete(log: CycleLog)

    @Query("SELECT * FROM cycle_logs WHERE date = :date LIMIT 1")
    fun getByDate(date: String): Flow<CycleLog?>

    @Query("SELECT * FROM cycle_logs WHERE date = :date LIMIT 1")
    suspend fun getByDateSuspend(date: String): CycleLog?

    @Query("SELECT * FROM cycle_logs ORDER BY date DESC")
    fun getAll(): Flow<List<CycleLog>>
}

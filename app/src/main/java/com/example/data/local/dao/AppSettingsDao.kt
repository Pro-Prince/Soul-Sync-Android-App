package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.AppSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(settings: AppSettings)

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun get(): Flow<AppSettings?>
}

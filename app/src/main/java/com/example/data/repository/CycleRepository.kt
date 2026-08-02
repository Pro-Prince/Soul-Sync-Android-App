package com.example.data.repository

import com.example.data.SettingsRepository
import com.example.data.local.dao.CycleLogDao
import com.example.data.local.dao.CyclePeriodDao
import com.example.data.local.entity.CycleLog
import com.example.data.local.entity.CyclePeriod
import com.example.data.sync.SupabaseSyncHelper
import com.example.data.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CycleRepository(
    private val logDao: CycleLogDao,
    private val periodDao: CyclePeriodDao,
    private val settingsRepository: SettingsRepository
) {
    fun getAllLogs() = logDao.getAll()
    fun getLogByDate(date: String) = logDao.getByDate(date)
    suspend fun getLogByDateSuspend(date: String) = withContext(Dispatchers.IO) { logDao.getByDateSuspend(date) }
    
    private suspend fun getActiveUserId(): String {
        try {
            val supabaseId = SupabaseClient.auth.currentUserOrNull()?.id
            if (!supabaseId.isNullOrBlank()) return supabaseId
        } catch (e: Exception) {
            // fallback
        }
        return settingsRepository.getUserId() ?: ""
    }

    suspend fun insertLog(log: CycleLog) = withContext(Dispatchers.IO) {
        logDao.insert(log)
        val userId = getActiveUserId()
        if (userId.isNotBlank()) {
            SupabaseSyncHelper.syncCycleLog(userId, log)
        }
    }
    
    fun getAllPeriods() = periodDao.getAll()
    fun getLatestPeriod() = periodDao.getLatest()
    
    suspend fun insertPeriod(period: CyclePeriod) = withContext(Dispatchers.IO) {
        periodDao.insert(period)
        val userId = getActiveUserId()
        if (userId.isNotBlank()) {
            SupabaseSyncHelper.syncCyclePeriod(userId, period)
        }
    }
}

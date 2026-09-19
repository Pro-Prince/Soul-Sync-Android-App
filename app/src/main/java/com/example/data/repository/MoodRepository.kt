package com.example.data.repository

import com.example.data.SettingsRepository
import com.example.data.local.dao.MoodLogDao
import com.example.data.local.entity.MoodLog
import com.example.data.sync.SupabaseSyncHelper
import com.example.data.supabase.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MoodRepository(
    private val dao: MoodLogDao,
    private val settingsRepository: SettingsRepository
) {
    private val syncScope = CoroutineScope(Dispatchers.IO)
    fun getAll() = dao.getAllOrderedByDate()
    fun getLast14Days() = dao.getLast14Days()
    fun getMoodCounts() = dao.getMoodCounts()
    fun getMoodCountsSince(cutoff: String) = dao.getMoodCountsSince(cutoff)

    private suspend fun getActiveUserId(): String {
        try {
            val supabaseId = SupabaseClient.auth.currentUserOrNull()?.id
            if (!supabaseId.isNullOrBlank()) return supabaseId
        } catch (e: Exception) {
            // fallback
        }
        return settingsRepository.getUserId() ?: ""
    }

    suspend fun cleanUpDuplicates() = withContext(Dispatchers.IO) {
        try {
            dao.deleteDuplicates()
        } catch (e: Exception) {
            android.util.Log.e("MoodRepository", "Error cleaning up duplicate mood logs", e)
        }
    }

    suspend fun insert(log: MoodLog) = withContext(Dispatchers.IO) {
        dao.insert(log)
        syncScope.launch {
            val userId = getActiveUserId()
            if (userId.isNotBlank()) {
                try {
                    SupabaseSyncHelper.syncMoodLog(userId, log)
                } catch (e: Exception) {
                    android.util.Log.e("MoodRepository", "Background Supabase mood sync failed for ${log.id}", e)
                }
            }
        }
    }
}

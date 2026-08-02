package com.example.data.repository

import com.example.data.SettingsRepository
import com.example.data.local.dao.DiaryEntryDao
import com.example.data.local.entity.DiaryEntry
import com.example.data.sync.SupabaseSyncHelper
import com.example.data.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DiaryRepository(
    private val dao: DiaryEntryDao,
    private val settingsRepository: SettingsRepository
) {
    fun getAll() = dao.getAllOrderedByDate()
    fun getByDateRange(start: String, end: String) = dao.getByDateRange(start, end)
    fun getThisWeekCount(weekStart: String) = dao.getThisWeekCount(weekStart)
    fun getMoodCountsSince(cutoff: String) = dao.getMoodCountsSince(cutoff)

    suspend fun getById(id: String) = withContext(Dispatchers.IO) { dao.getById(id) }
    fun getByIdFlow(id: String): Flow<DiaryEntry?> = dao.getByIdFlow(id)
    suspend fun getStreakEntries() = withContext(Dispatchers.IO) { dao.getStreakEntries() }
    
    private suspend fun getActiveUserId(): String {
        try {
            val supabaseId = SupabaseClient.auth.currentUserOrNull()?.id
            if (!supabaseId.isNullOrBlank()) return supabaseId
        } catch (e: Exception) {
            // fallback
        }
        return settingsRepository.getUserId() ?: ""
    }

    suspend fun insert(entry: DiaryEntry) = withContext(Dispatchers.IO) {
        dao.insert(entry)
        val userId = getActiveUserId()
        if (userId.isNotBlank()) {
            SupabaseSyncHelper.syncDiaryEntry(userId, entry)
        }
    }

    suspend fun delete(entry: DiaryEntry) = withContext(Dispatchers.IO) {
        dao.delete(entry)
        val userId = getActiveUserId()
        if (userId.isNotBlank()) {
            SupabaseSyncHelper.deleteDiaryEntry(userId, entry.id)
        }
    }
}

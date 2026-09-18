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

    suspend fun cleanUpDuplicates() = withContext(Dispatchers.IO) {
        try {
            dao.deleteDuplicates()
        } catch (e: Exception) {
            android.util.Log.e("DiaryRepository", "Error cleaning up duplicate entries", e)
        }
    }

    suspend fun insert(entry: DiaryEntry): DiaryEntry = withContext(Dispatchers.IO) {
        // Prevent duplicate creation if identical entry already exists
        val duplicate = dao.findDuplicate(entry.date, entry.title, entry.contentPlain)
        val finalEntry = if (duplicate != null && duplicate.id != entry.id) {
            entry.copy(id = duplicate.id, createdAt = duplicate.createdAt)
        } else {
            entry
        }

        // 1. Authoritative local database save - guarantees instant save and zero data loss
        dao.insert(finalEntry)

        // 2. Safe background sync to Supabase - network failures will never fail local save
        val userId = getActiveUserId()
        if (userId.isNotBlank()) {
            try {
                SupabaseSyncHelper.syncDiaryEntry(userId, finalEntry)
            } catch (e: Exception) {
                android.util.Log.e("DiaryRepository", "Background Supabase sync failed for ${finalEntry.id}", e)
            }
        }
        finalEntry
    }

    suspend fun delete(entry: DiaryEntry) = withContext(Dispatchers.IO) {
        dao.delete(entry)
        val userId = getActiveUserId()
        if (userId.isNotBlank()) {
            try {
                SupabaseSyncHelper.deleteDiaryEntry(userId, entry.id)
            } catch (e: Exception) {
                android.util.Log.e("DiaryRepository", "Background Supabase delete failed for ${entry.id}", e)
            }
        }
    }
}

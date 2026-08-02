package com.example.data.repository

import com.example.data.SettingsRepository
import com.example.data.local.dao.AchievementDao
import com.example.data.local.entity.Achievement
import com.example.data.sync.SupabaseSyncHelper
import com.example.data.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AchievementRepository(
    private val dao: AchievementDao,
    private val settingsRepository: SettingsRepository
) {

    private val _newlyUnlocked = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val newlyUnlocked = _newlyUnlocked.asSharedFlow()

    fun getAll() = dao.getAll()
    fun getUnlocked() = dao.getUnlocked()
    suspend fun getById(id: String) = dao.getById(id)
    
    private suspend fun getActiveUserId(): String {
        try {
            val supabaseId = SupabaseClient.auth.currentUserOrNull()?.id
            if (!supabaseId.isNullOrBlank()) return supabaseId
        } catch (e: Exception) {
            // fallback
        }
        return settingsRepository.getUserId() ?: ""
    }

    suspend fun unlock(id: String) {
        val current = dao.getById(id)
        if (current == null || current.unlockedAt == null) {
            val achievement = Achievement(id, System.currentTimeMillis())
            dao.insertOrReplace(achievement)
            _newlyUnlocked.tryEmit(id)
            val userId = getActiveUserId()
            if (userId.isNotBlank()) {
                SupabaseSyncHelper.syncAchievement(userId, achievement)
            }
        }
    }
    suspend fun initDefaultAchievements() {
        val targets = listOf(
            "first_entry", "streak_3", "streak_7", "streak_14",
            "emotional_awareness", "pattern_breaker", "first_image",
            "first_voice", "sticker_user", "theme_explorer", 
            "reflection_starter", "deep_reflection"
        )
        targets.forEach { id ->
            if (dao.getById(id) == null) {
                dao.insertOrReplace(Achievement(id, null))
            }
        }
    }
}

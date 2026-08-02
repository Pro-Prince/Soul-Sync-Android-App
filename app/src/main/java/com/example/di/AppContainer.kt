package com.example.di

import android.content.Context
import com.example.data.SettingsRepository
import com.example.data.dataStore
import com.example.data.NotificationPreferencesRepository
import com.example.data.local.SoulSyncDatabase
import com.example.data.repository.*
import com.example.notification.NotificationManager
import com.example.notification.NotificationScheduler
import kotlinx.coroutines.runBlocking

interface AppContainer {
    val settingsRepository: SettingsRepository
    val diaryRepository: DiaryRepository
    val moodRepository: MoodRepository
    val cycleRepository: CycleRepository
    val achievementRepository: AchievementRepository
    val authRepository: AuthRepository
    val notificationPreferencesRepository: NotificationPreferencesRepository
    val notificationManager: NotificationManager
    val notificationScheduler: NotificationScheduler
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val settingsRepository: SettingsRepository by lazy { SettingsRepository(context.dataStore) }

    private val database: SoulSyncDatabase by lazy { 
        val userId = runBlocking { 
            var id = settingsRepository.getUserId()
            val email = settingsRepository.getUserEmail()
            if (id == null) {
                try {
                    val currentSessionUser = com.example.data.supabase.SupabaseClient.auth.currentUserOrNull()
                    if (currentSessionUser != null) {
                        id = currentSessionUser.id
                        val currentEmail = currentSessionUser.email ?: email ?: ""
                        val name = currentSessionUser.userMetadata?.get("full_name")?.toString() ?: ""
                        settingsRepository.setUserInfo(id, currentEmail, name)
                        
                        if (currentEmail.isNotBlank()) {
                            SoulSyncDatabase.migrateDatabaseIfNecessary(context, currentEmail, id)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SoulSyncDatabase", "Failed to resolve Supabase session for migration", e)
                }
            } else if (email != null && email.isNotBlank()) {
                SoulSyncDatabase.migrateDatabaseIfNecessary(context, email, id)
            }
            id
        }
        SoulSyncDatabase.getDatabase(context, userId) 
    }
    override val diaryRepository: DiaryRepository by lazy { DiaryRepository(database.diaryEntryDao(), settingsRepository) }
    override val moodRepository: MoodRepository by lazy { MoodRepository(database.moodLogDao(), settingsRepository) }
    override val cycleRepository: CycleRepository by lazy { 
        CycleRepository(database.cycleLogDao(), database.cyclePeriodDao(), settingsRepository)
    }
    override val achievementRepository: AchievementRepository by lazy { AchievementRepository(database.achievementDao(), settingsRepository) }
    override val authRepository: AuthRepository by lazy { AuthRepository(context) }

    override val notificationPreferencesRepository: NotificationPreferencesRepository by lazy {
        NotificationPreferencesRepository(context.dataStore)
    }

    override val notificationManager: NotificationManager by lazy {
        NotificationManager(context, notificationPreferencesRepository)
    }

    override val notificationScheduler: NotificationScheduler by lazy {
        NotificationScheduler(context, notificationPreferencesRepository)
    }
}

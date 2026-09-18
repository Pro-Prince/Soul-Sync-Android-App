package com.example.data.sync

import android.util.Log
import com.example.data.local.SoulSyncDatabase
import com.example.data.local.entity.*
import com.example.data.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File
import kotlin.time.Duration.Companion.days

@Serializable
data class UserAccountDto(
    val id: String,
    val email: String,
    val password_key: String,
    val display_name: String
)

@Serializable
data class DiaryEntryDto(
    val id: String,
    val user_id: String,
    val date: String,
    val title: String?,
    val content: String,
    val content_plain: String,
    val mood: String,
    val voice_note_path: String?,
    val image_uris: String?,
    val video_path: String?,
    val hashtags: String?,
    val ai_summary: String?,
    val ai_pattern: String?,
    val ai_next_step: String?,
    val created_at: Long,
    val updated_at: Long
)

@Serializable
data class CyclePeriodDto(
    val id: String,
    val user_id: String,
    val start_date: String,
    val end_date: String?,
    val average_cycle_length: Int
)

@Serializable
data class CycleLogDto(
    val id: String,
    val user_id: String,
    val date: String,
    val flow: String?,
    val pain_level: Int?,
    val mood: String?,
    val symptoms: String?,
    val emotional_score: Int?,
    val stress_score: Int?,
    val supported_score: Int?,
    val anxiety_score: Int?,
    val loved_score: Int?,
    val confidence_score: Int?,
    val energy_score: Int?,
    val water_ml: Int?,
    val exercise_min: Int?,
    val medication: String?,
    val notes: String?,
    val created_at: Long
)

@Serializable
data class MoodLogDto(
    val id: String,
    val user_id: String,
    val date: String,
    val mood: String,
    val created_at: Long
)

@Serializable
data class AchievementDto(
    val id: String,
    val user_id: String,
    val unlocked_at: Long?
)

@Serializable
data class AppSettingsDto(
    val user_id: String,
    val color_theme: String,
    val dark_mode: Boolean,
    val period_tracker_enabled: Boolean
)

object SupabaseSyncHelper {
    private const val TAG = "SupabaseSyncHelper"

    private suspend fun uploadLocalFileToSupabase(userId: String, localPath: String, bucketName: String = "user-media"): String {
        if (localPath.startsWith("http://") || localPath.startsWith("https://")) {
            return localPath
        }
        val bytes: ByteArray = try {
            if (localPath.startsWith("content://")) {
                val uri = android.net.Uri.parse(localPath)
                com.example.SoulSyncApplication.instance.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return localPath
            } else {
                val file = File(localPath)
                if (!file.exists()) return localPath
                file.readBytes()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read local file: $localPath", e)
            return localPath
        }

        try {
            val extension = when {
                localPath.contains(".mp4") -> "mp4"
                localPath.contains(".m4a") -> "m4a"
                localPath.contains(".3gp") -> "3gp"
                localPath.contains(".png") -> "png"
                localPath.contains(".webp") -> "webp"
                localPath.contains(".jpg") || localPath.contains(".jpeg") -> "jpg"
                else -> "bin"
            }
            val sanitizedName = localPath.hashCode().toString().replace("-", "m")
            val fileName = "$userId/${System.currentTimeMillis()}_$sanitizedName.$extension"
            val bucket = SupabaseClient.client.storage[bucketName]
            
            bucket.upload(fileName, bytes, upsert = true)
            
            val remoteUrl = try {
                bucket.publicUrl(fileName)
            } catch (e: Exception) {
                bucket.createSignedUrl(fileName, expiresIn = 365.days)
            }
            Log.d(TAG, "Successfully uploaded media to Supabase storage: $fileName -> $remoteUrl")
            return remoteUrl
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload file to Supabase storage: $localPath", e)
            return localPath
        }
    }

    suspend fun syncUserAccount(user: UserAccount) = withContext(Dispatchers.IO) {
        try {
            val dto = UserAccountDto(
                id = user.id,
                email = user.email.trim().lowercase(),
                password_key = user.passwordKey,
                display_name = user.displayName
            )
            SupabaseClient.postgrest.from("user_accounts").upsert(dto)
            Log.d(TAG, "Successfully synced user account to Supabase: ${user.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing user account: ${user.id}", e)
        }
    }

    suspend fun syncDiaryEntry(userId: String, entry: DiaryEntry) = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext
        try {
            val remoteVoicePath = entry.voiceNotePath?.let { uploadLocalFileToSupabase(userId, it) }
            val remoteVideoPath = entry.videoPath?.let { uploadLocalFileToSupabase(userId, it) }
            val remoteImageUris = entry.imageUris?.split(",")?.map { uri ->
                if (uri.isNotBlank()) uploadLocalFileToSupabase(userId, uri.trim()) else uri
            }?.joinToString(",")

            val dto = DiaryEntryDto(
                id = entry.id,
                user_id = userId,
                date = entry.date,
                title = entry.title,
                content = entry.content,
                content_plain = entry.contentPlain,
                mood = entry.mood,
                voice_note_path = remoteVoicePath ?: entry.voiceNotePath,
                image_uris = remoteImageUris ?: entry.imageUris,
                video_path = remoteVideoPath ?: entry.videoPath,
                hashtags = entry.hashtags,
                ai_summary = entry.aiSummary,
                ai_pattern = entry.aiPattern,
                ai_next_step = entry.aiNextStep,
                created_at = entry.createdAt,
                updated_at = entry.updatedAt
            )
            SupabaseClient.postgrest.from("diary_entries").upsert(dto)
            Log.d(TAG, "Successfully synced diary entry to Supabase: ${entry.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing diary entry to Supabase: ${entry.id}", e)
        }
    }

    suspend fun deleteDiaryEntry(userId: String, entryId: String) = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext
        try {
            SupabaseClient.postgrest.from("diary_entries").delete {
                filter {
                    eq("id", entryId)
                    eq("user_id", userId)
                }
            }
            Log.d(TAG, "Successfully deleted diary entry from Supabase: $entryId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting diary entry from Supabase: $entryId", e)
        }
    }

    suspend fun syncMoodLog(userId: String, log: MoodLog) = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext
        try {
            val dto = MoodLogDto(
                id = log.id,
                user_id = userId,
                date = log.date,
                mood = log.mood,
                created_at = log.createdAt
            )
            SupabaseClient.postgrest.from("mood_logs").upsert(dto)
            Log.d(TAG, "Successfully synced mood log to Supabase: ${log.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing mood log to Supabase: ${log.id}", e)
        }
    }

    suspend fun syncCycleLog(userId: String, log: CycleLog) = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext
        try {
            val dto = CycleLogDto(
                id = log.id,
                user_id = userId,
                date = log.date,
                flow = log.flow,
                pain_level = log.painLevel,
                mood = log.mood,
                symptoms = log.symptoms,
                emotional_score = log.emotionalScore,
                stress_score = log.stressScore,
                supported_score = log.supportedScore,
                anxiety_score = log.anxietyScore,
                loved_score = log.lovedScore,
                confidence_score = log.confidenceScore,
                energy_score = log.energyScore,
                water_ml = log.waterMl,
                exercise_min = log.exerciseMin,
                medication = log.medication,
                notes = log.notes,
                created_at = log.createdAt
            )
            SupabaseClient.postgrest.from("cycle_logs").upsert(dto)
            Log.d(TAG, "Successfully synced cycle log to Supabase: ${log.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing cycle log to Supabase: ${log.id}", e)
        }
    }

    suspend fun syncCyclePeriod(userId: String, period: CyclePeriod) = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext
        try {
            val dto = CyclePeriodDto(
                id = period.id,
                user_id = userId,
                start_date = period.startDate,
                end_date = period.endDate,
                average_cycle_length = period.averageCycleLength
            )
            SupabaseClient.postgrest.from("cycle_periods").upsert(dto)
            Log.d(TAG, "Successfully synced cycle period to Supabase: ${period.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing cycle period to Supabase: ${period.id}", e)
        }
    }

    suspend fun syncAchievement(userId: String, achievement: Achievement) = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext
        try {
            val dto = AchievementDto(
                id = achievement.id,
                user_id = userId,
                unlocked_at = achievement.unlockedAt
            )
            SupabaseClient.postgrest.from("achievements").upsert(dto)
            Log.d(TAG, "Successfully synced achievement to Supabase: ${achievement.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing achievement to Supabase: ${achievement.id}", e)
        }
    }

    suspend fun pullAllFromSupabase(userId: String, db: SoulSyncDatabase) = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext
        Log.d(TAG, "Starting full database pull from Supabase for $userId")
        try {
            val diaryResult = SupabaseClient.postgrest.from("diary_entries")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<DiaryEntryDto>()
            val distinctDiaryDtos = diaryResult
                .distinctBy { it.id }
                .distinctBy { "${it.date}_${it.title?.trim().orEmpty()}_${it.content_plain.trim()}_${it.mood.uppercase()}" }
            for (dto in distinctDiaryDtos) {
                val entry = DiaryEntry(
                    id = dto.id,
                    date = dto.date,
                    title = dto.title,
                    content = dto.content,
                    contentPlain = dto.content_plain,
                    mood = dto.mood,
                    voiceNotePath = dto.voice_note_path,
                    imageUris = dto.image_uris,
                    videoPath = dto.video_path,
                    hashtags = dto.hashtags,
                    aiSummary = dto.ai_summary,
                    aiPattern = dto.ai_pattern,
                    aiNextStep = dto.ai_next_step,
                    createdAt = dto.created_at,
                    updatedAt = dto.updated_at
                )
                db.diaryEntryDao().insert(entry)
            }
            db.diaryEntryDao().deleteDuplicates()

            val moodResult = SupabaseClient.postgrest.from("mood_logs")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<MoodLogDto>()
            val distinctMoodDtos = moodResult
                .distinctBy { it.id }
                .distinctBy { "${it.date}_${it.mood.uppercase()}" }
            for (dto in distinctMoodDtos) {
                val log = MoodLog(
                    id = dto.id,
                    date = dto.date,
                    mood = dto.mood,
                    createdAt = dto.created_at
                )
                db.moodLogDao().insert(log)
            }
            db.moodLogDao().deleteDuplicates()

            val cycleResult = SupabaseClient.postgrest.from("cycle_logs")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<CycleLogDto>()
            for (dto in cycleResult) {
                val log = CycleLog(
                    id = dto.id,
                    date = dto.date,
                    flow = dto.flow,
                    painLevel = dto.pain_level,
                    mood = dto.mood,
                    symptoms = dto.symptoms,
                    emotionalScore = dto.emotional_score,
                    stressScore = dto.stress_score,
                    supportedScore = dto.supported_score,
                    anxietyScore = dto.anxiety_score,
                    lovedScore = dto.loved_score,
                    confidenceScore = dto.confidence_score,
                    energyScore = dto.energy_score,
                    waterMl = dto.water_ml,
                    exerciseMin = dto.exercise_min,
                    medication = dto.medication,
                    notes = dto.notes,
                    createdAt = dto.created_at
                )
                db.cycleLogDao().insert(log)
            }

            val periodResult = SupabaseClient.postgrest.from("cycle_periods")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<CyclePeriodDto>()
            for (dto in periodResult) {
                val period = CyclePeriod(
                    id = dto.id,
                    startDate = dto.start_date,
                    endDate = dto.end_date,
                    averageCycleLength = dto.average_cycle_length
                )
                db.cyclePeriodDao().insert(period)
            }

            val achResult = SupabaseClient.postgrest.from("achievements")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<AchievementDto>()
            for (dto in achResult) {
                val ach = Achievement(
                    id = dto.id,
                    unlockedAt = dto.unlocked_at
                )
                db.achievementDao().insertOrReplace(ach)
            }

            Log.d(TAG, "Full pull from Supabase completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error performing full Supabase pull", e)
        }
    }
}

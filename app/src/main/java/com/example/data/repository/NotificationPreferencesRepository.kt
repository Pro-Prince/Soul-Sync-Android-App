package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import android.util.Base64
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class NotificationPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val MASTER_ENABLED = booleanPreferencesKey("notif_master_enabled")
        val MORNING_MOOD_ENABLED = booleanPreferencesKey("notif_morning_mood_enabled")
        val MORNING_MOOD_TIME = stringPreferencesKey("notif_morning_mood_time")
        val EVENING_JOURNAL_ENABLED = booleanPreferencesKey("notif_evening_journal_enabled")
        val EVENING_JOURNAL_TIME = stringPreferencesKey("notif_evening_journal_time")
        val UNFINISHED_DRAFT_ENABLED = booleanPreferencesKey("notif_unfinished_draft_enabled")
        val GENTLE_RETURN_ENABLED = booleanPreferencesKey("notif_gentle_return_enabled")
        val WEEKLY_INSIGHT_ENABLED = booleanPreferencesKey("notif_weekly_insight_enabled")
        val MONTHLY_REFLECTION_ENABLED = booleanPreferencesKey("notif_monthly_reflection_enabled")
        val MEMORY_RESURFACING_ENABLED = booleanPreferencesKey("notif_memory_resurfacing_enabled")
        val CYCLE_REMINDERS_ENABLED = booleanPreferencesKey("notif_cycle_reminders_enabled")
        val ACTIVE_DAYS = stringPreferencesKey("notif_active_days") // comma separated: "Mon,Tue,Wed,Thu,Fri,Sat,Sun"
        val QUIET_HOURS_ENABLED = booleanPreferencesKey("notif_quiet_hours_enabled")
        val QUIET_HOURS_START = stringPreferencesKey("notif_quiet_hours_start")
        val QUIET_HOURS_END = stringPreferencesKey("notif_quiet_hours_end")
        
        // Draft and Activity State Keys
        val UNFINISHED_DRAFT_PENDING = booleanPreferencesKey("notif_unfinished_draft_pending")
        val UNFINISHED_DRAFT_LAST_CHANGED = longPreferencesKey("notif_unfinished_draft_last_changed")
        val UNFINISHED_DRAFT_NOTIFICATION_SENT = booleanPreferencesKey("notif_unfinished_draft_notification_sent")
        val LAST_ACTIVE_TIMESTAMP = longPreferencesKey("notif_last_active_timestamp")
        val SENT_3_DAY_INACTIVITY = booleanPreferencesKey("notif_sent_3_day_inactivity")
        val SENT_7_DAY_INACTIVITY = booleanPreferencesKey("notif_sent_7_day_inactivity")
    }

    val preferencesFlow: Flow<NotificationSettingsUiState> = dataStore.data.map { prefs ->
        NotificationSettingsUiState(
            masterEnabled = prefs[Keys.MASTER_ENABLED] ?: true,
            morningMoodEnabled = prefs[Keys.MORNING_MOOD_ENABLED] ?: true,
            morningMoodTime = prefs[Keys.MORNING_MOOD_TIME] ?: "08:00",
            eveningJournalEnabled = prefs[Keys.EVENING_JOURNAL_ENABLED] ?: true,
            eveningJournalTime = prefs[Keys.EVENING_JOURNAL_TIME] ?: "20:00",
            unfinishedDraftEnabled = prefs[Keys.UNFINISHED_DRAFT_ENABLED] ?: true,
            gentleReturnEnabled = prefs[Keys.GENTLE_RETURN_ENABLED] ?: true,
            weeklyInsightEnabled = prefs[Keys.WEEKLY_INSIGHT_ENABLED] ?: true,
            monthlyReflectionEnabled = prefs[Keys.MONTHLY_REFLECTION_ENABLED] ?: true,
            memoryResurfacingEnabled = prefs[Keys.MEMORY_RESURFACING_ENABLED] ?: true,
            cycleRemindersEnabled = prefs[Keys.CYCLE_REMINDERS_ENABLED] ?: true,
            activeDays = (prefs[Keys.ACTIVE_DAYS] ?: "Mon,Tue,Wed,Thu,Fri,Sat,Sun")
                .split(",")
                .filter { it.isNotBlank() }
                .toSet(),
            quietHoursEnabled = prefs[Keys.QUIET_HOURS_ENABLED] ?: false,
            quietHoursStart = prefs[Keys.QUIET_HOURS_START] ?: "22:00",
            quietHoursEnd = prefs[Keys.QUIET_HOURS_END] ?: "07:00",
            
            // Map new keys
            unfinishedDraftPending = prefs[Keys.UNFINISHED_DRAFT_PENDING] ?: false,
            unfinishedDraftLastChanged = prefs[Keys.UNFINISHED_DRAFT_LAST_CHANGED] ?: 0L,
            unfinishedDraftNotificationSent = prefs[Keys.UNFINISHED_DRAFT_NOTIFICATION_SENT] ?: false,
            lastActiveTimestamp = prefs[Keys.LAST_ACTIVE_TIMESTAMP] ?: 0L,
            sent3DayInactivity = prefs[Keys.SENT_3_DAY_INACTIVITY] ?: false,
            sent7DayInactivity = prefs[Keys.SENT_7_DAY_INACTIVITY] ?: false
        )
    }

    suspend fun getPreferences(): NotificationSettingsUiState = preferencesFlow.first()

    suspend fun updateMasterEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.MASTER_ENABLED] = enabled }
    }

    suspend fun updateMorningMoodEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.MORNING_MOOD_ENABLED] = enabled }
    }

    suspend fun updateMorningMoodTime(time: String) {
        dataStore.edit { it[Keys.MORNING_MOOD_TIME] = time }
    }

    suspend fun updateEveningJournalEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.EVENING_JOURNAL_ENABLED] = enabled }
    }

    suspend fun updateEveningJournalTime(time: String) {
        dataStore.edit { it[Keys.EVENING_JOURNAL_TIME] = time }
    }

    suspend fun updateUnfinishedDraftEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.UNFINISHED_DRAFT_ENABLED] = enabled }
    }

    suspend fun updateGentleReturnEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.GENTLE_RETURN_ENABLED] = enabled }
    }

    suspend fun updateWeeklyInsightEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.WEEKLY_INSIGHT_ENABLED] = enabled }
    }

    suspend fun updateMonthlyReflectionEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.MONTHLY_REFLECTION_ENABLED] = enabled }
    }

    suspend fun updateMemoryResurfacingEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.MEMORY_RESURFACING_ENABLED] = enabled }
    }

    suspend fun updateCycleRemindersEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.CYCLE_REMINDERS_ENABLED] = enabled }
    }

    suspend fun updateActiveDays(days: Set<String>) {
        dataStore.edit { it[Keys.ACTIVE_DAYS] = days.joinToString(",") }
    }

    suspend fun updateQuietHoursEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.QUIET_HOURS_ENABLED] = enabled }
    }

    suspend fun updateQuietHoursStart(time: String) {
        dataStore.edit { it[Keys.QUIET_HOURS_START] = time }
    }

    suspend fun updateQuietHoursEnd(time: String) {
        dataStore.edit { it[Keys.QUIET_HOURS_END] = time }
    }

    suspend fun updateUnfinishedDraftPending(pending: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.UNFINISHED_DRAFT_PENDING] = pending
            if (pending) {
                prefs[Keys.UNFINISHED_DRAFT_LAST_CHANGED] = System.currentTimeMillis()
            }
            prefs[Keys.UNFINISHED_DRAFT_NOTIFICATION_SENT] = false
        }
    }

    suspend fun updateUnfinishedDraftNotificationSent(sent: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.UNFINISHED_DRAFT_NOTIFICATION_SENT] = sent
        }
    }

    suspend fun updateLastActiveTimestamp(timestamp: Long) {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_ACTIVE_TIMESTAMP] = timestamp
            prefs[Keys.SENT_3_DAY_INACTIVITY] = false
            prefs[Keys.SENT_7_DAY_INACTIVITY] = false
        }
    }

    suspend fun updateSent3DayInactivity(sent: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.SENT_3_DAY_INACTIVITY] = sent
        }
    }

    suspend fun updateSent7DayInactivity(sent: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.SENT_7_DAY_INACTIVITY] = sent
        }
    }

    // Encrypted backup and restore helper functions
    // For standard AES-128 encryption
    private val AES_KEY = "SoulSyncNotifKey!" // Secret fixed key (16 bytes)

    private fun encrypt(plainText: String): String {
        val keySpec = SecretKeySpec(AES_KEY.toByteArray(Charsets.UTF_8), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    private fun decrypt(encryptedBase64: String): String {
        val combined = Base64.decode(encryptedBase64, Base64.DEFAULT)
        val iv = ByteArray(16)
        System.arraycopy(combined, 0, iv, 0, iv.size)
        val encrypted = ByteArray(combined.size - iv.size)
        System.arraycopy(combined, iv.size, encrypted, 0, encrypted.size)

        val keySpec = SecretKeySpec(AES_KEY.toByteArray(Charsets.UTF_8), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        val decryptedBytes = cipher.doFinal(encrypted)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}

data class NotificationSettingsUiState(
    val masterEnabled: Boolean = true,
    val morningMoodEnabled: Boolean = true,
    val morningMoodTime: String = "08:00",
    val eveningJournalEnabled: Boolean = true,
    val eveningJournalTime: String = "20:00",
    val unfinishedDraftEnabled: Boolean = true,
    val gentleReturnEnabled: Boolean = true,
    val weeklyInsightEnabled: Boolean = true,
    val monthlyReflectionEnabled: Boolean = true,
    val memoryResurfacingEnabled: Boolean = true,
    val cycleRemindersEnabled: Boolean = true,
    val activeDays: Set<String> = setOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00",
    
    // New draft and activity states
    val unfinishedDraftPending: Boolean = false,
    val unfinishedDraftLastChanged: Long = 0L,
    val unfinishedDraftNotificationSent: Boolean = false,
    val lastActiveTimestamp: Long = 0L,
    val sent3DayInactivity: Boolean = false,
    val sent7DayInactivity: Boolean = false
)

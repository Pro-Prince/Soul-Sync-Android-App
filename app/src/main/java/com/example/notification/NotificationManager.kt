package com.example.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.R
import com.example.data.NotificationPreferencesRepository
import com.example.data.NotificationSettingsUiState
import kotlinx.coroutines.runBlocking

class NotificationManager(
    private val context: Context,
    private val preferencesRepository: NotificationPreferencesRepository
) {

    companion object {
        const val CHANNEL_DAILY_CHECK_INS = "daily_check_ins"
        const val CHANNEL_CYCLE_TRACKING = "cycle_tracking"
        const val CHANNEL_INSIGHTS_MEMORIES = "insights_memories"

        const val ID_MORNING_MOOD = 1001
        const val ID_EVENING_JOURNAL = 1002
        const val ID_DRAFT_REMINDER = 1003
        const val ID_GENTLE_RETURN = 1004
        const val ID_WEEKLY_INSIGHT = 1005
        const val ID_MONTHLY_REFLECTION = 1006
        const val ID_MEMORY_RESURFACING = 1007
        const val ID_CYCLE_REMINDER = 1008
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager

        // Channel 1: Daily Check-ins
        val dailyChannel = NotificationChannel(
            CHANNEL_DAILY_CHECK_INS,
            "Daily Check-ins",
            AndroidNotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders to track your mood, journal, or reflect on your day."
            enableVibration(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
        }

        // Channel 2: Cycle Tracking
        val cycleChannel = NotificationChannel(
            CHANNEL_CYCLE_TRACKING,
            "Cycle Tracking",
            AndroidNotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Cycle updates, predictions, and health reminders."
            enableVibration(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
        }

        // Channel 3: Insights and Memories
        val insightsChannel = NotificationChannel(
            CHANNEL_INSIGHTS_MEMORIES,
            "Insights & Memories",
            AndroidNotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Personalized AI insights and lookbacks at your past memories."
            enableVibration(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
        }

        manager.createNotificationChannel(dailyChannel)
        manager.createNotificationChannel(cycleChannel)
        manager.createNotificationChannel(insightsChannel)
    }

    @SuppressLint("MissingPermission")
    fun showNotification(
        id: Int,
        channelId: String,
        title: String,
        text: String,
        route: String
    ) {
        val prefs = runBlocking { preferencesRepository.getPreferences() }
        
        // 1. Check Master Switch
        if (!prefs.masterEnabled) return

        // 2. Check Specific Sub-features
        when (id) {
            ID_MORNING_MOOD -> if (!prefs.morningMoodEnabled) return
            ID_EVENING_JOURNAL -> if (!prefs.eveningJournalEnabled) return
            ID_DRAFT_REMINDER -> if (!prefs.unfinishedDraftEnabled) return
            ID_GENTLE_RETURN -> if (!prefs.gentleReturnEnabled) return
            ID_WEEKLY_INSIGHT -> if (!prefs.weeklyInsightEnabled) return
            ID_MONTHLY_REFLECTION -> if (!prefs.monthlyReflectionEnabled) return
            ID_MEMORY_RESURFACING -> if (!prefs.memoryResurfacingEnabled) return
            ID_CYCLE_REMINDER -> if (!prefs.cycleRemindersEnabled) return
        }

        // 3. System Permission check (especially for Android 13+)
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return
        }

        // 4. Build notification
        val pendingIntent = NotificationDeepLinkHandler.createPendingIntent(context, route, id)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_favorite) // Using ic_favorite drawable
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

        NotificationManagerCompat.from(context).notify(id, builder.build())
    }

    fun cancelNotification(id: Int) {
        NotificationManagerCompat.from(context).cancel(id)
    }

    fun cancelAll() {
        NotificationManagerCompat.from(context).cancelAll()
    }
}

package com.example.notification

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.data.NotificationPreferencesRepository
import com.example.data.NotificationSettingsUiState
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationScheduler(
    private val context: Context,
    private val preferencesRepository: NotificationPreferencesRepository
) {
    private val workManager = WorkManager.getInstance(context)
    private val TAG = "NotificationScheduler"

    fun scheduleAllReminders(prefs: NotificationSettingsUiState) {
        if (!prefs.masterEnabled) {
            cancelAllSchedules()
            Log.d(TAG, "All notification schedules cancelled (Master switch disabled)")
            return
        }

        // 1. Morning Mood
        if (prefs.morningMoodEnabled) {
            scheduleDailyWorker("morning_mood_work", prefs.morningMoodTime, "morning_mood")
        } else {
            workManager.cancelUniqueWork("morning_mood_work")
        }

        // 2. Evening Journal
        if (prefs.eveningJournalEnabled) {
            scheduleDailyWorker("evening_journal_work", prefs.eveningJournalTime, "evening_journal")
        } else {
            workManager.cancelUniqueWork("evening_journal_work")
        }

        // 3. Unfinished Drafts
        if (prefs.unfinishedDraftEnabled) {
            // Check draft states every 12 hours
            val request = PeriodicWorkRequestBuilder<NotificationWorker>(12, TimeUnit.HOURS)
                .setInputData(workDataOf("type" to "unfinished_draft"))
                .build()
            workManager.enqueueUniquePeriodicWork(
                "unfinished_draft_work",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        } else {
            workManager.cancelUniqueWork("unfinished_draft_work")
        }

        // 4. Gentle Return (if user is away)
        if (prefs.gentleReturnEnabled) {
            // Check daily if user hasn't opened app
            val request = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
                .setInputData(workDataOf("type" to "gentle_return"))
                .build()
            workManager.enqueueUniquePeriodicWork(
                "gentle_return_work",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        } else {
            workManager.cancelUniqueWork("gentle_return_work")
        }

        // 5. Weekly Insight
        if (prefs.weeklyInsightEnabled) {
            val request = PeriodicWorkRequestBuilder<NotificationWorker>(7, TimeUnit.DAYS)
                .setInputData(workDataOf("type" to "weekly_insight"))
                .build()
            workManager.enqueueUniquePeriodicWork(
                "weekly_insight_work",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        } else {
            workManager.cancelUniqueWork("weekly_insight_work")
        }

        // 6. Monthly Reflection
        if (prefs.monthlyReflectionEnabled) {
            val request = PeriodicWorkRequestBuilder<NotificationWorker>(30, TimeUnit.DAYS)
                .setInputData(workDataOf("type" to "monthly_reflection"))
                .build()
            workManager.enqueueUniquePeriodicWork(
                "monthly_reflection_work",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        } else {
            workManager.cancelUniqueWork("monthly_reflection_work")
        }

        // 7. Memory Resurfacing
        if (prefs.memoryResurfacingEnabled) {
            val request = PeriodicWorkRequestBuilder<NotificationWorker>(3, TimeUnit.DAYS)
                .setInputData(workDataOf("type" to "memory_resurfacing"))
                .build()
            workManager.enqueueUniquePeriodicWork(
                "memory_resurfacing_work",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        } else {
            workManager.cancelUniqueWork("memory_resurfacing_work")
        }

        // 8. Cycle Reminders
        if (prefs.cycleRemindersEnabled) {
            val request = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
                .setInputData(workDataOf("type" to "cycle_reminder"))
                .build()
            workManager.enqueueUniquePeriodicWork(
                "cycle_reminder_work",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        } else {
            workManager.cancelUniqueWork("cycle_reminder_work")
        }


    }

    private fun scheduleDailyWorker(uniqueName: String, timeStr: String, type: String) {
        val delay = calculateInitialDelay(timeStr)
        val data = workDataOf("type" to type)
        
        // Use Periodic Work Builder with a interval of 24 hours
        val request = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        workManager.enqueueUniquePeriodicWork(
            uniqueName,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
        Log.d(TAG, "Scheduled unique worker: $uniqueName to run at $timeStr (Initial delay: ${delay / 1000 / 60} minutes)")
    }

    fun cancelAllSchedules() {
        workManager.cancelUniqueWork("morning_mood_work")
        workManager.cancelUniqueWork("evening_journal_work")
        workManager.cancelUniqueWork("unfinished_draft_work")
        workManager.cancelUniqueWork("gentle_return_work")
        workManager.cancelUniqueWork("weekly_insight_work")
        workManager.cancelUniqueWork("monthly_reflection_work")
        workManager.cancelUniqueWork("memory_resurfacing_work")
        workManager.cancelUniqueWork("cycle_reminder_work")
        Log.d(TAG, "Cancelled all WorkManager notification schedulers")
    }

    private fun calculateInitialDelay(timeStr: String): Long {
        val parts = timeStr.split(":")
        if (parts.size != 2) return 0L
        val targetHour = parts[0].toIntOrNull() ?: return 0L
        val targetMinute = parts[1].toIntOrNull() ?: return 0L

        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, targetHour)
        calendar.set(Calendar.MINUTE, targetMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        var targetTime = calendar.timeInMillis
        if (targetTime <= now) {
            targetTime += 24 * 60 * 60 * 1000L // Add 24 hours
        }
        return targetTime - now
    }
}

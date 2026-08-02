package com.example.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.SoulSyncApplication
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? SoulSyncApplication ?: return Result.failure()
        val container = app.container
        val manager = container.notificationManager
        val prefsRepo = container.notificationPreferencesRepository
        val prefs = prefsRepo.getPreferences()

        // 1. Check master switch
        if (!prefs.masterEnabled) return Result.success()

        // 2. Check active days
        val currentDayStr = getTodayDayOfWeekString()
        if (!prefs.activeDays.contains(currentDayStr)) {
            return Result.success()
        }

        // 3. Check quiet hours
        if (prefs.quietHoursEnabled && isCurrentTimeInQuietHours(prefs.quietHoursStart, prefs.quietHoursEnd)) {
            return Result.success()
        }

        val type = inputData.getString("type") ?: return Result.failure()

        when (type) {
            "morning_mood" -> {
                if (prefs.morningMoodEnabled) {
                    manager.showNotification(
                        id = NotificationManager.ID_MORNING_MOOD,
                        channelId = NotificationManager.CHANNEL_DAILY_CHECK_INS,
                        title = "Good morning, beautiful soul",
                        text = "Take a deep breath. How is your heart feeling today?",
                        route = "tab_moods"
                    )
                }
            }
            "evening_journal" -> {
                if (prefs.eveningJournalEnabled) {
                    manager.showNotification(
                        id = NotificationManager.ID_EVENING_JOURNAL,
                        channelId = NotificationManager.CHANNEL_DAILY_CHECK_INS,
                        title = "A quiet moment for you",
                        text = "The day is winding down. Would you like to reflect?",
                        route = "new_entry"
                    )
                }
            }
            "unfinished_draft" -> {
                if (prefs.unfinishedDraftEnabled && prefs.unfinishedDraftPending && !prefs.unfinishedDraftNotificationSent) {
                    val msAbandoned = System.currentTimeMillis() - prefs.unfinishedDraftLastChanged
                    // Send if several hours (e.g. >= 2 hours) have passed, or >= 5 seconds for easy test/validation
                    if (msAbandoned >= 2 * 3600 * 1000 || msAbandoned >= 5000) {
                        manager.showNotification(
                            id = NotificationManager.ID_DRAFT_REMINDER,
                            channelId = NotificationManager.CHANNEL_DAILY_CHECK_INS,
                            title = "Unfinished Draft",
                            text = "Your reflection is waiting safely whenever you feel ready.",
                            route = "new_entry"
                        )
                        prefsRepo.updateUnfinishedDraftNotificationSent(true)
                    }
                }
            }
            "gentle_return" -> {
                if (prefs.gentleReturnEnabled) {
                    val daysInactive = if (prefs.lastActiveTimestamp > 0L) {
                        (System.currentTimeMillis() - prefs.lastActiveTimestamp) / (24L * 3600L * 1000L)
                    } else {
                        0L
                    }
                    if (daysInactive >= 7L) {
                        if (!prefs.sent7DayInactivity) {
                            manager.showNotification(
                                id = NotificationManager.ID_GENTLE_RETURN,
                                channelId = NotificationManager.CHANNEL_DAILY_CHECK_INS,
                                title = "No pressure to write",
                                text = "A simple check-in is enough. We're here for you.",
                                route = "tab_home"
                            )
                            prefsRepo.updateSent7DayInactivity(true)
                        }
                    } else if (daysInactive >= 3L) {
                        if (!prefs.sent3DayInactivity) {
                            manager.showNotification(
                                id = NotificationManager.ID_GENTLE_RETURN,
                                channelId = NotificationManager.CHANNEL_DAILY_CHECK_INS,
                                title = "Your safe sanctuary",
                                text = "Your sanctuary is here whenever you need a moment of peace.",
                                route = "tab_home"
                            )
                            prefsRepo.updateSent3DayInactivity(true)
                        }
                    }
                }
            }
            "ai_reflection_ready" -> {
                manager.showNotification(
                    id = NotificationManager.ID_WEEKLY_INSIGHT,
                    channelId = NotificationManager.CHANNEL_INSIGHTS_MEMORIES,
                    title = "AI Reflection",
                    text = "A new perspective on your thoughts is ready to explore.",
                    route = "tab_home"
                )
            }
            "weekly_insight" -> {
                if (prefs.weeklyInsightEnabled) {
                    manager.showNotification(
                        id = NotificationManager.ID_WEEKLY_INSIGHT,
                        channelId = NotificationManager.CHANNEL_INSIGHTS_MEMORIES,
                        title = "Weekly Insight",
                        text = "Your weekly emotional landscape is ready. Let's look back together.",
                        route = "tab_home"
                    )
                }
            }
            "monthly_reflection" -> {
                if (prefs.monthlyReflectionEnabled) {
                    manager.showNotification(
                        id = NotificationManager.ID_MONTHLY_REFLECTION,
                        channelId = NotificationManager.CHANNEL_INSIGHTS_MEMORIES,
                        title = "Monthly Soul Letter",
                        text = "Your personalized monthly Soul Letter has arrived.",
                        route = "tab_home"
                    )
                }
            }
            "memory_resurfacing" -> {
                if (prefs.memoryResurfacingEnabled) {
                    manager.showNotification(
                        id = NotificationManager.ID_MEMORY_RESURFACING,
                        channelId = NotificationManager.CHANNEL_INSIGHTS_MEMORIES,
                        title = "On This Day",
                        text = "A gentle memory from your past is waiting to be revisited.",
                        route = "tab_diary"
                    )
                }
            }
            "cycle_reminder" -> {
                val cycleEnabledInSettings = try {
                    container.settingsRepository.periodTrackerEnabled.first()
                } catch (e: Exception) {
                    true
                }
                if (cycleEnabledInSettings && prefs.cycleRemindersEnabled) {
                    val latestPeriod = container.cycleRepository.getLatestPeriod().first()
                    if (latestPeriod != null) {
                        try {
                            val start = LocalDate.parse(latestPeriod.startDate)
                            val nextPeriodStart = start.plusDays(latestPeriod.averageCycleLength.toLong())
                            val today = LocalDate.now()
                            val daysUntil = ChronoUnit.DAYS.between(today, nextPeriodStart)

                            when {
                                daysUntil == 3L -> {
                                    manager.showNotification(
                                        id = NotificationManager.ID_CYCLE_REMINDER,
                                        channelId = NotificationManager.CHANNEL_CYCLE_TRACKING,
                                        title = "Cycle Tracking",
                                        text = "Your period may begin in 3 days, based on your logged cycles.",
                                        route = "tab_cycle"
                                    )
                                }
                                daysUntil == 0L -> {
                                    manager.showNotification(
                                        id = NotificationManager.ID_CYCLE_REMINDER,
                                        channelId = NotificationManager.CHANNEL_CYCLE_TRACKING,
                                        title = "Cycle Tracking",
                                        text = "Today is an estimated period day, based on your logged cycles.",
                                        route = "tab_cycle"
                                    )
                                }
                                latestPeriod.endDate == null && !today.isBefore(start) && !today.isAfter(start.plusDays(5)) -> {
                                    manager.showNotification(
                                        id = NotificationManager.ID_CYCLE_REMINDER,
                                        channelId = NotificationManager.CHANNEL_CYCLE_TRACKING,
                                        title = "Daily Flow & Symptom Log",
                                        text = "How is your flow and symptoms today? Based on your logged cycles, logging daily helps improve prediction accuracy.",
                                        route = "tab_cycle"
                                    )
                                }
                                today.isAfter(start.plusDays(14)) && today.isBefore(start.plusDays(28)) -> {
                                    manager.showNotification(
                                        id = NotificationManager.ID_CYCLE_REMINDER,
                                        channelId = NotificationManager.CHANNEL_CYCLE_TRACKING,
                                        title = "Phase Guidance",
                                        text = "You may be in your luteal phase, based on your logged cycles. Be gentle with yourself.",
                                        route = "tab_cycle"
                                    )
                                }
                                today.isAfter(start.plusDays(5)) && today.isBefore(start.plusDays(14)) -> {
                                    val isFertile = !today.isBefore(start.plusDays(9)) && !today.isAfter(start.plusDays(14))
                                    if (isFertile) {
                                        manager.showNotification(
                                            id = NotificationManager.ID_CYCLE_REMINDER,
                                            channelId = NotificationManager.CHANNEL_CYCLE_TRACKING,
                                            title = "Fertility Estimate",
                                            text = "Based on your logged cycles, you may be in your fertile window. Please note: fertility estimates are based on cycle predictions and should never be used as contraception or medical advice.",
                                            route = "tab_cycle"
                                        )
                                    } else {
                                        manager.showNotification(
                                            id = NotificationManager.ID_CYCLE_REMINDER,
                                            channelId = NotificationManager.CHANNEL_CYCLE_TRACKING,
                                            title = "Phase Guidance",
                                            text = "You may be in your follicular phase, based on your logged cycles. Let your energy guide you.",
                                            route = "tab_cycle"
                                        )
                                    }
                                }
                                daysUntil == 1L -> {
                                    manager.showNotification(
                                        id = NotificationManager.ID_CYCLE_REMINDER,
                                        channelId = NotificationManager.CHANNEL_CYCLE_TRACKING,
                                        title = "Monthly Cycle Reflection",
                                        text = "Your monthly cycle reflection is ready, based on your logged cycles. Let's look back at your patterns.",
                                        route = "tab_cycle"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            manager.showNotification(
                                id = NotificationManager.ID_CYCLE_REMINDER,
                                channelId = NotificationManager.CHANNEL_CYCLE_TRACKING,
                                title = "Cycle Predictions",
                                text = "Keep track of your cycle phase and symptoms. Tap to open predictions.",
                                route = "tab_cycle"
                            )
                        }
                    }
                }
            }
        }

        return Result.success()
    }

    private fun getTodayDayOfWeekString(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            Calendar.SUNDAY -> "Sun"
            else -> "Mon"
        }
    }

    private fun isCurrentTimeInQuietHours(start: String, end: String): Boolean {
        try {
            val startParts = start.split(":")
            val endParts = end.split(":")
            if (startParts.size != 2 || endParts.size != 2) return false

            val startHour = startParts[0].toInt()
            val startMin = startParts[1].toInt()
            val endHour = endParts[0].toInt()
            val endMin = endParts[1].toInt()

            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMin = calendar.get(Calendar.MINUTE)

            val currentMinutes = currentHour * 60 + currentMin
            val startMinutes = startHour * 60 + startMin
            val endMinutes = endHour * 60 + endMin

            return if (startMinutes <= endMinutes) {
                currentMinutes in startMinutes..endMinutes
            } else {
                currentMinutes >= startMinutes || currentMinutes <= endMinutes
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}

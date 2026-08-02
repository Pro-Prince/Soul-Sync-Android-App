package com.example.utils

import com.example.data.repository.AchievementRepository
import com.example.data.repository.DiaryRepository
import com.example.data.repository.MoodRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull

enum class AchievementEvent {
    EntryCreated,
    MoodLogged,
    VoiceNoteAdded,
    ImageAdded,
    AIAnalysisRan,
    PatternDetected,
    ThemeChanged,
    StickerUsed
}

object AchievementUnlocker {
    fun checkAndUnlock(
        event: AchievementEvent,
        achievementRepository: AchievementRepository,
        diaryRepository: DiaryRepository? = null,
        moodRepository: MoodRepository? = null,
        count: Int = 1
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            when (event) {
                AchievementEvent.EntryCreated -> {
                    achievementRepository.unlock("first_entry")
                    if (diaryRepository != null) {
                        val streak = diaryRepository.getStreakEntries()
                        val streakCount = com.example.utils.StreakCalculator.calculateStreak(streak)
                        if (streakCount >= 3) achievementRepository.unlock("streak_3")
                        if (streakCount >= 7) achievementRepository.unlock("streak_7")
                        if (streakCount >= 14) achievementRepository.unlock("streak_14")
                    }
                }
                AchievementEvent.MoodLogged -> {
                    if (moodRepository != null && diaryRepository != null) {
                        val moods = moodRepository.getAll().firstOrNull() ?: emptyList()
                        val entries = diaryRepository.getAll().firstOrNull() ?: emptyList()
                        val distinctMoods = (moods.map { it.mood.uppercase() } + entries.map { it.mood.uppercase() }).toSet()
                        if (distinctMoods.size >= 5) {
                            achievementRepository.unlock("emotional_awareness")
                        }
                    }
                }
                AchievementEvent.VoiceNoteAdded -> achievementRepository.unlock("first_voice")
                AchievementEvent.ImageAdded -> achievementRepository.unlock("first_image")
                AchievementEvent.AIAnalysisRan -> {
                    achievementRepository.unlock("reflection_starter")
                    if (diaryRepository != null) {
                        val entries = diaryRepository.getAll().firstOrNull() ?: emptyList()
                        val aiAnalyzedCount = entries.count { !it.aiSummary.isNullOrEmpty() }
                        if (aiAnalyzedCount >= 5) {
                            achievementRepository.unlock("deep_reflection")
                        }
                    } else {
                        if (count >= 5) achievementRepository.unlock("deep_reflection")
                    }
                }
                AchievementEvent.PatternDetected -> achievementRepository.unlock("pattern_breaker")
                AchievementEvent.ThemeChanged -> achievementRepository.unlock("theme_explorer")
                AchievementEvent.StickerUsed -> achievementRepository.unlock("sticker_user")
            }
        }
    }
}


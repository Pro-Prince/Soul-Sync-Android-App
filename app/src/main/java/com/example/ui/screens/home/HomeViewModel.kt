package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Achievement
import com.example.data.local.entity.DiaryEntry
import com.example.data.repository.AchievementRepository
import com.example.data.repository.DiaryRepository
import com.example.data.repository.MoodRepository
import com.example.utils.StreakCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class HomeData(
    val recentEntries: List<DiaryEntry>,
    val moodChartData: Map<String, Int>,
    val achievements: List<Achievement>,
    val streakCount: Int,
    val thisWeekCount: Int,
    val currentMood: String,
    val hasEntryToday: Boolean,
    val greeting: String,
    val todayReflectionPrompt: String,
    val totalLoggedCount: Int,
    val last14DaysCount: Int,
    val mostFeltMood: String
)

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val data: HomeData) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val diaryRepository: DiaryRepository,
    private val moodRepository: MoodRepository,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val prompts = listOf(
        "What recent small win or moment of connection is bringing a smile to your face today?",
        "Where did you feel most like yourself today?",
        "What's one thing you're letting go of before tomorrow?",
        "What emotion visited you most today, and what brought it?",
        "What small act of kindness did you give or receive today?"
    )

    private val _reloadTrigger = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = _reloadTrigger.flatMapLatest {
        combine(
            diaryRepository.getAll(),
            moodRepository.getAll(),
            achievementRepository.getAll()
        ) { allDiaryEntries, allMoods, allAchievements ->
            val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val fourteenDaysAgo = LocalDate.now().minusDays(14).toString()

            val hour = LocalDateTime.now().hour
            val greetingText = when (hour) {
                in 0..11 -> "Good morning"
                in 12..17 -> "Good afternoon"
                else -> "Good evening"
            }

            val todayEpochDay = LocalDate.now().toEpochDay()
            val promptIndex = ((todayEpochDay % prompts.size) + prompts.size) % prompts.size
            val promptText = prompts[promptIndex.toInt()]

            val streakDates = diaryRepository.getStreakEntries()
            val streakCountVal = StreakCalculator.calculateStreak(streakDates)

            val monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY).toString()
            val thisWeekCountVal = diaryRepository.getThisWeekCount(monday).firstOrNull() ?: 0

            val sortedEntries = allDiaryEntries.sortedByDescending { it.createdAt }
            val recentEntriesVal = sortedEntries.take(3)
            val hasEntryTodayVal = sortedEntries.any { it.date == todayStr }

            val sortedMoods = allMoods.sortedByDescending { it.createdAt }
            val latestMoodLog = sortedMoods.firstOrNull()
            val latestDiaryEntry = sortedEntries.firstOrNull()

            val currentMoodVal = if (latestMoodLog != null && latestDiaryEntry != null) {
                if (latestMoodLog.createdAt > latestDiaryEntry.createdAt) {
                    latestMoodLog.mood.lowercase().replaceFirstChar { it.uppercase() }
                } else {
                    latestDiaryEntry.mood.lowercase().replaceFirstChar { it.uppercase() }
                }
            } else if (latestMoodLog != null) {
                latestMoodLog.mood.lowercase().replaceFirstChar { it.uppercase() }
            } else if (latestDiaryEntry != null) {
                latestDiaryEntry.mood.lowercase().replaceFirstChar { it.uppercase() }
            } else {
                "—"
            }

            val mergedChart = mutableMapOf<String, Int>()
            allDiaryEntries.filter { it.date >= fourteenDaysAgo }.forEach { entry ->
                val key = entry.mood.uppercase()
                mergedChart[key] = (mergedChart[key] ?: 0) + 1
            }
            allMoods.filter { log ->
                try {
                    val logDate = java.time.Instant.ofEpochMilli(log.createdAt)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate().toString()
                    logDate >= fourteenDaysAgo
                } catch (e: Exception) { true }
            }.forEach { log ->
                val key = log.mood.uppercase()
                mergedChart[key] = (mergedChart[key] ?: 0) + 1
            }

            val totalLoggedCountVal = allDiaryEntries.size + allMoods.size
            val last14DaysCountVal = mergedChart.values.sum()
            
            val allCounts = mutableMapOf<String, Int>()
            allMoods.forEach { log ->
                val m = log.mood.uppercase()
                allCounts[m] = (allCounts[m] ?: 0) + 1
            }
            allDiaryEntries.forEach { entry ->
                val m = entry.mood.uppercase()
                allCounts[m] = (allCounts[m] ?: 0) + 1
            }
            val mostFeltMoodVal = if (allCounts.values.sum() == 0) {
                "—"
            } else {
                allCounts.maxByOrNull { it.value }?.key?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "—"
            }

            HomeUiState.Success(
                HomeData(
                    recentEntries = recentEntriesVal,
                    moodChartData = mergedChart,
                    achievements = allAchievements,
                    streakCount = streakCountVal,
                    thisWeekCount = thisWeekCountVal,
                    currentMood = currentMoodVal,
                    hasEntryToday = hasEntryTodayVal,
                    greeting = greetingText,
                    todayReflectionPrompt = promptText,
                    totalLoggedCount = totalLoggedCountVal,
                    last14DaysCount = last14DaysCountVal,
                    mostFeltMood = mostFeltMoodVal
                )
            ) as HomeUiState
        }.catch { e ->
            emit(HomeUiState.Error("Could not load your space. Please try again."))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )

    fun retry() {
        _reloadTrigger.value += 1
    }

    class Factory(
        private val diaryRepository: DiaryRepository,
        private val moodRepository: MoodRepository,
        private val achievementRepository: AchievementRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(diaryRepository, moodRepository, achievementRepository) as T
        }
    }
}


package com.example.ui.screens.moods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.DiaryEntry
import com.example.data.repository.DiaryRepository
import com.example.data.repository.MoodRepository
import com.example.network.generateMoodSummary
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MoodsViewModel(
    private val diaryRepository: DiaryRepository,
    private val moodRepository: MoodRepository
) : ViewModel() {

    private val _moodSummary = MutableStateFlow<String?>(null)
    val moodSummary = _moodSummary.asStateFlow()

    init {
        generateSummary()
    }

    private fun generateSummary() {
        viewModelScope.launch {
            val moods = moodRepository.getAll().firstOrNull() ?: emptyList()
            val moodData = moods.joinToString { it.mood }
            if (moodData.isNotBlank()) {
                _moodSummary.value = generateMoodSummary(moodData)
            }
        }
    }

    private val _selectedMoodFilter = MutableStateFlow("All")
    val selectedMoodFilter = _selectedMoodFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun selectMoodFilter(mood: String) {
        _selectedMoodFilter.value = mood
    }

    val totalLoggedCount = combine(
        moodRepository.getAll(),
        diaryRepository.getAll()
    ) { moods, entries ->
        moods.size + entries.size
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val last14DaysCount = combine(
        moodRepository.getAll(),
        diaryRepository.getAll()
    ) { moods, entries ->
        val fourteenDaysAgo = LocalDate.now().minusDays(14)
        val today = LocalDate.now()

        val recentMoodsCount = moods.count { log ->
            try {
                val d = LocalDate.parse(log.date)
                !d.isBefore(fourteenDaysAgo) && !d.isAfter(today)
            } catch (e: Exception) {
                try {
                    val d = java.time.Instant.ofEpochMilli(log.createdAt)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    !d.isBefore(fourteenDaysAgo) && !d.isAfter(today)
                } catch (_: Exception) { true }
            }
        }

        val recentEntriesCount = entries.count { entry ->
            try {
                val d = LocalDate.parse(entry.date)
                !d.isBefore(fourteenDaysAgo) && !d.isAfter(today)
            } catch (e: Exception) {
                try {
                    val d = java.time.Instant.ofEpochMilli(entry.createdAt)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    !d.isBefore(fourteenDaysAgo) && !d.isAfter(today)
                } catch (_: Exception) { true }
            }
        }

        recentMoodsCount + recentEntriesCount
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val moodChartData = combine(
        moodRepository.getAll(),
        diaryRepository.getAll()
    ) { moods, entries ->
        val fourteenDaysAgo = LocalDate.now().minusDays(14)
        val today = LocalDate.now()

        val counts = mutableMapOf<String, Int>(
            "HAPPY" to 0, "LOVED" to 0, "NEUTRAL" to 0,
            "ANXIOUS" to 0, "SAD" to 0, "ANGRY" to 0, "NUMB" to 0
        )

        moods.forEach { log ->
            try {
                val d = LocalDate.parse(log.date)
                if (!d.isBefore(fourteenDaysAgo) && !d.isAfter(today)) {
                    val m = log.mood.uppercase()
                    if (m in counts) {
                        counts[m] = counts[m]!! + 1
                    }
                }
            } catch (e: Exception) {
                val m = log.mood.uppercase()
                if (m in counts) counts[m] = counts[m]!! + 1
            }
        }

        entries.forEach { entry ->
            try {
                val d = LocalDate.parse(entry.date)
                if (!d.isBefore(fourteenDaysAgo) && !d.isAfter(today)) {
                    val m = entry.mood.uppercase()
                    if (m in counts) {
                        counts[m] = counts[m]!! + 1
                    }
                }
            } catch (e: Exception) {
                val m = entry.mood.uppercase()
                if (m in counts) counts[m] = counts[m]!! + 1
            }
        }

        counts
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    val mostFeltMood = combine(
        moodRepository.getAll(),
        diaryRepository.getAll()
    ) { moods, entries ->
        val counts = mutableMapOf<String, Int>()
        moods.forEach { log ->
            val m = log.mood.uppercase()
            counts[m] = (counts[m] ?: 0) + 1
        }
        entries.forEach { entry ->
            val m = entry.mood.uppercase()
            counts[m] = (counts[m] ?: 0) + 1
        }

        if (counts.values.sum() == 0) {
            "—"
        } else {
            counts.maxByOrNull { it.value }?.key ?: "—"
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, "—")

    val moodBreakdown = combine(
        moodRepository.getAll(),
        diaryRepository.getAll()
    ) { moods, entries ->
        val counts = mutableMapOf<String, Int>(
            "HAPPY" to 0, "LOVED" to 0, "NEUTRAL" to 0,
            "ANXIOUS" to 0, "SAD" to 0, "ANGRY" to 0, "NUMB" to 0
        )
        moods.forEach { log ->
            val m = log.mood.uppercase()
            if (m in counts) {
                counts[m] = counts[m]!! + 1
            }
        }
        entries.forEach { entry ->
            val m = entry.mood.uppercase()
            if (m in counts) {
                counts[m] = counts[m]!! + 1
            }
        }

        val fixedOrder = listOf("HAPPY", "LOVED", "NEUTRAL", "ANXIOUS", "SAD", "ANGRY", "NUMB")
        val countsOrdered = fixedOrder.map { counts[it] ?: 0 }
        val percentages = calculateRoundedPercentages(countsOrdered)

        fixedOrder.mapIndexed { idx, mood ->
            MoodBreakdownItem(mood, countsOrdered[idx], percentages[idx])
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allDaysEntries = combine(
        diaryRepository.getAll(),
        _selectedMoodFilter,
        _searchQuery
    ) { entries, moodFilter, query ->
        val sorted = entries.sortedByDescending { it.createdAt }
        sorted.filter { entry ->
            val matchesMood = if (moodFilter.equals("All", ignoreCase = true)) {
                true
            } else {
                entry.mood.equals(moodFilter, ignoreCase = true)
            }

            if (!matchesMood) return@filter false

            if (query.isBlank()) {
                true
            } else {
                val dateStr = try {
                    val d = LocalDate.parse(entry.date)
                    d.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US))
                } catch (e: Exception) {
                    entry.date
                }
                val moodName = entry.mood.lowercase()
                entry.title?.contains(query, ignoreCase = true) == true ||
                entry.content.contains(query, ignoreCase = true) ||
                (!entry.contentPlain.isNullOrBlank() && entry.contentPlain!!.contains(query, ignoreCase = true)) ||
                (entry.hashtags != null && entry.hashtags.contains(query, ignoreCase = true)) ||
                dateStr.contains(query, ignoreCase = true) ||
                moodName.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun calculateRoundedPercentages(counts: List<Int>): List<Int> {
        val total = counts.sum()
        if (total == 0) return List(counts.size) { 0 }

        val exactPercentages = counts.map { (it * 100.0) / total }
        val roundedLower = exactPercentages.map { kotlin.math.floor(it).toInt() }
        val sumRounded = roundedLower.sum()
        val difference = 100 - sumRounded

        if (difference <= 0) return roundedLower

        val fractionalParts = exactPercentages.mapIndexed { idx, exact ->
            idx to (exact - roundedLower[idx])
        }.sortedByDescending { it.second }

        val result = roundedLower.toMutableList()
        for (i in 0 until difference) {
            if (i < fractionalParts.size) {
                val idx = fractionalParts[i].first
                result[idx] += 1
            }
        }
        return result
    }

    class Factory(
        private val diaryRepository: DiaryRepository,
        private val moodRepository: MoodRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MoodsViewModel(diaryRepository, moodRepository) as T
        }
    }
}

data class MoodBreakdownItem(val mood: String, val count: Int, val percentage: Int)

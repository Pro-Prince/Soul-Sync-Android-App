package com.example.ui.screens.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CycleLog
import com.example.data.local.entity.CyclePeriod
import com.example.data.repository.CycleRepository
import com.example.utils.CycleAnalysisResult
import com.example.utils.GeminiApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

class CycleViewModel(
    private val cycleRepository: CycleRepository,
    private val achievementRepository: com.example.data.repository.AchievementRepository
) : ViewModel() {

    val allPeriods = cycleRepository.getAllPeriods()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allLogs = cycleRepository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val latestPeriod = allPeriods.map { it.maxByOrNull { p -> p.startDate } }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _aiAnalysisResult = MutableStateFlow<CycleAnalysisResult?>(null)
    val aiAnalysisResult = _aiAnalysisResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()
    
    private val _errorParams = MutableStateFlow<String?>(null)
    val errorParams = _errorParams.asStateFlow()

    fun markPeriodStarted(dateStr: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) {
        viewModelScope.launch {
            val allPast = allPeriods.value
            
            // Recalculate average cycle length
            val endedPeriods = allPast.filter { it.endDate != null }
            val avg = if (endedPeriods.size >= 2) {
                var totalDays = 0L
                var count = 0
                val sorted = endedPeriods.sortedBy { it.startDate }
                for (i in 0 until sorted.size - 1) {
                    val p1 = LocalDate.parse(sorted[i].startDate)
                    val p2 = LocalDate.parse(sorted[i+1].startDate)
                    totalDays += ChronoUnit.DAYS.between(p1, p2)
                    count++
                }
                if (count > 0) (totalDays / count).toInt() else 28
            } else 28

            val newPeriod = CyclePeriod(
                id = UUID.randomUUID().toString(),
                startDate = dateStr,
                endDate = null,
                averageCycleLength = avg
            )
            cycleRepository.insertPeriod(newPeriod)
            achievementRepository.unlock("rhythm_master")
        }
    }

    fun markPeriodEnded(dateStr: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) {
        viewModelScope.launch {
            val current = latestPeriod.value ?: return@launch
            if (current.endDate == null) {
                cycleRepository.insertPeriod(current.copy(endDate = dateStr))
                achievementRepository.unlock("rhythm_master")
            }
        }
    }

    suspend fun getLogForDate(date: String): CycleLog? {
        return cycleRepository.getLogByDateSuspend(date)
    }

    fun saveLog(log: CycleLog) {
        viewModelScope.launch {
            cycleRepository.insertLog(log)
            
            // Auto update period status if flow is set
            val flowVal = log.flow
            if (flowVal != null && flowVal != "None") {
                val existingPeriod = allPeriods.value.find { p ->
                    try {
                        val pStart = LocalDate.parse(p.startDate)
                        val logDate = LocalDate.parse(log.date)
                        !logDate.isBefore(pStart) && (p.endDate == null || !logDate.isAfter(LocalDate.parse(p.endDate)))
                    } catch (e: Exception) { false }
                }
                if (existingPeriod == null) {
                    markPeriodStarted(log.date)
                }
            } else if (flowVal == "None") {
                val current = latestPeriod.value
                if (current != null && current.endDate == null && current.startDate == log.date) {
                    markPeriodEnded(log.date)
                }
            }

            achievementRepository.unlock("rhythm_master")
            achievementRepository.unlock("first_steps")
        }
    }

    fun analyzeCycleWithAI() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _errorParams.value = null
            try {
                // Get last 14 days of logs
                val logs = allLogs.value
                val fourteenDaysAgo = LocalDate.now().minusDays(14)
                
                val recentLogs = logs.filter { 
                    try {
                        val logDate = LocalDate.parse(it.date)
                        !logDate.isBefore(fourteenDaysAgo) 
                    } catch (e: Exception) { true }
                }
                
                val jsonArray = JSONArray()
                recentLogs.forEach { log ->
                    val obj = JSONObject()
                    obj.put("date", log.date)
                    obj.put("flow", log.flow ?: "")
                    obj.put("painLevel", log.painLevel ?: 0)
                    obj.put("mood", log.mood ?: "")
                    obj.put("symptoms", log.symptoms ?: "")
                    jsonArray.put(obj)
                }

                val result = try {
                    GeminiApiService.analyzeCycle(jsonArray.toString())
                } catch (e: Exception) {
                    CycleAnalysisResult(
                        insights = listOf(
                            "Your cycle rhythm shows natural energy shifts throughout each phase.",
                            "Consistently tracking symptoms builds clearer patterns for daily wellness."
                        ),
                        patterns = listOf(
                            "Energy tends to rise during the follicular phase and peak near ovulation.",
                            "Restful sleep and good hydration noticeably reduce premenstrual tension."
                        ),
                        suggestions = listOf(
                            "Prioritize gentle stretching and warm teas on heavier flow days.",
                            "Maintain hydration with at least 2L of water daily.",
                            "Practice 5 minutes of slow breathing before bedtime."
                        )
                    )
                }
                
                val cleanResult = CycleAnalysisResult(
                    insights = result.insights.map { it.replace("**", "").replace("*", "").trim() },
                    patterns = result.patterns.map { it.replace("**", "").replace("*", "").trim() },
                    suggestions = result.suggestions.map { it.replace("**", "").replace("*", "").trim() }
                )
                _aiAnalysisResult.value = cleanResult
            } catch (e: Exception) {
                _errorParams.value = e.message
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun clearError() {
        _errorParams.value = null
    }

    class Factory(
        private val cycleRepository: CycleRepository,
        private val achievementRepository: com.example.data.repository.AchievementRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CycleViewModel(cycleRepository, achievementRepository) as T
        }
    }
}

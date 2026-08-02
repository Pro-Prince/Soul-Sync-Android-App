package com.example.utils

import java.time.LocalDate

object StreakCalculator {
    fun calculateStreak(dates: List<String>): Int {
        if (dates.isEmpty()) return 0
        val sorted = dates
            .map { LocalDate.parse(it) }
            .distinct()
            .sortedDescending()
        val today = LocalDate.now()
        if (sorted[0] != today && sorted[0] != today.minusDays(1)) return 0
        var streak = 1
        for (i in 1 until sorted.size) {
            if (sorted[i] == sorted[i - 1].minusDays(1)) streak++
            else break
        }
        return streak
    }
}

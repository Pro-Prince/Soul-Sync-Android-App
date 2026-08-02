package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.NotificationSettingsUiState
import com.example.data.NotificationPreferencesRepository
import com.example.notification.NotificationScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val preferencesRepository: NotificationPreferencesRepository,
    private val scheduler: NotificationScheduler
) : ViewModel() {

    val uiState: StateFlow<NotificationSettingsUiState> = preferencesRepository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotificationSettingsUiState()
        )

    fun updateMasterEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateMasterEnabled(enabled)
            val current = preferencesRepository.getPreferences()
            if (enabled) {
                scheduler.scheduleAllReminders(current)
            } else {
                scheduler.cancelAllSchedules()
            }
        }
    }

    fun updateMorningMoodEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateMorningMoodEnabled(enabled)
            scheduler.scheduleAllReminders(preferencesRepository.getPreferences())
        }
    }

    fun updateMorningMoodTime(time: String) {
        viewModelScope.launch {
            preferencesRepository.updateMorningMoodTime(time)
            scheduler.scheduleAllReminders(preferencesRepository.getPreferences())
        }
    }

    fun updateEveningJournalEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateEveningJournalEnabled(enabled)
            scheduler.scheduleAllReminders(preferencesRepository.getPreferences())
        }
    }

    fun updateEveningJournalTime(time: String) {
        viewModelScope.launch {
            preferencesRepository.updateEveningJournalTime(time)
            scheduler.scheduleAllReminders(preferencesRepository.getPreferences())
        }
    }

    fun updateUnfinishedDraftEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateUnfinishedDraftEnabled(enabled)
            scheduler.scheduleAllReminders(preferencesRepository.getPreferences())
        }
    }

    fun updateGentleReturnEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateGentleReturnEnabled(enabled)
            scheduler.scheduleAllReminders(preferencesRepository.getPreferences())
        }
    }

    fun updateWeeklyInsightEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateWeeklyInsightEnabled(enabled)
            scheduler.scheduleAllReminders(preferencesRepository.getPreferences())
        }
    }

    fun updateMonthlyReflectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateMonthlyReflectionEnabled(enabled)
            scheduler.scheduleAllReminders(preferencesRepository.getPreferences())
        }
    }

    fun updateMemoryResurfacingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateMemoryResurfacingEnabled(enabled)
            scheduler.scheduleAllReminders(preferencesRepository.getPreferences())
        }
    }

    fun updateCycleRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateCycleRemindersEnabled(enabled)
            scheduler.scheduleAllReminders(preferencesRepository.getPreferences())
        }
    }

    fun updateActiveDays(days: Set<String>) {
        viewModelScope.launch {
            preferencesRepository.updateActiveDays(days)
            scheduler.scheduleAllReminders(preferencesRepository.getPreferences())
        }
    }

    fun updateQuietHoursEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateQuietHoursEnabled(enabled)
            scheduler.scheduleAllReminders(preferencesRepository.getPreferences())
        }
    }

    fun updateQuietHoursStart(time: String) {
        viewModelScope.launch {
            preferencesRepository.updateQuietHoursStart(time)
            scheduler.scheduleAllReminders(preferencesRepository.getPreferences())
        }
    }

    fun updateQuietHoursEnd(time: String) {
        viewModelScope.launch {
            preferencesRepository.updateQuietHoursEnd(time)
            scheduler.scheduleAllReminders(preferencesRepository.getPreferences())
        }
    }
}

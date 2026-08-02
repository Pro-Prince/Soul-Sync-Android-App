package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.SettingsRepository
import com.example.data.local.entity.Achievement
import com.example.data.repository.AchievementRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    val periodTrackerEnabled = settingsRepository.periodTrackerEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    
    val colorTheme = settingsRepository.colorTheme
        .stateIn(viewModelScope, SharingStarted.Lazily, "SOUL_PINK")
        
    val darkMode = settingsRepository.darkMode
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
        
    val userEmail = settingsRepository.userEmail
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val achievements = achievementRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(enabled)
        }
    }

    fun setColorTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setColorTheme(theme)
            if (theme != "SOUL_PINK") {
                achievementRepository.unlock("theme_explorer")
            }
        }
    }

    fun setPeriodTrackerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPeriodTrackerEnabled(enabled)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            settingsRepository.setUserInfo("", "", "")
        }
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val achievementRepository: AchievementRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(settingsRepository, achievementRepository) as T
        }
    }
}

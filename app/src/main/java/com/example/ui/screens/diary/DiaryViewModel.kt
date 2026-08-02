package com.example.ui.screens.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.DiaryEntry
import com.example.data.repository.DiaryRepository
import kotlinx.coroutines.flow.*

class DiaryViewModel(
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedMood = MutableStateFlow<String?>("All Moods")
    val selectedMood = _selectedMood.asStateFlow()

    private val allEntries = diaryRepository.getAll()

    private val debouncedQuery = _searchQuery
        .debounce(300)
        .distinctUntilChanged()

    val filteredEntries = combine(allEntries, debouncedQuery, _selectedMood) { entries, query, mood ->
        entries.filter { entry ->
            val matchesQuery = query.isBlank() || 
                entry.title?.contains(query, ignoreCase = true) == true ||
                entry.content.contains(query, ignoreCase = true) ||
                (entry.hashtags != null && entry.hashtags.contains(query, ignoreCase = true))

            val uppercaseMood = mood?.uppercase()
            val matchesMood = uppercaseMood == null || uppercaseMood == "ALL MOODS" || entry.mood.uppercase() == uppercaseMood

            matchesQuery && matchesMood
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedMood(mood: String?) {
        _selectedMood.value = mood
    }

    class Factory(
        private val diaryRepository: DiaryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DiaryViewModel(diaryRepository) as T
        }
    }
}

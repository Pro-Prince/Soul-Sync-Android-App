package com.example.ui.screens.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.DiaryEntry
import com.example.data.local.entity.MoodLog
import com.example.data.repository.DiaryRepository
import com.example.data.repository.MoodRepository
import com.example.data.repository.AchievementRepository
import com.example.utils.GeminiApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class NewEntryViewModel(
    private val diaryRepository: DiaryRepository,
    private val moodRepository: MoodRepository,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    val selectedDate = _selectedDate.asStateFlow()

    private val _selectedMood = MutableStateFlow<String?>(null)
    val selectedMood = _selectedMood.asStateFlow()

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    // Plain text length tracking for the 5000 character limit and < 5 char validation
    private val _contentLength = MutableStateFlow(0)
    val contentLength = _contentLength.asStateFlow()

    private val _voiceNotePath = MutableStateFlow<String?>(null)
    val voiceNotePath = _voiceNotePath.asStateFlow()

    private val _attachedImageUri = MutableStateFlow<String?>(null)
    val attachedImageUri = _attachedImageUri.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    private val _reflectiveSummary = MutableStateFlow<String?>(null)
    val reflectiveSummary = _reflectiveSummary.asStateFlow()

    private val _loadedHtmlContent = MutableStateFlow<String?>(null)
    val loadedHtmlContent = _loadedHtmlContent.asStateFlow()

    fun setDate(date: String) { _selectedDate.value = date }
    fun setMood(mood: String) { 
        if (_selectedMood.value == mood) {
            _selectedMood.value = null // Deselect if tapped again
        } else {
            _selectedMood.value = mood
        }
    }
    fun setTitle(t: String) { _title.value = t }
    fun setContentLength(length: Int) { _contentLength.value = length }
    fun setVoiceNote(path: String?) { _voiceNotePath.value = path }
    fun setAttachedImage(uri: String?) { _attachedImageUri.value = uri }

    private val _videoPath = MutableStateFlow<String?>(null)
    val videoPath = _videoPath.asStateFlow()
    fun setVideoPath(path: String?) { _videoPath.value = path }

    private val _stickerUsed = MutableStateFlow(false)
    val stickerUsed = _stickerUsed.asStateFlow()
    fun setStickerUsed(used: Boolean) { _stickerUsed.value = used }

    private var editingId: String? = null

    fun loadEntry(id: String) {
        editingId = id
        viewModelScope.launch {
            val entry = diaryRepository.getById(id) ?: return@launch
            _selectedDate.value = entry.date
            _selectedMood.value = entry.mood
            _title.value = entry.title ?: ""
            _voiceNotePath.value = entry.voiceNotePath
            _attachedImageUri.value = entry.imageUris
            _videoPath.value = entry.videoPath
            _contentLength.value = entry.contentPlain?.length ?: 0
            _loadedHtmlContent.value = entry.content
        }
    }

    fun saveEntry(
        htmlContent: String, 
        plainTextContent: String, 
        analyzeWithAI: Boolean, 
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        // Synchronous guard to immediately reject any multi-click / double-tap
        if (_isSaving.value || _isAnalyzing.value) {
            android.util.Log.w("NewEntryViewModel", "Save or analysis already in progress, ignoring subsequent tap")
            return
        }
        if (analyzeWithAI) {
            _isAnalyzing.value = true
        } else {
            _isSaving.value = true
        }

        val mood = _selectedMood.value ?: ""

        viewModelScope.launch {
            try {
                val oldEntry = editingId?.let { diaryRepository.getById(it) }
                val entryId = editingId ?: UUID.randomUUID().toString()
                val entry = DiaryEntry(
                    id = entryId,
                    date = _selectedDate.value,
                    title = _title.value.takeIf { it.isNotBlank() },
                    content = htmlContent,
                    contentPlain = plainTextContent,
                    mood = mood.uppercase(),
                    voiceNotePath = _voiceNotePath.value,
                    imageUris = _attachedImageUri.value,
                    videoPath = _videoPath.value,
                    hashtags = null,
                    aiSummary = null,
                    aiPattern = null,
                    aiNextStep = null,
                    createdAt = oldEntry?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                // If analyze with AI, call Gemini first before saving to DB
                val finalEntry = if (analyzeWithAI) {
                     try {
                         val result = GeminiApiService.analyzeEntry(plainTextContent, mood)
                         entry.copy(
                             aiSummary = result.summary.ifBlank { "You expressed your thoughts for today with clarity." },
                             aiPattern = result.pattern.ifBlank { "Journaling regularly builds emotional self-awareness." },
                             aiNextStep = result.nextStep.ifBlank { "Take a moment to relax and celebrate taking time for yourself." },
                             hashtags = result.hashtags?.joinToString(",")?.ifBlank { "reflection,mindfulness,journal" } ?: "reflection,mindfulness,journal"
                         )
                     } catch (e: Exception) {
                         val moodLabel = if (mood.isNotBlank()) mood.lowercase() else "thoughtful"
                         val defaultSummary = "Reflecting on your entry: You took time to express your feelings while feeling $moodLabel. Writing down thoughts helps create emotional balance and space."
                         val defaultPattern = "Consistent reflection helps you spot patterns in your daily thoughts and energy."
                         val defaultStep = "Take three deep breaths and give yourself credit for showing up today."
                         val defaultTags = "reflection,mindfulness,journal"
                         entry.copy(aiSummary = defaultSummary, aiPattern = defaultPattern, aiNextStep = defaultStep, hashtags = defaultTags)
                     }
                } else {
                     if (oldEntry != null) {
                         entry.copy(
                             aiSummary = oldEntry.aiSummary,
                             aiPattern = oldEntry.aiPattern,
                             aiNextStep = oldEntry.aiNextStep,
                             hashtags = oldEntry.hashtags
                         )
                     } else {
                         entry
                     }
                }

                val savedEntry = diaryRepository.insert(finalEntry)
                editingId = savedEntry.id
                diaryRepository.cleanUpDuplicates()
                moodRepository.cleanUpDuplicates()
                
                // Get supportive summary after successful save
                viewModelScope.launch {
                    try {
                        _reflectiveSummary.value = com.example.utils.GeminiApiService.getReflectiveSummary(plainTextContent, mood)
                    } catch (e: Exception) {
                        _reflectiveSummary.value = "Keep going, you're doing great!"
                    }
                }

                if (mood.isNotBlank() && (oldEntry == null || oldEntry.mood.uppercase() != mood.uppercase())) {
                    val moodLog = MoodLog(
                        id = UUID.randomUUID().toString(),
                        date = _selectedDate.value,
                        mood = mood.uppercase(),
                        createdAt = System.currentTimeMillis()
                    )
                    moodRepository.insert(moodLog)
                }

                // Achievements
                try {
                    com.example.utils.AchievementUnlocker.checkAndUnlock(
                        com.example.utils.AchievementEvent.EntryCreated,
                        achievementRepository,
                        diaryRepository
                    )
                    com.example.utils.AchievementUnlocker.checkAndUnlock(
                        com.example.utils.AchievementEvent.MoodLogged,
                        achievementRepository,
                        diaryRepository,
                        moodRepository
                    )
                    if (_voiceNotePath.value != null) {
                        com.example.utils.AchievementUnlocker.checkAndUnlock(
                            com.example.utils.AchievementEvent.VoiceNoteAdded,
                            achievementRepository
                        )
                    }
                    if (_attachedImageUri.value != null) {
                        com.example.utils.AchievementUnlocker.checkAndUnlock(
                            com.example.utils.AchievementEvent.ImageAdded,
                            achievementRepository
                        )
                    }
                    if (_stickerUsed.value) {
                        com.example.utils.AchievementUnlocker.checkAndUnlock(
                            com.example.utils.AchievementEvent.StickerUsed,
                            achievementRepository
                        )
                    }
                } catch(e: Exception) { }
                
                if (analyzeWithAI) {
                    try {
                        com.example.utils.AchievementUnlocker.checkAndUnlock(
                            com.example.utils.AchievementEvent.AIAnalysisRan,
                            achievementRepository,
                            diaryRepository = diaryRepository
                        )
                        if (finalEntry.aiPattern != null) {
                            com.example.utils.AchievementUnlocker.checkAndUnlock(
                                com.example.utils.AchievementEvent.PatternDetected,
                                achievementRepository
                            )
                        }
                    } catch(e: Exception) { }
                }

                onSuccess(savedEntry.id)
            } catch (e: Exception) {
                onError("Failed to save: ${e.message}")
            } finally {
                _isSaving.value = false
                _isAnalyzing.value = false
            }
        }
    }

    class Factory(
        private val diaryRepository: DiaryRepository,
        private val moodRepository: MoodRepository,
        private val achievementRepository: AchievementRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NewEntryViewModel(diaryRepository, moodRepository, achievementRepository) as T
        }
    }
}

package com.example.ui.screens.diary

import android.widget.TextView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.DiaryEntry
import com.example.data.repository.DiaryRepository
import com.example.di.AppContainer
import com.example.utils.GeminiApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.ui.components.*
import com.example.ui.theme.Spacing

class DiaryEntryDetailViewModel(
    private val entryId: String,
    private val diaryRepository: DiaryRepository
) : ViewModel() {
    private val _entry = MutableStateFlow<DiaryEntry?>(null)
    val entry = _entry.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    fun loadEntry() {
        // Handled reactively by flow collection
    }

    init {
        viewModelScope.launch {
            diaryRepository.getByIdFlow(entryId).collect {
                _entry.value = it
            }
        }
    }

    fun analyze() {
        val currentEntry = _entry.value ?: return
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val plainText = if (!currentEntry.contentPlain.isNullOrBlank()) {
                    currentEntry.contentPlain
                } else {
                    try {
                        HtmlCompat.fromHtml(currentEntry.content, HtmlCompat.FROM_HTML_MODE_COMPACT).toString().trim()
                    } catch (e: Exception) {
                        currentEntry.content
                    }
                }
                val result = GeminiApiService.analyzeEntry(plainText, currentEntry.mood)
                val updatedEntry = currentEntry.copy(
                    aiSummary = result.summary.ifBlank { "You reflected on your day with sincerity." },
                    aiPattern = result.pattern.ifBlank { "Regular journaling brings clarity." },
                    aiNextStep = result.nextStep.ifBlank { "Take a moment to pause and appreciate your progress." },
                    hashtags = result.hashtags?.joinToString(",")?.ifBlank { "reflection,mindfulness,journal" } ?: "reflection,mindfulness,journal"
                )
                diaryRepository.insert(updatedEntry)
                _entry.value = updatedEntry
            } catch (e: Exception) {
                val moodLabel = if (currentEntry.mood.isNotBlank()) currentEntry.mood.lowercase() else "thoughtful"
                val defaultSummary = "Reflecting on your entry: You took time to express your feelings while feeling $moodLabel. Writing down thoughts helps create emotional balance and space."
                val defaultPattern = "Consistent reflection helps you spot patterns in your daily thoughts and energy."
                val defaultStep = "Take three deep breaths and give yourself credit for showing up today."
                val defaultTags = "reflection,mindfulness,journal"
                val fallbackEntry = currentEntry.copy(
                    aiSummary = defaultSummary,
                    aiPattern = defaultPattern,
                    aiNextStep = defaultStep,
                    hashtags = defaultTags
                )
                diaryRepository.insert(fallbackEntry)
                _entry.value = fallbackEntry
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun deleteEntry(onDeleted: () -> Unit) {
        val currentEntry = _entry.value ?: return
        viewModelScope.launch {
            diaryRepository.delete(currentEntry)
            onDeleted()
        }
    }

    class Factory(
        private val entryId: String,
        private val diaryRepository: DiaryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DiaryEntryDetailViewModel(entryId, diaryRepository) as T
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEntryDetailScreen(
    entryId: String,
    appContainer: AppContainer,
    onBack: () -> Unit,
    onEdit: (String) -> Unit
) {
    val viewModel: DiaryEntryDetailViewModel = viewModel(
        factory = DiaryEntryDetailViewModel.Factory(entryId, appContainer.diaryRepository)
    )

    val entry by viewModel.entry.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadEntry()
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    var fullScreenVideoPath by remember { mutableStateOf<String?>(null) }

    var mediaPlayer: android.media.MediaPlayer? by remember { mutableStateOf(null) }
    var isPlayingVoice by remember { mutableStateOf(false) }

    val playVoiceNote = { path: String ->
        try {
            mediaPlayer?.release()
            val mp = android.media.MediaPlayer().apply {
                setDataSource(path)
                setOnPreparedListener { 
                    start() 
                    isPlayingVoice = true
                }
                setOnCompletionListener { 
                    reset()
                    release()
                    isPlayingVoice = false
                }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            // fail-safe
        }
    }

    val stopVoiceNote = {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
            isPlayingVoice = false
        } catch(e: Exception) {}
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete this entry?", style = MaterialTheme.typography.titleLarge) },
            text = { Text("This cannot be undone.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteEntry(onDeleted = {
                        scope.launch {
                            appContainer.notificationPreferencesRepository.updateUnfinishedDraftPending(false)
                            appContainer.notificationManager.cancelNotification(com.example.notification.NotificationManager.ID_DRAFT_REMINDER)
                        }
                        onBack()
                    })
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (fullScreenImageUrl != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { fullScreenImageUrl = null }) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black).clickable { fullScreenImageUrl = null }, contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Image(
                    painter = coil.compose.rememberAsyncImagePainter(fullScreenImageUrl),
                    contentDescription = "Full screen image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
                IconButton(
                    onClick = { fullScreenImageUrl = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(TablerIcons.X, "Close", tint = Color.White)
                }
            }
        }
    }

    if (fullScreenVideoPath != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { fullScreenVideoPath = null }) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black).clickable { fullScreenVideoPath = null }, contentAlignment = Alignment.Center) {
                AndroidView(
                    factory = { context ->
                        android.widget.VideoView(context).apply {
                            setVideoPath(fullScreenVideoPath)
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                start()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { fullScreenVideoPath = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(TablerIcons.X, "Close", tint = Color.White)
                }
            }
        }
    }

    val darkTheme = com.example.ui.theme.LocalIsDarkTheme.current
    val screenBg = if (darkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardBg = if (darkTheme) Color(0xFF1E293B) else Color.White
    val cardBorder = if (darkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
    val titleTextColor = if (darkTheme) Color.White else Color(0xFF1E293B)
    val subtextColor = if (darkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(modifier = Modifier.fillMaxSize().background(screenBg).navigationBarsPadding()) {
        TopAppBar(
            title = {
                Text(
                    "Entry Detail",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = titleTextColor
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(TablerIcons.ArrowLeft, contentDescription = "Back", tint = titleTextColor)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        entry?.let { currentEntry ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.cardPaddingLarge, vertical = Spacing.small),
                verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap)
            ) {
                // Entry Card
                SoulCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = cardBg,
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(Spacing.cardPaddingLarge)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            val moodConfig = com.example.ui.screens.home.getMoodConfig(currentEntry.mood)
                            Box(
                                modifier = Modifier.size(44.dp).background(moodConfig.tintColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = moodConfig.icon,
                                    contentDescription = moodConfig.label,
                                    tint = moodConfig.tintColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(Spacing.medium))
                            Column(modifier = Modifier.weight(1f)) {
                                val dateStr = try {
                                    LocalDate.parse(currentEntry.date).format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.US))
                                } catch (e: Exception) { currentEntry.date }
                                Text(dateStr, style = MaterialTheme.typography.labelSmall, color = subtextColor)
                                Text(currentEntry.mood.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, style = MaterialTheme.typography.labelMedium, color = moodConfig.tintColor)
                            }
                            IconButton(onClick = { onEdit(currentEntry.id) }) {
                                Icon(TablerIcons.Pencil, contentDescription = "Edit", tint = subtextColor)
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(TablerIcons.Trash, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        if (!currentEntry.title.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(Spacing.medium))
                            Text(currentEntry.title!!, style = MaterialTheme.typography.headlineSmall, color = titleTextColor)
                        }

                        Spacer(modifier = Modifier.height(Spacing.medium))
                        val textContentColorArgb = if (darkTheme) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#1E293B")
                        AndroidView(
                            factory = { context ->
                                TextView(context).apply {
                                    textSize = 16f
                                    setLineSpacing(8f, 1f)
                                }
                            },
                            update = { view ->
                                view.setTextColor(textContentColorArgb)
                                view.text = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                    android.text.Html.fromHtml(currentEntry.content, android.text.Html.FROM_HTML_MODE_COMPACT)
                                } else {
                                    @Suppress("DEPRECATION")
                                    android.text.Html.fromHtml(currentEntry.content)
                                }
                            }
                        )

                        currentEntry.imageUris?.let { uri ->
                            Spacer(modifier = Modifier.height(Spacing.sectionGap))
                            SoulCard(
                                modifier = Modifier.fillMaxWidth().height(200.dp).clickable { fullScreenImageUrl = uri },
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = coil.compose.rememberAsyncImagePainter(uri),
                                    contentDescription = "Attached Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                        }

                        currentEntry.videoPath?.let { path ->
                            Spacer(modifier = Modifier.height(Spacing.sectionGap))
                            Text("Attached Video", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            SoulCard(
                                modifier = Modifier.fillMaxWidth().height(200.dp).clickable { fullScreenVideoPath = path },
                                containerColor = Color.Black
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    var isPlayingVideo by remember { mutableStateOf(false) }
                                    var videoViewInstance: android.widget.VideoView? by remember { mutableStateOf(null) }

                                    AndroidView(
                                        factory = { context ->
                                            android.widget.VideoView(context).apply {
                                                setVideoPath(path)
                                                setOnPreparedListener { mp ->
                                                    mp.isLooping = true
                                                }
                                                setOnCompletionListener {
                                                    isPlayingVideo = false
                                                }
                                            }.also { videoViewInstance = it }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    if (!isPlayingVideo) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                                .clickable {
                                                    videoViewInstance?.start()
                                                    isPlayingVideo = true
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = TablerIcons.PlayerPlay,
                                                contentDescription = "Play Video",
                                                tint = Color.White,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable {
                                                    videoViewInstance?.pause()
                                                    isPlayingVideo = false
                                                }
                                        )
                                    }
                                }
                            }
                        }

                        currentEntry.voiceNotePath?.let { path ->
                            Spacer(modifier = Modifier.height(Spacing.sectionGap))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (isPlayingVoice) {
                                                stopVoiceNote()
                                            } else {
                                                playVoiceNote(path)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingVoice) TablerIcons.PlayerStop else TablerIcons.PlayerPlay,
                                            contentDescription = if (isPlayingVoice) "Stop playback" else "Play voice note",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Voice Note Recording", style = MaterialTheme.typography.labelMedium)
                                        Text(if (isPlayingVoice) "Playing..." else "Click to listen", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                // AI Coach Card
                if (currentEntry.aiSummary != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, cardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Header Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        TablerIcons.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "A note from your coach",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = titleTextColor,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "AI Insight",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Helper to strip raw markdown asterisks
                            fun cleanAiText(s: String): String = s.replace("**", "").replace("*", "").trim()

                            // SUMMARY
                            Text(
                                text = "SUMMARY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = subtextColor,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cleanAiText(currentEntry.aiSummary ?: ""),
                                fontSize = 14.sp,
                                color = titleTextColor,
                                lineHeight = 20.sp
                            )

                            // PATTERN
                            if (!currentEntry.aiPattern.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "PATTERN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = subtextColor,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cleanAiText(currentEntry.aiPattern!!),
                                    fontSize = 14.sp,
                                    color = titleTextColor,
                                    lineHeight = 20.sp
                                )
                            }

                            // ONE SMALL STEP
                            if (!currentEntry.aiNextStep.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "ONE SMALL STEP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = cleanAiText(currentEntry.aiNextStep!!),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = titleTextColor,
                                        lineHeight = 20.sp
                                    )
                                }
                            }

                            // HASHTAGS
                            val hashtags = currentEntry.hashtags?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
                            if (hashtags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(hashtags) { tag ->
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(if (darkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = if (tag.trim().startsWith("#")) tag.trim() else "#${tag.trim()}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            if (isAnalyzing) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Re-analyzing with AI...", style = MaterialTheme.typography.bodyMedium, color = titleTextColor)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { viewModel.analyze() },
                                    shape = RoundedCornerShape(50.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Icon(TablerIcons.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Re-analyze with AI", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                } else {
                    if (isAnalyzing) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, cardBorder)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Reflecting on your entry with AI...", style = MaterialTheme.typography.bodyMedium, color = titleTextColor)
                            }
                        }
                    } else {
                        SoulButton(
                            text = "Analyze with AI",
                            icon = TablerIcons.Star,
                            onClick = { viewModel.analyze() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

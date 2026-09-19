package com.example.ui.screens.diary

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.Html
import android.text.InputFilter
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.widget.EditText
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.Dispatchers
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.di.AppContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.ui.components.*

// Custom EditText subclass to capture cursor and selection changes and ensure accessibility inside scroll views
class DiaryEditText(context: Context) : androidx.appcompat.widget.AppCompatEditText(context) {
    var onSelectionChangedListener: ((start: Int, end: Int) -> Unit)? = null

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        onSelectionChangedListener?.invoke(selStart, selEnd)
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            android.view.MotionEvent.ACTION_UP -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                requestFocus()
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
                imm?.showSoftInput(this, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            }
            android.view.MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.onTouchEvent(event)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryScreen(
    appContainer: AppContainer,
    onBack: () -> Unit,
    onEntrySaved: (String) -> Unit,
    showHistoryToggle: Boolean = true,
    onHistoryClick: (() -> Unit)? = null,
    entryId: String? = null
) {
    val viewModel: NewEntryViewModel = viewModel(
        factory = NewEntryViewModel.Factory(
            appContainer.diaryRepository,
            appContainer.moodRepository,
            appContainer.achievementRepository
        )
    )

    // Preload entry if editing
    LaunchedEffect(entryId) {
        if (entryId != null) {
            viewModel.loadEntry(entryId)
        }
    }

    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedMood by viewModel.selectedMood.collectAsState()
    val title by viewModel.title.collectAsState()
    val contentLength by viewModel.contentLength.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val voiceNotePath by viewModel.voiceNotePath.collectAsState()
    val attachedImageUri by viewModel.attachedImageUri.collectAsState()
    val videoPath by viewModel.videoPath.collectAsState()

    var editText: DiaryEditText? by remember { mutableStateOf(null) }
    val context = LocalContext.current
    val darkTheme = com.example.ui.theme.LocalIsDarkTheme.current
    val scope = rememberCoroutineScope()

    // Color definitions matching exact screenshots
    val bgColor = if (darkTheme) Color(0xFF0F172A) else Color(0xFFFAFAFA)
    val cardBgColor = if (darkTheme) Color(0xFF1E293B) else Color.White
    val cardBorderColor = if (darkTheme) Color(0xFF334155) else Color(0xFFF3F4F6)
    val subtextColor = if (darkTheme) Color(0xFF94A3B8) else Color(0xFF71717A)
    val titleTextColor = if (darkTheme) Color.White else Color(0xFF1E1E1E)
    val primaryPurple = MaterialTheme.colorScheme.primary
    val darkPurpleText = MaterialTheme.colorScheme.onPrimary

    // Loaded HTML formatted text
    val loadedHtmlContent by viewModel.loadedHtmlContent.collectAsState()
    var hasPrefilledText by remember { mutableStateOf(false) }

    LaunchedEffect(loadedHtmlContent, editText) {
        val html = loadedHtmlContent
        val et = editText
        if (!html.isNullOrBlank() && et != null && !hasPrefilledText) {
            val spanned = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(html)
            }
            et.setText(spanned)
            et.setSelection(spanned.length)
            hasPrefilledText = true
        }
    }

    // Formatting active button highlights
    var isBoldActive by remember { mutableStateOf(false) }
    var isItalicActive by remember { mutableStateOf(false) }
    var isBulletActive by remember { mutableStateOf(false) }

    // Media and voice state
    var isRecording by remember { mutableStateOf(false) }
    var recordTimeSeconds by remember { mutableStateOf(0) }
    var currentVoiceFile by remember { mutableStateOf<File?>(null) }
    var mediaRecorder: android.media.MediaRecorder? by remember { mutableStateOf(null) }

    // Video media limit check errors
    var videoErrorMsg by remember { mutableStateOf<String?>(null) }
    var validationErrorMsg by remember { mutableStateOf<String?>(null) }
    var isDirty by remember { mutableStateOf(false) }

    // Progress triggers
    var isUploadingMedia by remember { mutableStateOf(false) }
    var mediaProgress by remember { mutableStateOf(0f) }

    // Dynamic dialog states
    var showCalendarDialog by remember { mutableStateOf(false) }
    var showEmojiDialog by remember { mutableStateOf(false) }

    // Waveform scale factor animation
    val animWaveform = rememberInfiniteTransition(label = "pulse_wf")
    val scaleFactor by animWaveform.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsing_wf"
    )

    // Preview audio playback states
    var isPlayingPreview by remember { mutableStateOf(false) }
    var previewPlayer: android.media.MediaPlayer? by remember { mutableStateOf(null) }
    var previewDurationMs by remember { mutableStateOf(1) }
    var previewPositionMs by remember { mutableStateOf(0) }

    LaunchedEffect(isPlayingPreview) {
        if (isPlayingPreview) {
            while (isPlayingPreview) {
                previewPlayer?.let {
                    try {
                        if (it.isPlaying) {
                            previewPositionMs = it.currentPosition
                        }
                    } catch (e: Exception) {}
                }
                delay(150)
            }
        }
    }

    var amplitudes by remember { mutableStateOf(List(30) { 0.1f }) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordTimeSeconds = 0
            var ticks = 0
            while (isRecording) {
                val amp = mediaRecorder?.maxAmplitude ?: 0
                val normalized = (amp / 32767f).coerceIn(0f, 1f)
                val displayAmp = if (normalized < 0.05f) (5..15).random() / 100f else normalized
                amplitudes = (amplitudes.drop(1) + displayAmp)
                delay(100)
                ticks++
                if (ticks % 10 == 0) {
                    recordTimeSeconds++
                }
            }
        } else {
            amplitudes = List(30) { 0.1f }
        }
    }

    // Permission and media pickers
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            android.widget.Toast.makeText(context, "Microphone permission required for voice notes.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isUploadingMedia = true
            scope.launch {
                for (p in 1..10) {
                    mediaProgress = p / 10f
                    delay(80)
                }
                isUploadingMedia = false
                
                val savedFile = kotlinx.coroutines.withContext(Dispatchers.IO) {
                    try {
                        val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                        val ext = if (mime.contains("png")) "png" else if (mime.contains("webp")) "webp" else "jpg"
                        val mediaDir = File(context.filesDir, "media").apply { if (!exists()) mkdirs() }
                        val file = File(mediaDir, "img_${System.currentTimeMillis()}_${(1000..9999).random()}.$ext")
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            file.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        if (file.exists() && file.length() > 0) file.absolutePath else uri.toString()
                    } catch (e: Exception) {
                        uri.toString()
                    }
                }
                
                val currentImages = attachedImageUri ?: ""
                val newImages = if (currentImages.isBlank()) savedFile else "$currentImages,$savedFile"
                viewModel.setAttachedImage(newImages)
                isDirty = true
            }
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isUploadingMedia = true
            videoErrorMsg = null
            scope.launch {
                for (p in 1..10) {
                    mediaProgress = p / 10f
                    delay(120)
                }
                isUploadingMedia = false

                var isValid = true
                try {
                    val fd = context.contentResolver.openAssetFileDescriptor(uri, "r")
                    val sizeBytes = fd?.length ?: 0L
                    fd?.close()

                    val retriever = android.media.MediaMetadataRetriever()
                    retriever.setDataSource(context, uri)
                    val durationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val durationMs = durationStr?.toLongOrNull() ?: 0L
                    retriever.release()

                    val sizeMB = sizeBytes / (1024f * 1024f)
                    val durationSeconds = durationMs / 1000f

                    if (durationSeconds > 60 || sizeMB > 50) {
                        isValid = false
                        videoErrorMsg = "Video must be under 60s and 50MB"
                    }
                } catch (e: Exception) {
                    videoErrorMsg = "Unable to process video file limits"
                    isValid = false
                }

                if (isValid) {
                    val savedVideoPath = kotlinx.coroutines.withContext(Dispatchers.IO) {
                        try {
                            val mediaDir = File(context.filesDir, "media").apply { if (!exists()) mkdirs() }
                            val file = File(mediaDir, "vid_${System.currentTimeMillis()}.mp4")
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                file.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            if (file.exists() && file.length() > 0) file.absolutePath else uri.toString()
                        } catch (e: Exception) {
                            uri.toString()
                        }
                    }
                    viewModel.setVideoPath(savedVideoPath)
                    isDirty = true
                }
            }
        }
    }

    val startRecording = {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        } else {
            try {
                isPlayingPreview = false
                previewPlayer?.release()
                previewPlayer = null

                val file = File(context.filesDir, "voice_${System.currentTimeMillis()}.m4a")
                val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    android.media.MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    android.media.MediaRecorder()
                }

                recorder.apply {
                    setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
                    setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(file.absolutePath)
                    prepare()
                    start()
                }
                mediaRecorder = recorder
                currentVoiceFile = file
                isRecording = true
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Failed to start recording", android.widget.Toast.LENGTH_SHORT).show()
                isRecording = false
            }
        }
    }

    val stopRecording = {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {}
        mediaRecorder = null
        isRecording = false

        currentVoiceFile?.let { file ->
            if (file.exists() && file.length() > 0) {
                viewModel.setVoiceNote(file.absolutePath)
                isDirty = true
                previewDurationMs = recordTimeSeconds * 1000
            }
        }
    }

    val playVoiceNote = { path: String ->
        try {
            previewPlayer?.release()
            val mp = android.media.MediaPlayer().apply {
                setDataSource(path)
                setOnPreparedListener {
                    previewDurationMs = it.duration.coerceAtLeast(1)
                    it.start()
                    isPlayingPreview = true
                }
                setOnCompletionListener {
                    isPlayingPreview = false
                    previewPositionMs = 0
                }
                prepareAsync()
            }
            previewPlayer = mp
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Playback error", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val stopVoiceNote = {
        try {
            previewPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {}
        previewPlayer = null
        isPlayingPreview = false
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaRecorder?.release()
                previewPlayer?.release()
            } catch (e: Exception) {}
        }
    }

    val reflectionPromptPool = listOf(
        "What small win today brought you the most peace while staying focused on your goals?",
        "Who or what made you feel genuinely supported and understood today?",
        "What is one thing you are grateful for that you almost overlooked today?",
        "Where did you feel most like yourself today?",
        "What emotion showed up most today and what do you think caused it?",
        "If today had a title, what would it be?",
        "What recent small win or moment of connection brought a smile to your face?"
    )

    val selectedReflectionPrompt = remember(selectedDate) {
        val parsed = try { LocalDate.parse(selectedDate) } catch (e: Exception) { LocalDate.now() }
        val index = Math.abs(parsed.toEpochDay().toInt()) % reflectionPromptPool.size
        reflectionPromptPool[index]
    }

    val executeSave = { triggerAi: Boolean ->
        if (isSaving || isAnalyzing) {
            // Prevent duplicate taps while saving or analyzing
        } else {
            validationErrorMsg = null
            val currentEt = editText
            val (cleanHtml, cleanPlain) = if (currentEt != null) {
                val spanned = currentEt.text as? android.text.Spanned
                val rawHtml = if (spanned != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        Html.toHtml(spanned, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
                    } else {
                        @Suppress("DEPRECATION")
                        Html.toHtml(spanned)
                    }
                } else {
                    currentEt.text?.toString() ?: ""
                }
                val h = rawHtml.trim()
                val p = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    Html.fromHtml(h, Html.FROM_HTML_MODE_COMPACT).toString().trim()
                } else {
                    @Suppress("DEPRECATION")
                    Html.fromHtml(h).toString().trim()
                }
                Pair(h, p)
            } else {
                val h = loadedHtmlContent?.ifBlank { "" } ?: ""
                val p = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    Html.fromHtml(h, Html.FROM_HTML_MODE_COMPACT).toString().trim()
                } else {
                    @Suppress("DEPRECATION")
                    Html.fromHtml(h).toString().trim()
                }
                Pair(h, p)
            }

            viewModel.saveEntry(cleanHtml, cleanPlain, triggerAi, { savedId ->
                scope.launch {
                    appContainer.notificationPreferencesRepository.updateUnfinishedDraftPending(false)
                    appContainer.notificationManager.cancelNotification(com.example.notification.NotificationManager.ID_DRAFT_REMINDER)
                }
                onEntrySaved(savedId)
            }, { err ->
                android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
            })
            isDirty = false
        }
    }

    Scaffold(
        containerColor = bgColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // 1. Back Header Button & History Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.clickable { onBack() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = TablerIcons.ChevronLeft,
                        contentDescription = "Home",
                        tint = subtextColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Home",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = subtextColor
                    )
                }

                if (showHistoryToggle && onHistoryClick != null) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onHistoryClick() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = TablerIcons.History,
                            contentDescription = "History",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "History",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 2. Title & Subtitle
            Text(
                text = "New entry",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pick a date, set a mood, write a little.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = subtextColor
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Today's Reflection Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        editText?.let { et ->
                            val promptToInsert = selectedReflectionPrompt
                            val currentText = et.text?.toString() ?: ""
                            if (currentText.isBlank()) {
                                et.setText(promptToInsert)
                                et.setSelection(promptToInsert.length)
                            } else if (!currentText.contains(promptToInsert)) {
                                val newText = "$currentText\n\n$promptToInsert\n"
                                et.setText(newText)
                                et.setSelection(newText.length)
                            }
                        }
                    },
                shape = RoundedCornerShape(20.dp),
                color = if (darkTheme) Color(0xFF1E293B) else Color(0xFFF9FAFB),
                border = BorderStroke(1.dp, if (darkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = TablerIcons.Bulb,
                            contentDescription = "Reflection",
                            tint = Color(0xFF78716C),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "TODAY'S REFLECTION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF78716C),
                            letterSpacing = 0.8.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "\"$selectedReflectionPrompt\"",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = titleTextColor,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Date Selector Pill Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = cardBgColor,
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Prev
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                val d = LocalDate.parse(selectedDate).minusDays(1)
                                viewModel.setDate(d.format(DateTimeFormatter.ISO_LOCAL_DATE))
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = TablerIcons.ChevronLeft,
                            contentDescription = "Prev",
                            tint = subtextColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prev", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = subtextColor)
                    }

                    // Today Pill Center
                    val parsedToday = LocalDate.now()
                    val selectedLocalDate = try { LocalDate.parse(selectedDate) } catch (e: Exception) { parsedToday }
                    val isSelectedToday = selectedLocalDate == parsedToday
                    val dateLabel = if (isSelectedToday) "Today" else selectedLocalDate.format(DateTimeFormatter.ofPattern("MMM d", Locale.US))

                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = if (darkTheme) Color(0xFF334155) else Color.White,
                        border = BorderStroke(1.dp, if (darkTheme) Color(0xFF475569) else Color(0xFFE2E8F0)),
                        modifier = Modifier.clickable { showCalendarDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = TablerIcons.Calendar,
                                contentDescription = "Calendar",
                                tint = titleTextColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = dateLabel,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = titleTextColor
                            )
                        }
                    }

                    // Next
                    val isNextEnabled = !isSelectedToday && !selectedLocalDate.isAfter(parsedToday)
                    Row(
                        modifier = Modifier
                            .alpha(if (isNextEnabled) 1f else 0.4f)
                            .clip(CircleShape)
                            .clickable(enabled = isNextEnabled) {
                                val d = selectedLocalDate.plusDays(1)
                                viewModel.setDate(d.format(DateTimeFormatter.ISO_LOCAL_DATE))
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Next", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isNextEnabled) subtextColor else Color.LightGray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = TablerIcons.ChevronRight,
                            contentDescription = "Next",
                            tint = if (isNextEnabled) subtextColor else Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. How are you feeling?
            Text(
                text = "How are you feeling?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Mood items exact from screenshots
            val moodsList = listOf(
                MoodItemData("Happy", "HAPPY", TablerIcons.Sun, Color(0xFFFEF3C7), Color(0xFFF59E0B)),
                MoodItemData("Loved", "LOVED", TablerIcons.Heart, Color(0xFFFCE7F3), Color(0xFFEC4899)),
                MoodItemData("Neutral", "NEUTRAL", TablerIcons.MoodSmile, Color(0xFFF1F5F9), Color(0xFF64748B)),
                MoodItemData("Anxious", "ANXIOUS", TablerIcons.Wind, Color(0xFFEDE9FE), Color(0xFF8B5CF6)),
                MoodItemData("Sad", "SAD", TablerIcons.Droplet, Color(0xFFE0F2FE), Color(0xFF0284C7)),
                MoodItemData("Angry", "ANGRY", TablerIcons.Flame, Color(0xFFFFEDD5), Color(0xFFF97316)),
                MoodItemData("Numb", "NUMB", TablerIcons.Cloud, Color(0xFFF1F5F9), Color(0xFF64748B))
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 4.dp)
            ) {
                items(moodsList) { item ->
                    val isSelected = selectedMood?.uppercase() == item.key
                    Surface(
                        modifier = Modifier
                            .width(88.dp)
                            .height(104.dp)
                            .clickable { viewModel.setMood(item.key) },
                        shape = RoundedCornerShape(20.dp),
                        color = cardBgColor,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) item.iconColor else cardBorderColor
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(item.circleBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = item.iconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = titleTextColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. Your Entry Section
            Text(
                text = "Your entry",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Entry Outer Box Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = cardBgColor,
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Title Input Box
                    OutlinedTextField(
                        value = title,
                        onValueChange = viewModel::setTitle,
                        placeholder = {
                            Text(
                                "Title (optional)",
                                fontSize = 15.sp,
                                color = Color(0xFF94A3B8)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (darkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (darkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = titleTextColor,
                            unfocusedTextColor = titleTextColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Formatting Toolbar Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Bold
                        IconButton(
                            onClick = {
                                editText?.let { et ->
                                    val start = et.selectionStart
                                    val end = et.selectionEnd
                                    val s = et.text
                                    if (s != null && start >= 0 && end >= 0 && start != end) {
                                        val spans = s.getSpans(start, end, StyleSpan::class.java)
                                        val boldSpans = spans.filter { it.style == Typeface.BOLD }
                                        if (boldSpans.isNotEmpty()) {
                                            boldSpans.forEach { s.removeSpan(it) }
                                            isBoldActive = false
                                        } else {
                                            s.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                            isBoldActive = true
                                        }
                                    } else {
                                        isBoldActive = !isBoldActive
                                    }
                                    et.requestFocus()
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text(
                                "B",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBoldActive) MaterialTheme.colorScheme.primary else subtextColor
                            )
                        }

                        // Italic
                        IconButton(
                            onClick = {
                                editText?.let { et ->
                                    val start = et.selectionStart
                                    val end = et.selectionEnd
                                    val s = et.text
                                    if (s != null && start >= 0 && end >= 0 && start != end) {
                                        val spans = s.getSpans(start, end, StyleSpan::class.java)
                                        val italicSpans = spans.filter { it.style == Typeface.ITALIC }
                                        if (italicSpans.isNotEmpty()) {
                                            italicSpans.forEach { s.removeSpan(it) }
                                            isItalicActive = false
                                        } else {
                                            s.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                            isItalicActive = true
                                        }
                                    } else {
                                        isItalicActive = !isItalicActive
                                    }
                                    et.requestFocus()
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text(
                                "I",
                                fontSize = 17.sp,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Bold,
                                color = if (isItalicActive) MaterialTheme.colorScheme.primary else subtextColor
                            )
                        }

                        // Bullet List
                        IconButton(
                            onClick = {
                                editText?.let { et ->
                                    val start = et.selectionStart
                                    val end = et.selectionEnd
                                    val s = et.text
                                    if (s != null && start >= 0) {
                                        if (start != end) {
                                            val selectedText = s.substring(start, end)
                                            if (selectedText.contains("• ")) {
                                                val unbulleted = selectedText.replace("• ", "")
                                                s.replace(start, end, unbulleted)
                                                isBulletActive = false
                                            } else {
                                                val bulleted = selectedText.lines().joinToString("\n") { if (it.startsWith("• ")) it else "• $it" }
                                                s.replace(start, end, bulleted)
                                                isBulletActive = true
                                            }
                                        } else {
                                            val textStr = s.toString()
                                            val startLineIdx = textStr.lastIndexOf('\n', (start - 1).coerceAtLeast(0))
                                            val lineStart = if (startLineIdx == -1) 0 else startLineIdx + 1
                                            val currentLine = if (lineStart <= start) textStr.substring(lineStart, start) else ""
                                            if (currentLine.startsWith("• ")) {
                                                s.delete(lineStart, lineStart + 2)
                                                isBulletActive = false
                                            } else {
                                                s.insert(lineStart, "• ")
                                                isBulletActive = true
                                            }
                                        }
                                    }
                                    et.requestFocus()
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = TablerIcons.List,
                                contentDescription = "List",
                                tint = if (isBulletActive) MaterialTheme.colorScheme.primary else subtextColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Vertical Divider Line
                        Divider(
                            modifier = Modifier
                                .height(20.dp)
                                .width(1.dp),
                            color = Color(0xFFE2E8F0)
                        )

                        // Image Picker Icon Button
                        IconButton(
                            onClick = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = TablerIcons.Photo,
                                contentDescription = "Image",
                                tint = subtextColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Emoji Picker Icon Button
                        IconButton(
                            onClick = { showEmojiDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = TablerIcons.MoodSmile,
                                contentDescription = "Emoji",
                                tint = subtextColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Text Editor Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                DiaryEditText(ctx).apply {
                                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                    hint = "What happened today? Be honest, this space is yours."
                                    setHintTextColor(android.graphics.Color.parseColor("#94A3B8"))
                                    setPadding(0, 8, 0, 8)
                                    gravity = android.view.Gravity.TOP or android.view.Gravity.START
                                    minLines = 7
                                    inputType = android.text.InputType.TYPE_CLASS_TEXT or 
                                                android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or 
                                                android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                                    isFocusable = true
                                    isFocusableInTouchMode = true
                                    textSize = 15f
                                    filters = arrayOf(InputFilter.LengthFilter(5000))
                                    val textColor = if (darkTheme) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#1E293B")
                                    setTextColor(textColor)

                                    addTextChangedListener(object: TextWatcher {
                                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                            if ((s?.length ?: 0) > 0) {
                                                scope.launch {
                                                    appContainer.notificationPreferencesRepository.updateUnfinishedDraftPending(true)
                                                }
                                            }
                                            viewModel.setContentLength(s?.length ?: 0)
                                            isDirty = true
                                            
                                            if (s != null && count > 0 && before == 0) {
                                                val startOfText = start
                                                val endOfText = start + count
                                                post {
                                                    if (isBoldActive) {
                                                        this@apply.text?.setSpan(StyleSpan(Typeface.BOLD), startOfText, endOfText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                                    }
                                                    if (isItalicActive) {
                                                        this@apply.text?.setSpan(StyleSpan(Typeface.ITALIC), startOfText, endOfText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                                    }
                                                }
                                            }

                                            if (s != null && count == 1 && s[start] == '\n' && isBulletActive) {
                                                post {
                                                    this@apply.text?.insert(start + 1, "• ")
                                                }
                                            }
                                        }
                                        override fun afterTextChanged(s: Editable?) {}
                                    })

                                    onSelectionChangedListener = { start, end ->
                                        if (start != end) {
                                            val spans = this@apply.text?.getSpans(start, end, StyleSpan::class.java) ?: emptyArray()
                                            isBoldActive = spans.any { it.style == Typeface.BOLD }
                                            isItalicActive = spans.any { it.style == Typeface.ITALIC }
                                            isBulletActive = this@apply.text?.substring(start, end)?.contains("•") == true
                                        }
                                    }
                                    editText = this
                                }
                            },
                            update = { et ->
                                val textColor = if (darkTheme) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#1E293B")
                                et.setTextColor(textColor)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 180.dp)
                        )
                    }

                    if (isUploadingMedia) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = mediaProgress,
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = Color(0xFF3B82F6),
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Voice Note / Upload / Video attachment controls exact to screenshots
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Record Voice Note Pill Button
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = if (isRecording) Color(0xFFFEE2E2) else (if (darkTheme) Color(0xFF1E293B) else Color.White),
                                border = BorderStroke(1.dp, if (isRecording) Color(0xFFEF4444) else (if (darkTheme) Color(0xFF334155) else Color(0xFFE2E8F0))),
                                modifier = Modifier.clickable {
                                    if (isRecording) stopRecording() else startRecording()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = TablerIcons.Microphone,
                                        contentDescription = "Voice note",
                                        tint = if (isRecording) Color(0xFFEF4444) else titleTextColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isRecording) "Recording (${recordTimeSeconds}s)..." else "Record voice note",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isRecording) Color(0xFFEF4444) else titleTextColor
                                    )
                                }
                            }

                            // Or Upload Button
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        imagePickerLauncher.launch(
                                            PickVisualMediaRequest(
                                                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = TablerIcons.Upload,
                                    contentDescription = "Upload",
                                    tint = subtextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Or upload",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = subtextColor
                                )
                            }
                        }

                        // Add Video Button (60s · 50MB)
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = if (darkTheme) Color(0xFF1E293B) else Color.White,
                            border = BorderStroke(1.dp, if (darkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)),
                            modifier = Modifier.clickable {
                                videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = TablerIcons.Video,
                                    contentDescription = "Video",
                                    tint = titleTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Add video",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = titleTextColor
                                )
                                Text(
                                    text = "60s · 50MB",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = subtextColor
                                )
                            }
                        }

                        if (videoErrorMsg != null) {
                            Text(
                                text = videoErrorMsg!!,
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp
                            )
                        }

                        // Display attached video preview if present
                        videoPath?.let { path ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (darkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = TablerIcons.Video,
                                            contentDescription = "Video attached",
                                            tint = Color(0xFF3B82F6),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text("Video attached", fontSize = 14.sp, color = titleTextColor)
                                    }
                                    IconButton(
                                        onClick = { viewModel.setVideoPath(null) }
                                    ) {
                                        Icon(TablerIcons.Trash, "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        // Display recorded voice note preview if present
                        voiceNotePath?.let { path ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (darkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (isPlayingPreview) stopVoiceNote() else playVoiceNote(path)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isPlayingPreview) TablerIcons.PlayerPause else TablerIcons.PlayerPlay,
                                                contentDescription = "Play voice note",
                                                tint = Color(0xFF3B82F6)
                                            )
                                        }
                                        Text("Voice note attached", fontSize = 14.sp, color = titleTextColor)
                                    }
                                    IconButton(
                                        onClick = {
                                            stopVoiceNote()
                                            viewModel.setVoiceNote(null)
                                        }
                                    ) {
                                        Icon(TablerIcons.Trash, "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        // Display attached images row
                        val attachedUrisList = attachedImageUri?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
                        if (attachedUrisList.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(attachedUrisList) { uri ->
                                    Box(modifier = Modifier.size(64.dp)) {
                                        Image(
                                            painter = rememberAsyncImagePainter(uri),
                                            contentDescription = "Photo",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .align(Alignment.TopEnd)
                                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                                .clickable {
                                                    val updated = attachedUrisList.filter { it != uri }.joinToString(",")
                                                    viewModel.setAttachedImage(updated.ifBlank { null })
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(TablerIcons.X, "Delete", tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Character counter at bottom left/right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "$contentLength/5000",
                            fontSize = 13.sp,
                            color = subtextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (validationErrorMsg != null) {
                Text(
                    text = validationErrorMsg!!,
                    color = Color(0xFFEF4444),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 7. Bottom Action Buttons exact match:
            // Left: [ 💾 Save ] (white pill button with border)
            // Right: [ ✨ Analyze with AI ] (pastel purple pill button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Save Button
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = if (darkTheme) Color(0xFF1E293B) else Color.White,
                    border = BorderStroke(1.dp, if (darkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable(enabled = !isSaving && !isAnalyzing) {
                            val currentTextLength = editText?.text?.toString()?.trim()?.length ?: contentLength
                            if (selectedMood == null && currentTextLength == 0 && voiceNotePath.isNullOrBlank()) {
                                validationErrorMsg = "Pick a mood or write something first"
                            } else {
                                executeSave(false)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = titleTextColor
                            )
                        } else {
                            Icon(
                                imageVector = TablerIcons.FileText,
                                contentDescription = "Save",
                                tint = if (isAnalyzing) titleTextColor.copy(alpha = 0.5f) else titleTextColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSaving) "Saving..." else "Save",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isAnalyzing) titleTextColor.copy(alpha = 0.5f) else titleTextColor
                        )
                    }
                }

                // Analyze with AI Button
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = if (isSaving || isAnalyzing) primaryPurple.copy(alpha = 0.6f) else primaryPurple,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                        .clickable(enabled = !isSaving && !isAnalyzing) {
                            val currentTextLength = editText?.text?.toString()?.trim()?.length ?: contentLength
                            if (currentTextLength < 5 && voiceNotePath.isNullOrBlank()) {
                                validationErrorMsg = "Please write a short reflection or record a voice note for AI analysis"
                            } else {
                                executeSave(true)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = darkPurpleText
                            )
                        } else {
                            Icon(
                                imageVector = TablerIcons.Star,
                                contentDescription = "Analyze",
                                tint = darkPurpleText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAnalyzing) "Analyzing..." else "Analyze with AI",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkPurpleText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Emoji Picker Dialog
    if (showEmojiDialog) {
        val emojis = listOf(
            "❤️", "😊", "🌟", "🌧️", "🔥", "🍃", "🧘", "☕",
            "🎉", "🕊️", "🎯", "💡", "🌸", "⚡", "✨", "🌊",
            "🌈", "🦋", "🌻", "🎨", "🚀", "💪", "🍀", "🌙"
        )
        Dialog(onDismissRequest = { showEmojiDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardBgColor,
                border = BorderStroke(1.dp, cardBorderColor),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pick an Emoji",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleTextColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(emojis) { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (darkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
                                    .clickable {
                                        editText?.let { et ->
                                            val start = et.selectionStart.coerceAtLeast(0)
                                            et.text?.insert(start, emoji)
                                        }
                                        viewModel.setStickerUsed(true)
                                        showEmojiDialog = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 22.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { showEmojiDialog = false }) {
                        Text("Close", color = subtextColor)
                    }
                }
            }
        }
    }

    // Custom calendar dialog
    if (showCalendarDialog) {
        val parsedDate = try {
            LocalDate.parse(selectedDate)
        } catch (e: Exception) {
            LocalDate.now()
        }
        CustomCalendarDialog(
            initialDate = parsedDate,
            onDateSelected = { date ->
                viewModel.setDate(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
            },
            onDismiss = { showCalendarDialog = false }
        )
    }
}

// Custom calendar dialog definition
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCalendarDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(initialDate.withDayOfMonth(1)) }
    var tempSelectedDate by remember { mutableStateOf(initialDate) }
    val today = LocalDate.now()
    val darkTheme = com.example.ui.theme.LocalIsDarkTheme.current
    val cardBgColor = if (darkTheme) Color(0xFF1E293B) else Color.White
    val cardBorderColor = if (darkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
    val titleTextColor = if (darkTheme) Color.White else Color(0xFF1E293B)
    val subtextColor = if (darkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = cardBgColor,
            border = BorderStroke(1.dp, cardBorderColor),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Entry Date",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleTextColor,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Header Row (Month Year Selection)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (darkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(TablerIcons.ChevronLeft, "Prev Month", tint = titleTextColor)
                    }
                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = titleTextColor
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(TablerIcons.ChevronRight, "Next Month", tint = titleTextColor)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Days of week row
                val daysOfWeek = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = subtextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Days Grid
                val firstDayOfWeek = currentMonth.dayOfWeek.value % 7  // 0 is Sunday, 1 is Monday...
                val daysInMonth = currentMonth.lengthOfMonth()
                val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (week in 0 until (totalCells / 7)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            for (day in 0 until 7) {
                                val cellIndex = week * 7 + day
                                val dayNumber = cellIndex - firstDayOfWeek + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val thisDate = currentMonth.withDayOfMonth(dayNumber)
                                    val isSelectable = !thisDate.isAfter(today)
                                    val isPicked = thisDate == tempSelectedDate
                                    val isTodayDate = thisDate == today

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(CircleShape)
                                            .background(
                                                if (isPicked) MaterialTheme.colorScheme.primary 
                                                else if (isTodayDate) MaterialTheme.colorScheme.primaryContainer
                                                else Color.Transparent
                                            )
                                            .border(
                                                width = if (isTodayDate && !isPicked) 1.dp else 0.dp,
                                                color = if (isTodayDate && !isPicked) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable(enabled = isSelectable) {
                                                tempSelectedDate = thisDate
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            fontSize = 14.sp,
                                            fontWeight = if (isPicked || isTodayDate) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isPicked) MaterialTheme.colorScheme.onPrimary
                                            else if (!isSelectable) subtextColor.copy(alpha = 0.4f)
                                            else if (isTodayDate) MaterialTheme.colorScheme.primary
                                            else titleTextColor
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            tempSelectedDate = today
                            currentMonth = today.withDayOfMonth(1)
                        },
                        shape = RoundedCornerShape(50.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("Today", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            onDateSelected(tempSelectedDate)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1.5f).height(44.dp)
                    ) {
                        Text("Confirm Date", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// Data holder for mood card
private data class MoodItemData(
    val label: String,
    val key: String,
    val icon: ImageVector,
    val circleBg: Color,
    val iconColor: Color
)

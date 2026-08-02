package com.example.ui.screens.diary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.DiaryEntry
import com.example.di.AppContainer
import com.example.ui.components.*
import com.example.ui.theme.Spacing
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DiaryScreen(
    appContainer: AppContainer,
    onNavigateToNewEntry: () -> Unit,
    onNavigateToEntryDetail: (String) -> Unit
) {
    val viewModel: DiaryViewModel = viewModel(
        factory = DiaryViewModel.Factory(appContainer.diaryRepository)
    )
    val entries by viewModel.filteredEntries.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedMood by viewModel.selectedMood.collectAsState()

    var showHistory by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = showHistory,
        transitionSpec = {
            if (targetState) {
                slideInHorizontally(
                    animationSpec = tween(320, easing = FastOutSlowInEasing),
                    initialOffsetX = { it / 6 }
                ) + fadeIn(
                    animationSpec = tween(320)
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(320, easing = FastOutSlowInEasing),
                    targetOffsetX = { -it / 6 }
                ) + fadeOut(
                    animationSpec = tween(320)
                )
            } else {
                slideInHorizontally(
                    animationSpec = tween(320, easing = FastOutSlowInEasing),
                    initialOffsetX = { -it / 6 }
                ) + fadeIn(
                    animationSpec = tween(320)
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(320, easing = FastOutSlowInEasing),
                    targetOffsetX = { it / 6 }
                ) + fadeOut(
                    animationSpec = tween(320)
                )
            }
        },
        label = "journalingArchiveTransition"
    ) { isHistory ->
        if (isHistory) {
            ListState(
                entries = entries,
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                selectedMood = selectedMood,
                onMoodSelected = viewModel::updateSelectedMood,
                onNavigateToEntryDetail = onNavigateToEntryDetail,
                onBackToEditor = { showHistory = false }
            )
        } else {
            NewEntryScreen(
                appContainer = appContainer,
                onBack = onNavigateToNewEntry,
                onEntrySaved = onNavigateToEntryDetail,
                showHistoryToggle = true,
                onHistoryClick = { showHistory = true }
            )
        }
    }
}

@Composable
fun EmptyState(onNewEntryClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(TablerIcons.Pencil, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(modifier = Modifier.height(Spacing.large))
        Text("Your space is empty", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text("Start writing to gently capture your feelings.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(Spacing.large))
        SoulButton(
            text = "NEW ENTRY",
            icon = TablerIcons.Plus,
            onClick = onNewEntryClick,
            modifier = Modifier.fillMaxWidth(0.7f)
        )
    }
}

@Composable
fun ListState(
    entries: List<DiaryEntry>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedMood: String?,
    onMoodSelected: (String) -> Unit,
    onNavigateToEntryDetail: (String) -> Unit,
    onBackToEditor: () -> Unit
) {
    val darkTheme = com.example.ui.theme.LocalIsDarkTheme.current
    val bgColor = if (darkTheme) Color(0xFF0F172A) else Color(0xFFFAFAFA)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.cardPaddingLarge, vertical = Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToEditor,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(
                    imageVector = TablerIcons.ArrowLeft, 
                    contentDescription = "Back to Editor",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Journal History",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        SoulSearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            modifier = Modifier.padding(horizontal = Spacing.cardPaddingLarge, vertical = Spacing.small),
            placeholder = "Search entries..."
        )

        val moodsChips = listOf("All Moods", "Happy", "Loved", "Neutral", "Anxious", "Sad", "Angry", "Numb")
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = Spacing.cardPaddingLarge, vertical = Spacing.small),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            moodsChips.forEach { mood ->
                val isSelected = selectedMood == mood || (selectedMood == null && mood == "All Moods")
                SoulChip(
                    text = mood,
                    selected = isSelected,
                    onClick = { onMoodSelected(mood) }
                )
            }
        }
        
        CalendarHeatmap(entries, modifier = Modifier.padding(horizontal = Spacing.cardPaddingLarge))

        if (entries.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                EmptyState(onNewEntryClick = onBackToEditor)
            }
        } else {
            val groupedEntries = entries.groupBy { it.date }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = Spacing.cardPaddingLarge, vertical = Spacing.small),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                groupedEntries.forEach { (date, entriesForDate) ->
                    item {
                        Text(
                            text = date, 
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(vertical = Spacing.xs)
                        )
                    }
                    items(entriesForDate, key = { it.id }) { entry ->
                        DiaryEntryCard(entry, onClick = { onNavigateToEntryDetail(entry.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarHeatmap(entries: List<DiaryEntry>, modifier: Modifier = Modifier) {
    val today = LocalDate.now()
    val lastMonth = today.minusMonths(1)
    
    val days = (0..29).map { lastMonth.plusDays(it.toLong()) }
    val entryDates = entries.map { it.date }.toSet()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = Spacing.small),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        days.forEach { date ->
            val hasEntry = entryDates.contains(date.toString())
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (hasEntry) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) 
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }
    }
}

@Composable
fun DiaryEntryCard(entry: DiaryEntry, onClick: () -> Unit) {
    val moodConfig = com.example.ui.screens.home.getMoodConfig(entry.mood)
    val hasSparkle = entry.aiSummary != null || entry.aiPattern != null || entry.aiNextStep != null
    
    SoulCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (hasSparkle) {
                Icon(
                    TablerIcons.Star, 
                    contentDescription = "AI Analysis Available", 
                    tint = MaterialTheme.colorScheme.primary, 
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Spacing.cardPadding)
                        .size(16.dp)
                )
            }

            Column(modifier = Modifier.padding(Spacing.cardPadding)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(moodConfig.tintColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = moodConfig.icon,
                            contentDescription = moodConfig.label,
                            tint = moodConfig.tintColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(Spacing.medium))
                    Column(modifier = Modifier.weight(1f)) {
                        val dateStr = try {
                            LocalDate.parse(entry.date).format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US))
                        } catch (e: Exception) { entry.date }
                        Text(
                            text = dateStr.uppercase(), 
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp), 
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(Spacing.medium))
                if (!entry.title.isNullOrEmpty()) {
                    Text(
                        text = entry.title!!, 
                        style = MaterialTheme.typography.titleMedium, 
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                }
                
                val contentText = if (!entry.contentPlain.isNullOrBlank()) entry.contentPlain!! else entry.content
                val plainText = stripHtml(contentText)
                Text(
                    text = plainText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = Spacing.cardPadding)
                )
                
                val hasImage = !entry.imageUris.isNullOrEmpty()
                val hasVoice = entry.voiceNotePath != null
                val hasVideo = entry.videoPath != null
                if (hasImage || hasVoice || hasVideo) {
                    Spacer(modifier = Modifier.height(Spacing.medium))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                        if (hasImage) {
                            Icon(TablerIcons.Photo, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (hasVoice) {
                            Icon(TablerIcons.Microphone, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (hasVideo) {
                            Icon(TablerIcons.Video, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

private fun stripHtml(html: String): String {
    if (html.isBlank()) return ""
    val decoded = try {
        androidx.core.text.HtmlCompat.fromHtml(html, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    } catch (_: Exception) {
        html
    }
    return decoded
        .replace("&#8226;", "• ")
        .replace("&#8226", "• ")
        .replace("&bull;", "• ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace(Regex("<[^>]*>"), "")
        .replace(Regex("\\s+"), " ")
        .trim()
}

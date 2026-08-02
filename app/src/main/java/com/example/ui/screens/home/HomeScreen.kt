package com.example.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.DiaryEntry
import com.example.di.AppContainer
import com.example.ui.components.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import com.example.ui.theme.LocalIsDarkTheme

@Composable
fun HomeScreen(
    appContainer: AppContainer,
    onNavigateToNewEntry: () -> Unit,
    onNavigateToDiary: () -> Unit,
    onNavigateToMoods: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEntryDetail: (String) -> Unit
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(
            appContainer.diaryRepository,
            appContainer.moodRepository,
            appContainer.achievementRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val darkTheme = LocalIsDarkTheme.current
    val bgColor = if (darkTheme) Color(0xFF0F172A) else Color(0xFFFAFAFA)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> LoadingState()
            is HomeUiState.Error -> ErrorState(state.message, onRetry = { viewModel.retry() })
            is HomeUiState.Success -> SuccessState(
                data = state.data,
                onNavigateToNewEntry = onNavigateToNewEntry,
                onNavigateToDiary = onNavigateToDiary,
                onNavigateToMoods = onNavigateToMoods,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToEntryDetail = onNavigateToEntryDetail
            )
        }
    }
}

@Composable
fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        ShimmerCard(height = 32.dp, modifier = Modifier.fillMaxWidth(0.5f))
        Spacer(modifier = Modifier.height(12.dp))
        ShimmerCard(height = 24.dp, modifier = Modifier.fillMaxWidth(0.7f))
        Spacer(modifier = Modifier.height(24.dp))
        ShimmerCard(height = 140.dp)
        Spacer(modifier = Modifier.height(20.dp))
        ShimmerCard(height = 200.dp)
        Spacer(modifier = Modifier.height(20.dp))
        ShimmerCard(height = 120.dp)
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    val darkTheme = LocalIsDarkTheme.current
    val titleColor = if (darkTheme) Color.White else Color(0xFF1E1E1E)
    val subtextColor = if (darkTheme) Color(0xFF94A3B8) else Color(0xFF71717A)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = TablerIcons.AlertCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleLarge,
            color = titleColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = subtextColor
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
        ) {
            Text("Try Again", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SuccessState(
    data: HomeData,
    onNavigateToNewEntry: () -> Unit,
    onNavigateToDiary: () -> Unit,
    onNavigateToMoods: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEntryDetail: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val darkTheme = LocalIsDarkTheme.current
    val cardBgColor = if (darkTheme) Color(0xFF1E293B) else Color.White
    val cardBorderColor = if (darkTheme) Color(0xFF334155) else Color(0xFFF3F4F6)
    val dividerColor = if (darkTheme) Color(0xFF334155) else Color(0xFFF3F4F6)
    val subtextColor = if (darkTheme) Color(0xFF94A3B8) else Color(0xFF71717A)
    val titleTextColor = if (darkTheme) Color.White else Color(0xFF1E1E1E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp, top = 24.dp)
    ) {
        var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
        
        LaunchedEffect(Unit) {
            while(true) {
                kotlinx.coroutines.delay(60000) // Update every minute
                currentTime = LocalDateTime.now()
            }
        }
        
        val todayStrFormatted = currentTime.toLocalDate().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US)).uppercase()
        val hour = currentTime.hour
        val greetingText = when (hour) {
            in 0..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }

        Text(
            text = todayStrFormatted,
            color = subtextColor,
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$greetingText.",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = titleTextColor),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You're on a ${data.streakCount}-day streak. Keep going gently.",
            color = subtextColor,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MoodStatCard(
                modifier = Modifier.weight(1f).testTag("home_mood_logged_card"),
                icon = TablerIcons.Heart,
                iconTint = Color(0xFFEC4899),
                iconBg = if (darkTheme) Color(0xFF331826) else Color(0xFFFCE7F3),
                label = "Logged",
                value = "${data.totalLoggedCount}",
                cardBgColor = cardBgColor,
                cardBorderColor = cardBorderColor,
                titleTextColor = titleTextColor,
                subtextColor = subtextColor,
                onClick = onNavigateToMoods
            )
            MoodStatCard(
                modifier = Modifier.weight(1f).testTag("home_mood_14d_card"),
                icon = TablerIcons.Calendar,
                iconTint = Color(0xFF8B5CF6),
                iconBg = if (darkTheme) Color(0xFF2E1F49) else Color(0xFFEDE9FE),
                label = "Last 14d",
                value = "${data.last14DaysCount}",
                cardBgColor = cardBgColor,
                cardBorderColor = cardBorderColor,
                titleTextColor = titleTextColor,
                subtextColor = subtextColor,
                onClick = onNavigateToMoods
            )
            val mostFeltConfig = getExactMoodConfig(data.mostFeltMood)
            val mostFeltIcon = if (data.mostFeltMood == "—") TablerIcons.MoodSmile else mostFeltConfig.icon
            val mostFeltTint = if (data.mostFeltMood == "—") Color(0xFFF59E0B) else mostFeltConfig.iconTint
            val mostFeltBg = if (darkTheme) Color(0xFF332A15) else Color(0xFFFEF3C7)
            
            MoodStatCard(
                modifier = Modifier.weight(1f).testTag("home_mood_most_card"),
                icon = mostFeltIcon,
                iconTint = mostFeltTint,
                iconBg = mostFeltBg,
                label = "Most felt",
                value = data.mostFeltMood,
                cardBgColor = cardBgColor,
                cardBorderColor = cardBorderColor,
                titleTextColor = titleTextColor,
                subtextColor = subtextColor,
                onClick = onNavigateToMoods
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Today's reflection",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
            color = titleTextColor,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (data.hasEntryToday) "You already wrote today — thank you." else "Take a moment to reflect.",
            color = subtextColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickable(onClick = onNavigateToNewEntry),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = BorderStroke(1.dp, cardBorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(TablerIcons.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (data.hasEntryToday) "Add another note" else "Write today's note",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                        color = titleTextColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Pick a mood, write a few lines, optionally let AI reflect back.",
                        color = subtextColor,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onNavigateToNewEntry,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, 
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(TablerIcons.Plus, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New", fontSize = 14.sp)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        // Section 1: Recent Entries
        HomeSectionHeader(
            title = "Recent entries",
            actionText = "All >",
            onActionClick = onNavigateToMoods
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (data.recentEntries.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No entries yet.", color = subtextColor, fontSize = 15.sp)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                data.recentEntries.take(3).forEach { entry ->
                    RecentEntryCard(entry = entry, onClick = { onNavigateToEntryDetail(entry.id) })
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Section 2: Mood Trends
        HomeSectionHeader(
            title = "Mood trends",
            subtitle = "The last 14 days at a glance.",
            actionText = "History >",
            onActionClick = onNavigateToMoods
        )

        Spacer(modifier = Modifier.height(12.dp))

        MoodBarChartExact(data = data.moodChartData, onBarClick = onNavigateToMoods)

        Spacer(modifier = Modifier.height(28.dp))

        // Section 3: Achievements
        val totalAchievements = data.achievements.size.coerceAtLeast(12)
        val unlockedCount = data.achievements.count { it.unlockedAt != null }
        HomeSectionHeader(
            title = "Achievements",
            subtitle = "$unlockedCount of $totalAchievements unlocked",
            actionText = "All >",
            onActionClick = onNavigateToSettings
        )

        Spacer(modifier = Modifier.height(12.dp))

        AchievementsCarousel(data = data)

        Spacer(modifier = Modifier.height(28.dp))

        // Section 4: Quick Actions
        HomeSectionHeader(
            title = "Quick actions"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = BorderStroke(1.dp, cardBorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column {
                QuickActionRow(
                    icon = TablerIcons.Plus,
                    title = "New entry",
                    subtitle = "Capture a moment",
                    onClick = onNavigateToNewEntry
                )
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
                QuickActionRow(
                    icon = TablerIcons.TrendingUp,
                    title = "Mood history",
                    subtitle = "See patterns over time",
                    onClick = onNavigateToMoods
                )
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
                QuickActionRow(
                    icon = TablerIcons.Settings,
                    title = "Settings",
                    subtitle = "Themes, period tracker, account",
                    onClick = onNavigateToSettings
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun HomeSectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val darkTheme = LocalIsDarkTheme.current
    val titleTextColor = if (darkTheme) Color.White else Color(0xFF1E1E1E)
    val subtextColor = if (darkTheme) Color(0xFF94A3B8) else Color(0xFF71717A)
    val actionColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = titleTextColor
            )
            if (!subtitle.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = subtextColor
                )
            }
        }
        if (!actionText.isNullOrEmpty() && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = actionColor,
                modifier = Modifier
                    .clickable(onClick = onActionClick)
                    .padding(start = 8.dp, top = 2.dp)
            )
        }
    }
}

@Composable
fun RecentEntryCard(entry: DiaryEntry, onClick: () -> Unit) {
    val darkTheme = LocalIsDarkTheme.current
    val cardBgColor = if (darkTheme) Color(0xFF1E293B) else Color.White
    val cardBorderColor = if (darkTheme) Color(0xFF334155) else Color(0xFFF3F4F6)
    val titleTextColor = if (darkTheme) Color.White else Color(0xFF1E1E1E)
    val subtextColor = if (darkTheme) Color(0xFF94A3B8) else Color(0xFF71717A)
    val bodyTextColor = if (darkTheme) Color(0xFFCBD5E1) else Color(0xFF52525B)
    val starColor = MaterialTheme.colorScheme.primary

    val moodConfig = getExactMoodConfig(entry.mood)
    val formattedDate = try {
        LocalDate.parse(entry.date).format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US))
    } catch (_: Exception) { entry.date }.uppercase()

    val hasImage = !entry.imageUris.isNullOrEmpty()
    val hasAudio = !entry.voiceNotePath.isNullOrEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Sparkle icon in top right
            Icon(
                imageVector = TablerIcons.Star,
                contentDescription = null,
                tint = starColor,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
            )

            Row(verticalAlignment = Alignment.Top) {
                // Circular Mood Badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(if (darkTheme) moodConfig.iconTint.copy(alpha = 0.2f) else moodConfig.circleBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = moodConfig.icon,
                        contentDescription = null,
                        tint = moodConfig.iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Date & Mood Header
                    Text(
                        text = "$formattedDate · ${moodConfig.label}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = subtextColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Entry Title
                    Text(
                        text = if (!entry.title.isNullOrEmpty()) entry.title!! else "Untitled",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = titleTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Content Snippet
                    val contentText = if (!entry.contentPlain.isNullOrBlank()) entry.contentPlain!! else entry.content
                    val cleanedContent = cleanDiaryContent(contentText)
                    if (cleanedContent.isNotEmpty()) {
                        Text(
                            text = cleanedContent,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = bodyTextColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Media Attachment tags (e.g. Image, Voice)
                    if (hasImage || hasAudio) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (hasImage) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = TablerIcons.Photo,
                                        contentDescription = null,
                                        tint = subtextColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Image",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = subtextColor
                                    )
                                }
                            }
                            if (hasAudio) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = TablerIcons.Microphone,
                                        contentDescription = null,
                                        tint = subtextColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Voice",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = subtextColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MoodBarChartExact(data: Map<String, Int>, onBarClick: () -> Unit) {
    val darkTheme = LocalIsDarkTheme.current
    val cardBgColor = if (darkTheme) Color(0xFF1E293B) else Color.White
    val cardBorderColor = if (darkTheme) Color(0xFF334155) else Color(0xFFF3F4F6)
    val titleTextColor = if (darkTheme) Color.White else Color(0xFF1E1E1E)
    val subtextColor = if (darkTheme) Color(0xFF94A3B8) else Color(0xFF71717A)
    val axisLabelColor = if (darkTheme) Color(0xFF64748B) else Color(0xFF9CA3AF)
    val emptyDashColor = if (darkTheme) Color(0xFF334155) else Color(0xFFE5E7EB)

    val moodsList = listOf(
        ExactMoodItem("Happy", "HAPPY", Color(0xFFFBBF24), "☀️", TablerIcons.Sun),
        ExactMoodItem("Loved", "LOVED", Color(0xFFF472B6), "💖", TablerIcons.Heart),
        ExactMoodItem("Neutral", "NEUTRAL", Color(0xFF94A3B8), "🙂", TablerIcons.MoodSmile),
        ExactMoodItem("Anxious", "ANXIOUS", Color(0xFFA78BFA), "🌀", TablerIcons.Wind),
        ExactMoodItem("Sad", "SAD", Color(0xFF60A5FA), "💧", TablerIcons.Droplet),
        ExactMoodItem("Angry", "ANGRY", Color(0xFFF97316), "🔥", TablerIcons.Flame),
        ExactMoodItem("Numb", "NUMB", Color(0xFF64748B), "☁️", TablerIcons.Snowflake)
    )

    val maxCount = (data.values.maxOrNull() ?: 0).coerceAtLeast(5)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable(onClick = onBarClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Y-Axis scale labels column
            Column(
                modifier = Modifier
                    .height(180.dp)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(text = "5", fontSize = 11.sp, color = axisLabelColor)
                Text(text = "2", fontSize = 11.sp, color = axisLabelColor)
                Text(text = "0", fontSize = 11.sp, color = axisLabelColor)
            }

            // Mood Bars Row
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(210.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                moodsList.forEach { mood ->
                    val rawCount = data[mood.key] ?: data[mood.label] ?: 0
                    val heightRatio = (rawCount.toFloat() / maxCount).coerceIn(0f, 1f)
                    val animatedRatio by animateFloatAsState(targetValue = heightRatio, animationSpec = tween(700))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Count label above bar
                        if (rawCount > 0) {
                            Text(
                                text = rawCount.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleTextColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        } else {
                            Spacer(modifier = Modifier.height(18.dp))
                        }

                        // Vertical Bar container
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(120.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (rawCount > 0) {
                                val barHeight = (120.dp * animatedRatio).coerceAtLeast(10.dp)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(barHeight)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(mood.barColor)
                                )
                            } else {
                                // Baseline dash line for 0 count
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(emptyDashColor)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Mood Label
                        Text(
                            text = mood.label,
                            fontSize = 11.sp,
                            color = subtextColor,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Emoji / Icon
                        Text(
                            text = mood.emoji,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

data class ExactMoodItem(
    val label: String,
    val key: String,
    val barColor: Color,
    val emoji: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun AchievementsCarousel(data: HomeData) {
    val unlockedSet = data.achievements.filter { it.unlockedAt != null }.map { it.id }.toSet()

    val achievementItems = listOf(
        AchievementItemData("First Entry", "Wrote your first reflection", TablerIcons.Pencil, unlockedSet.contains("first_entry")),
        AchievementItemData("3 Day Streak", "Three days in a row", TablerIcons.Sun, unlockedSet.contains("streak_3")),
        AchievementItemData("7 Day Streak", "A full week of journaling", TablerIcons.CalendarEvent, unlockedSet.contains("streak_7")),
        AchievementItemData("14 Day Streak", "Two full weeks showing up", TablerIcons.CalendarEvent, unlockedSet.contains("streak_14")),
        AchievementItemData("Emotional Awareness", "Logged 5 distinct moods", TablerIcons.MoodSmile, unlockedSet.contains("emotional_awareness")),
        AchievementItemData("Visual Memory", "Added an image to an entry", TablerIcons.Photo, unlockedSet.contains("first_image")),
        AchievementItemData("Voice Note", "Recorded a voice reflection", TablerIcons.Microphone, unlockedSet.contains("first_voice")),
        AchievementItemData("Reflection Starter", "Ran your first AI analysis", TablerIcons.Bulb, unlockedSet.contains("reflection_starter")),
        AchievementItemData("Deep Reflection", "Completed 5 AI analyses", TablerIcons.Bulb, unlockedSet.contains("deep_reflection")),
        AchievementItemData("Pattern Breaker", "Discovered a mood pattern", TablerIcons.Activity, unlockedSet.contains("pattern_breaker")),
        AchievementItemData("Theme Explorer", "Switched light/dark themes", TablerIcons.Palette, unlockedSet.contains("theme_explorer")),
        AchievementItemData("Expressive Soul", "Used a sticker in an entry", TablerIcons.Sticker, unlockedSet.contains("sticker_user"))
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(achievementItems) { item ->
            AchievementCardItem(item)
        }
    }
}

data class AchievementItemData(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isUnlocked: Boolean
)

@Composable
fun AchievementCardItem(item: AchievementItemData) {
    val darkTheme = LocalIsDarkTheme.current
    val cardBgColor = if (darkTheme) Color(0xFF1E293B) else Color.White
    val cardBorderColor = if (darkTheme) Color(0xFF334155) else Color(0xFFF3F4F6)
    val titleTextColor = if (darkTheme) Color.White else Color(0xFF1E1E1E)
    val subtextColor = if (darkTheme) Color(0xFF94A3B8) else Color(0xFF71717A)
    val iconBgColor = if (item.isUnlocked) MaterialTheme.colorScheme.primaryContainer else (if (darkTheme) Color(0xFF334155) else Color(0xFFF4F4F5))
    val iconTintColor = if (item.isUnlocked) MaterialTheme.colorScheme.primary else (if (darkTheme) Color(0xFF64748B) else Color(0xFFA1A1AA))

    Card(
        modifier = Modifier
            .width(135.dp)
            .height(165.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        iconBgColor,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isUnlocked) item.icon else TablerIcons.Lock,
                    contentDescription = null,
                    tint = iconTintColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = titleTextColor,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = subtextColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun QuickActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val darkTheme = LocalIsDarkTheme.current
    val titleTextColor = if (darkTheme) Color.White else Color(0xFF1E1E1E)
    val subtextColor = if (darkTheme) Color(0xFF94A3B8) else Color(0xFF71717A)
    val iconBgColor = MaterialTheme.colorScheme.primaryContainer
    val iconTintColor = MaterialTheme.colorScheme.primary
    val chevronColor = if (darkTheme) Color(0xFF64748B) else Color(0xFF9CA3AF)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconBgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                ),
                color = titleTextColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = subtextColor
            )
        }

        Icon(
            imageVector = TablerIcons.ChevronRight,
            contentDescription = null,
            tint = chevronColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

fun cleanDiaryContent(content: String): String {
    if (content.isBlank()) return ""
    val decoded = try {
        androidx.core.text.HtmlCompat.fromHtml(content, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    } catch (_: Exception) {
        content
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
        .replace(Regex("#+\\s"), "")
        .replace(Regex("\\s+"), " ")
        .trim()
}

data class ExactMoodConfig(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val circleBg: Color,
    val iconTint: Color
)

fun getExactMoodConfig(moodName: String): ExactMoodConfig {
    return when (moodName.uppercase()) {
        "HAPPY" -> ExactMoodConfig("Happy", TablerIcons.Sun, Color(0xFFFEF3C7), Color(0xFFF59E0B))
        "LOVED" -> ExactMoodConfig("Loved", TablerIcons.Heart, Color(0xFFFCE7F3), Color(0xFFEC4899))
        "NEUTRAL" -> ExactMoodConfig("Neutral", TablerIcons.MoodSmile, Color(0xFFF3F4F6), Color(0xFF9CA3AF))
        "ANXIOUS" -> ExactMoodConfig("Anxious", TablerIcons.Wind, Color(0xFFF3E8FF), Color(0xFF8B5CF6))
        "SAD" -> ExactMoodConfig("Sad", TablerIcons.Droplet, Color(0xFFE0F2FE), Color(0xFF3B82F6))
        "ANGRY" -> ExactMoodConfig("Angry", TablerIcons.Flame, Color(0xFFFFEDD5), Color(0xFFEA580C))
        "NUMB" -> ExactMoodConfig("Numb", TablerIcons.Snowflake, Color(0xFFF1F5F9), Color(0xFF64748B))
        else -> ExactMoodConfig(moodName, TablerIcons.MoodSmile, Color(0xFFF3F4F6), Color(0xFF9CA3AF))
    }
}

// Backwards compatibility for other screens
data class MoodConfig(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val tintColor: Color)

fun getMoodConfig(moodName: String): MoodConfig {
    val exact = getExactMoodConfig(moodName)
    return MoodConfig(exact.label, exact.icon, exact.iconTint)
}

@Composable
fun MoodStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    cardBgColor: Color,
    cardBorderColor: Color,
    titleTextColor: Color,
    subtextColor: Color,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        shape = RoundedCornerShape(20.dp),
        color = cardBgColor,
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = subtextColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

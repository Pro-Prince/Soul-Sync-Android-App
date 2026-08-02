package com.example.ui.screens.moods

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.DiaryEntry
import com.example.di.AppContainer
import com.example.ui.components.*
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.Spacing
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MoodsScreen(
    appContainer: AppContainer,
    onNavigateToEntryDetail: (String) -> Unit,
    onNavigateToHome: () -> Unit
) {
    val viewModel: MoodsViewModel = viewModel(
        factory = MoodsViewModel.Factory(appContainer.diaryRepository, appContainer.moodRepository)
    )

    val totalLoggedCount by viewModel.totalLoggedCount.collectAsState()
    val last14DaysCount by viewModel.last14DaysCount.collectAsState()
    val mostFeltMood by viewModel.mostFeltMood.collectAsState()
    val moodChartData by viewModel.moodChartData.collectAsState()
    val moodBreakdown by viewModel.moodBreakdown.collectAsState()
    val allDaysEntries by viewModel.allDaysEntries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedMoodFilter by viewModel.selectedMoodFilter.collectAsState()
    val moodSummary by viewModel.moodSummary.collectAsState()

    val darkTheme = LocalIsDarkTheme.current
    val bgColor = if (darkTheme) Color(0xFF0F172A) else Color(0xFFFAFAFA)
    val cardBgColor = if (darkTheme) Color(0xFF1E293B) else Color.White
    val cardBorderColor = if (darkTheme) Color(0xFF334155) else Color(0xFFF1F5F9)
    val titleTextColor = if (darkTheme) Color.White else Color(0xFF1E1E1E)
    val subtextColor = if (darkTheme) Color(0xFF94A3B8) else Color(0xFF71717A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Top Bar Header with Chevron Back to Home
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigateToHome() }
                    .padding(vertical = 4.dp, horizontal = 4.dp),
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
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().imePadding(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header Section
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Mood history",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleTextColor,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Every feeling you've named, kept private and gentle.",
                        fontSize = 15.sp,
                        color = subtextColor
                    )
                }

                // AI Reflection Banner
                if (!moodSummary.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = if (darkTheme) Color(0xFF312E81) else Color(0xFFF5F3FF),
                        border = BorderStroke(1.dp, if (darkTheme) Color(0xFF4338CA) else Color(0xFFDDD6FE))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = TablerIcons.Bulb,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SOULSYNC REFLECTION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = moodSummary?.replace("**", "")?.replace("*", "")?.trim() ?: "",
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = if (darkTheme) Color(0xFFE0E7FF) else Color(0xFF4C1D95)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Stat Cards Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MoodStatCard(
                        modifier = Modifier.weight(1f).testTag("mood_logged_card"),
                        icon = TablerIcons.Heart,
                        iconTint = Color(0xFFEC4899),
                        iconBg = if (darkTheme) Color(0xFF331826) else Color(0xFFFCE7F3),
                        label = "Logged",
                        value = "$totalLoggedCount",
                        cardBgColor = cardBgColor,
                        cardBorderColor = cardBorderColor,
                        titleTextColor = titleTextColor,
                        subtextColor = subtextColor,
                        onClick = { viewModel.selectMoodFilter("All") }
                    )
                    MoodStatCard(
                        modifier = Modifier.weight(1f).testTag("mood_14d_card"),
                        icon = TablerIcons.Calendar,
                        iconTint = Color(0xFF8B5CF6),
                        iconBg = if (darkTheme) Color(0xFF2E1F49) else Color(0xFFEDE9FE),
                        label = "Last 14d",
                        value = "$last14DaysCount",
                        cardBgColor = cardBgColor,
                        cardBorderColor = cardBorderColor,
                        titleTextColor = titleTextColor,
                        subtextColor = subtextColor,
                        onClick = { viewModel.selectMoodFilter("All") }
                    )
                    
                    val (mostFeltIcon, mostFeltTint) = getMoodIconAndTint(mostFeltMood)
                    val displayIcon = mostFeltIcon ?: TablerIcons.MoodSmile
                    val displayTint = if (mostFeltIcon != null) mostFeltTint else Color(0xFFF59E0B)
                    val displayBg = if (darkTheme) Color(0xFF332A15) else Color(0xFFFEF3C7)
                    val displayMood = if (mostFeltMood == "—") "—" else {
                        mostFeltMood.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
                    }
                    MoodStatCard(
                        modifier = Modifier.weight(1f).testTag("mood_most_felt_card"),
                        icon = displayIcon,
                        iconTint = displayTint,
                        iconBg = displayBg,
                        label = "Most Felt",
                        value = displayMood,
                        cardBgColor = cardBgColor,
                        cardBorderColor = cardBorderColor,
                        titleTextColor = titleTextColor,
                        subtextColor = subtextColor,
                        onClick = {
                            if (mostFeltMood != "—") {
                                viewModel.selectMoodFilter(mostFeltMood.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() })
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Mood Filter Chips Carousel
            item {
                Column(modifier = Modifier.padding(bottom = 20.dp)) {
                    Text(
                        text = "Filter by mood",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleTextColor,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filterOptions = listOf("All", "Happy", "Loved", "Neutral", "Anxious", "Sad", "Angry", "Numb")
                        filterOptions.forEach { mood ->
                            val isSelected = selectedMoodFilter == mood
                            val (icon, tint) = getMoodIconAndTint(mood)
                            
                            Surface(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        viewModel.selectMoodFilter(mood)
                                    },
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else cardBgColor,
                                border = if (isSelected) null else BorderStroke(1.dp, cardBorderColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (icon != null) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = mood,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else tint,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = mood,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else titleTextColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Trends Section
            item {
                SoulSectionHeader(
                    title = "Trends",
                    subtitle = "The last 14 days at a glance."
                )
                TrendsBarChart(
                    data = moodChartData,
                    cardBgColor = cardBgColor,
                    cardBorderColor = cardBorderColor,
                    titleTextColor = titleTextColor,
                    subtextColor = subtextColor
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Mood Breakdown Section
            item {
                SoulSectionHeader(title = "Mood breakdown")

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = cardBgColor,
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        moodBreakdown.forEachIndexed { idx, stat ->
                            MoodBreakdownRow(
                                moodName = stat.mood,
                                count = stat.count,
                                percentage = stat.percentage,
                                titleTextColor = titleTextColor,
                                subtextColor = subtextColor,
                                cardBorderColor = cardBorderColor
                            )

                            if (idx < moodBreakdown.size - 1) {
                                HorizontalDivider(
                                    color = cardBorderColor,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // All Days Section
            item {
                SoulSectionHeader(title = "All days")

                SoulSearchBar(
                    query = searchQuery,
                    onQueryChange = {
                        viewModel.updateSearchQuery(it)
                    },
                    modifier = Modifier.padding(horizontal = 20.dp),
                    placeholder = "Search by date, mood, keyword..."
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (allDaysEntries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = TablerIcons.MoodSad,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = subtextColor.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No entries match filters",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = subtextColor
                            )
                        }
                    }
                }
            } else {
                items(allDaysEntries, key = { it.id }) { entry ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                        MoodEntryRow(
                            entry = entry,
                            onClick = { onNavigateToEntryDetail(entry.id) },
                            cardBgColor = cardBgColor,
                            cardBorderColor = cardBorderColor,
                            titleTextColor = titleTextColor,
                            subtextColor = subtextColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoodStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
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
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TrendsBarChart(
    data: Map<String, Int>,
    cardBgColor: Color,
    cardBorderColor: Color,
    titleTextColor: Color,
    subtextColor: Color
) {
    val moods = listOf("Happy", "Loved", "Neutral", "Anxious", "Sad", "Angry", "Numb")
    val maxCount = (data.values.maxOrNull() ?: 0).coerceAtLeast(5)
    val midCount = maxCount / 2
    val chartHeight = 220.dp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        color = cardBgColor,
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // Y-Axis Labels
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(bottom = 28.dp, end = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(maxCount.toString(), fontSize = 11.sp, color = subtextColor)
                Text(midCount.toString(), fontSize = 11.sp, color = subtextColor)
                Text("0", fontSize = 11.sp, color = subtextColor)
            }

            // Bars
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                moods.forEach { moodName ->
                    val rawCount = data[moodName.uppercase()] ?: data[moodName.lowercase()] ?: data[moodName] ?: 0
                    val heightRatio = (rawCount.toFloat() / maxCount).coerceIn(0f, 1f)
                    val animatedPercentage by animateFloatAsState(
                        targetValue = heightRatio,
                        animationSpec = tween(700, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                        label = "BarHeightAnimation"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        if (rawCount > 0) {
                            Text(
                                text = rawCount.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleTextColor,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        val barHeight = (chartHeight - 85.dp) * animatedPercentage
                        val barColor = when (moodName.lowercase()) {
                            "happy" -> Color(0xFFFBBF24) // Yellow
                            "loved" -> Color(0xFFF472B6) // Pink
                            "neutral" -> Color(0xFF94A3B8) // Slate
                            "anxious" -> Color(0xFF8B5CF6) // Violet
                            "sad" -> Color(0xFF3B82F6) // Blue
                            "angry" -> Color(0xFFEF4444) // Red
                            "numb" -> Color(0xFF64748B) // Dark Slate
                            else -> Color(0xFF94A3B8)
                        }

                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(if (rawCount > 0) barHeight.coerceAtLeast(16.dp) else 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (rawCount > 0) {
                                        barColor
                                    } else {
                                        cardBorderColor
                                    }
                                )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = moodName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = subtextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoodBreakdownRow(
    moodName: String,
    count: Int,
    percentage: Int,
    titleTextColor: Color,
    subtextColor: Color,
    cardBorderColor: Color
) {
    val (icon, tint) = getMoodIconAndTint(moodName)
    val displayLabel = moodName.lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(tint.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = displayLabel,
                                tint = tint,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = displayLabel,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = titleTextColor
                    )
                }
                Text(
                    text = "$count · $percentage%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = subtextColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        cardBorderColor,
                        RoundedCornerShape(3.dp)
                    )
            ) {
                if (percentage > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentage / 100f)
                            .height(6.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(tint.copy(alpha = 0.7f), tint)
                                ),
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun MoodEntryRow(
    entry: DiaryEntry,
    onClick: () -> Unit,
    cardBgColor: Color,
    cardBorderColor: Color,
    titleTextColor: Color,
    subtextColor: Color
) {
    val (icon, tint) = getMoodIconAndTint(entry.mood)

    val formattedDate = try {
        val d = LocalDate.parse(entry.date)
        val weekday = d.format(DateTimeFormatter.ofPattern("EEEE", Locale.US))
        val monthDayYear = d.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US))
        val moodName = entry.mood.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
        "$weekday, $monthDayYear · $moodName"
    } catch (e: Exception) {
        entry.date
    }

    val contentText = if (!entry.contentPlain.isNullOrBlank()) entry.contentPlain!! else entry.content
    val bodyPreview = stripHtml(contentText)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = cardBgColor,
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(tint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = entry.mood,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formattedDate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleTextColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                val displayPreview = if (!entry.title.isNullOrEmpty()) entry.title else bodyPreview
                Text(
                    text = displayPreview,
                    fontSize = 13.sp,
                    color = subtextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = TablerIcons.ChevronRight,
                contentDescription = null,
                tint = subtextColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

fun getMoodIconAndTint(mood: String): Pair<ImageVector?, Color> {
    return when (mood.uppercase()) {
        "HAPPY" -> Pair(TablerIcons.Sun, Color(0xFFF59E0B))
        "LOVED" -> Pair(TablerIcons.Heart, Color(0xFFEC4899))
        "NEUTRAL" -> Pair(TablerIcons.MoodSmile, Color(0xFF94A3B8))
        "ANXIOUS" -> Pair(TablerIcons.Wind, Color(0xFF8B5CF6))
        "SAD" -> Pair(TablerIcons.Droplet, Color(0xFF3B82F6))
        "ANGRY" -> Pair(TablerIcons.Flame, Color(0xFFEF4444))
        "NUMB" -> Pair(TablerIcons.Snowflake, Color(0xFF64748B))
        else -> Pair(null, Color(0xFF94A3B8))
    }
}

fun stripHtml(html: String): String {
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


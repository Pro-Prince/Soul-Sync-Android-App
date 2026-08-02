package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.di.AppContainer
import com.example.ui.theme.LocalIsDarkTheme
import android.content.Context
import android.widget.Toast
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Composable
fun SettingsScreen(
    appContainer: AppContainer,
    onNavigateToHome: () -> Unit,
    onSignOut: () -> Unit
) {
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(appContainer.settingsRepository, appContainer.achievementRepository)
    )

    val darkMode by viewModel.darkMode.collectAsState()
    val colorTheme by viewModel.colorTheme.collectAsState()
    val periodTrackerEnabled by viewModel.periodTrackerEnabled.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    var showSignOutDialog by remember { mutableStateOf(false) }

    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = LocalIsDarkTheme.current

    val bgColor = if (darkTheme) Color(0xFF0F172A) else Color(0xFFFAFAFA)
    val cardBgColor = if (darkTheme) Color(0xFF1E293B) else Color.White
    val cardBorderColor = if (darkTheme) Color(0xFF334155) else Color(0xFFF1F5F9)
    val titleTextColor = if (darkTheme) Color.White else Color(0xFF1E1E1E)
    val subtextColor = if (darkTheme) Color(0xFF94A3B8) else Color(0xFF71717A)
    val iconBgColor = MaterialTheme.colorScheme.primaryContainer
    val iconTintColor = MaterialTheme.colorScheme.primary

    val targetIds = listOf(
        "first_entry", "streak_3", "streak_7",
        "streak_14", "emotional_awareness", "pattern_breaker",
        "first_image", "first_voice", "sticker_user", "theme_explorer",
        "reflection_starter", "deep_reflection"
    )
    val localAchievements = achievements.filter { it.id in targetIds }
    val unlockedCount = localAchievements.count { it.unlockedAt != null }

    var selectedAchievementDetail by remember { mutableStateOf<AchievementDetailInfo?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 40.dp)
    ) {
        // TOP NAVIGATION BAR: ChevronLeft + Home
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

        // HEADER: Settings & Email
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Settings",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (userEmail.isNullOrBlank()) "No email" else userEmail!!,
                fontSize = 15.sp,
                color = subtextColor
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 1. APPEARANCE SECTION
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Appearance",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor
            )
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = cardBgColor,
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Dark mode row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(iconBgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = TablerIcons.Sun,
                                    contentDescription = null,
                                    tint = iconTintColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Dark mode",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = titleTextColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Soft on the eyes at night",
                                    fontSize = 13.sp,
                                    color = subtextColor
                                )
                            }
                        }
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = { viewModel.setDarkMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = if (darkTheme) Color(0xFF334155) else Color(0xFFE2E8F0),
                                checkedBorderColor = MaterialTheme.colorScheme.primary,
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = cardBorderColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Color theme row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(iconBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = TablerIcons.Palette,
                                contentDescription = null,
                                tint = iconTintColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Color theme",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleTextColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pick a palette that feels like you",
                                fontSize = 13.sp,
                                color = subtextColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Palettes Cards
                    val palettes = listOf(
                        Triple("SOUL_PINK", "Soul Pink", "Default · calm and feminine" to Color(0xFFF472B6)),
                        Triple("WARM_ROSE", "Warm Rose", "Romantic warmth" to Color(0xFFFB7185)),
                        Triple("WARM_SAND", "Warm Sand", "Quiet earth tones" to Color(0xFFC29B68)),
                        Triple("LAVENDER_CALM", "Lavender Calm", "Soothing and dreamy" to Color(0xFFC4B5FD))
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        palettes.forEach { (id, name, pair) ->
                            val (subtitle, swatchColor) = pair
                            val isSelected = colorTheme == id || (colorTheme.isEmpty() && id == "SOUL_PINK")
                            
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { viewModel.setColorTheme(id) },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else (if (darkTheme) Color(0xFF0F172A) else Color(0xFFFAFAFA)),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else cardBorderColor
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(swatchColor)
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column {
                                            Text(
                                                text = name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = titleTextColor
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = subtitle,
                                                fontSize = 13.sp,
                                                color = subtextColor
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = TablerIcons.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 2. WELLNESS SECTION
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Wellness",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor
            )
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = cardBgColor,
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(iconBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = TablerIcons.Droplet,
                                contentDescription = null,
                                tint = iconTintColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Period Tracker",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleTextColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (periodTrackerEnabled) "Private cycle tab enabled in your nav." else "Private cycle tab disabled.",
                                fontSize = 13.sp,
                                color = subtextColor
                            )
                        }
                    }
                    Switch(
                        checked = periodTrackerEnabled,
                        onCheckedChange = { viewModel.setPeriodTrackerEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = if (darkTheme) Color(0xFF334155) else Color(0xFFE2E8F0),
                            checkedBorderColor = MaterialTheme.colorScheme.primary,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 3. ACHIEVEMENTS SECTION
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Achievements",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleTextColor
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$unlockedCount of 12 unlocked",
                fontSize = 13.sp,
                color = subtextColor
            )
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = cardBgColor,
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // STARTER GROUP
                    Text(
                        text = "STARTER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = subtextColor,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val firstEntryObj = achievements.find { it.id == "first_entry" }
                        val streak3Obj = achievements.find { it.id == "streak_3" }
                        val streak7Obj = achievements.find { it.id == "streak_7" }

                        AchievementGridCard(
                            title = "First Entry",
                            desc = "Wrote your first...",
                            icon = TablerIcons.Pencil,
                            isUnlocked = firstEntryObj?.unlockedAt != null,
                            cardBorderColor = cardBorderColor,
                            titleTextColor = titleTextColor,
                            subtextColor = subtextColor,
                            onClick = {
                                selectedAchievementDetail = AchievementDetailInfo(
                                    id = "first_entry",
                                    title = "First Entry",
                                    category = "STARTER",
                                    desc = "Wrote your first journal entry",
                                    fullDesc = "Your reflection journey begins! Writing your first diary entry lays the foundation for tracking your mood and emotional wellness over time.",
                                    icon = TablerIcons.Pencil,
                                    isUnlocked = firstEntryObj?.unlockedAt != null,
                                    unlockedAt = firstEntryObj?.unlockedAt
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        AchievementGridCard(
                            title = "3 Day Streak",
                            desc = "Three days in a row",
                            icon = TablerIcons.Sun,
                            isUnlocked = streak3Obj?.unlockedAt != null,
                            cardBorderColor = cardBorderColor,
                            titleTextColor = titleTextColor,
                            subtextColor = subtextColor,
                            onClick = {
                                selectedAchievementDetail = AchievementDetailInfo(
                                    id = "streak_3",
                                    title = "3 Day Streak",
                                    category = "STARTER",
                                    desc = "Logged entries for 3 consecutive days",
                                    fullDesc = "Building momentum! Journaling 3 days in a row starts establishing a regular habit for mindful self-reflection.",
                                    icon = TablerIcons.Sun,
                                    isUnlocked = streak3Obj?.unlockedAt != null,
                                    unlockedAt = streak3Obj?.unlockedAt
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        AchievementGridCard(
                            title = "7 Day Streak",
                            desc = "A full week of...",
                            icon = TablerIcons.CalendarEvent,
                            isUnlocked = streak7Obj?.unlockedAt != null,
                            cardBorderColor = cardBorderColor,
                            titleTextColor = titleTextColor,
                            subtextColor = subtextColor,
                            onClick = {
                                selectedAchievementDetail = AchievementDetailInfo(
                                    id = "streak_7",
                                    title = "7 Day Streak",
                                    category = "STARTER",
                                    desc = "Logged entries for 7 consecutive days",
                                    fullDesc = "A full week of mindful reflection! Maintaining a 7-day journaling streak helps bring deep clarity and emotional focus.",
                                    icon = TablerIcons.CalendarEvent,
                                    isUnlocked = streak7Obj?.unlockedAt != null,
                                    unlockedAt = streak7Obj?.unlockedAt
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // GROWTH GROUP
                    Text(
                        text = "GROWTH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = subtextColor,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val streak14Obj = achievements.find { it.id == "streak_14" }
                        val emotionalObj = achievements.find { it.id == "emotional_awareness" }
                        val patternObj = achievements.find { it.id == "pattern_breaker" }

                        AchievementGridCard(
                            title = "14 Day Consistency",
                            desc = "Two full weeks of...",
                            icon = TablerIcons.CalendarEvent,
                            isUnlocked = streak14Obj?.unlockedAt != null,
                            cardBorderColor = cardBorderColor,
                            titleTextColor = titleTextColor,
                            subtextColor = subtextColor,
                            onClick = {
                                selectedAchievementDetail = AchievementDetailInfo(
                                    id = "streak_14",
                                    title = "14 Day Consistency",
                                    category = "GROWTH",
                                    desc = "Logged entries for 14 consecutive days",
                                    fullDesc = "Two full weeks of daily entries! Consistent journaling transforms brief reflections into lasting emotional resilience.",
                                    icon = TablerIcons.CalendarEvent,
                                    isUnlocked = streak14Obj?.unlockedAt != null,
                                    unlockedAt = streak14Obj?.unlockedAt
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        AchievementGridCard(
                            title = "Emotional Awareness",
                            desc = "Logged five different...",
                            icon = TablerIcons.Heart,
                            isUnlocked = emotionalObj?.unlockedAt != null,
                            cardBorderColor = cardBorderColor,
                            titleTextColor = titleTextColor,
                            subtextColor = subtextColor,
                            onClick = {
                                selectedAchievementDetail = AchievementDetailInfo(
                                    id = "emotional_awareness",
                                    title = "Emotional Awareness",
                                    category = "GROWTH",
                                    desc = "Logged 5 different mood states",
                                    fullDesc = "Expanding your emotional palette! Recognizing and logging various feelings helps develop higher emotional intelligence.",
                                    icon = TablerIcons.Heart,
                                    isUnlocked = emotionalObj?.unlockedAt != null,
                                    unlockedAt = emotionalObj?.unlockedAt
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        AchievementGridCard(
                            title = "Pattern Breaker",
                            desc = "Spotted a pattern...",
                            icon = TablerIcons.Eye,
                            isUnlocked = patternObj?.unlockedAt != null,
                            cardBorderColor = cardBorderColor,
                            titleTextColor = titleTextColor,
                            subtextColor = subtextColor,
                            onClick = {
                                selectedAchievementDetail = AchievementDetailInfo(
                                    id = "pattern_breaker",
                                    title = "Pattern Breaker",
                                    category = "GROWTH",
                                    desc = "Discovered a recurring emotional pattern with AI",
                                    fullDesc = "Gaining deep insight! Using AI to identify recurring triggers empowers you to navigate emotional patterns constructively.",
                                    icon = TablerIcons.Eye,
                                    isUnlocked = patternObj?.unlockedAt != null,
                                    unlockedAt = patternObj?.unlockedAt
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // FEATURES GROUP
                    Text(
                        text = "FEATURES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = subtextColor,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val imgObj = achievements.find { it.id == "first_image" }
                    val voiceObj = achievements.find { it.id == "first_voice" }
                    val stickerObj = achievements.find { it.id == "sticker_user" }
                    val themeObj = achievements.find { it.id == "theme_explorer" }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AchievementGridCard(
                            title = "First Image Added",
                            desc = "Captured a moment...",
                            icon = TablerIcons.Photo,
                            isUnlocked = imgObj?.unlockedAt != null,
                            cardBorderColor = cardBorderColor,
                            titleTextColor = titleTextColor,
                            subtextColor = subtextColor,
                            onClick = {
                                selectedAchievementDetail = AchievementDetailInfo(
                                    id = "first_image",
                                    title = "First Image Added",
                                    category = "FEATURES",
                                    desc = "Attached an image to a journal entry",
                                    fullDesc = "A picture is worth a thousand words! Preserving memories with photos enriches your personal diary history.",
                                    icon = TablerIcons.Photo,
                                    isUnlocked = imgObj?.unlockedAt != null,
                                    unlockedAt = imgObj?.unlockedAt
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        AchievementGridCard(
                            title = "First Voice Note",
                            desc = "Added your first voice...",
                            icon = TablerIcons.Microphone,
                            isUnlocked = voiceObj?.unlockedAt != null,
                            cardBorderColor = cardBorderColor,
                            titleTextColor = titleTextColor,
                            subtextColor = subtextColor,
                            onClick = {
                                selectedAchievementDetail = AchievementDetailInfo(
                                    id = "first_voice",
                                    title = "First Voice Note",
                                    category = "FEATURES",
                                    desc = "Recorded a voice note in a journal entry",
                                    fullDesc = "Speaking from the heart! Capturing raw voice thoughts brings authentic emotion to your journal entries.",
                                    icon = TablerIcons.Microphone,
                                    isUnlocked = voiceObj?.unlockedAt != null,
                                    unlockedAt = voiceObj?.unlockedAt
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        AchievementGridCard(
                            title = "Sticker User",
                            desc = "Decorated an entry...",
                            icon = TablerIcons.Notes,
                            isUnlocked = stickerObj?.unlockedAt != null,
                            cardBorderColor = cardBorderColor,
                            titleTextColor = titleTextColor,
                            subtextColor = subtextColor,
                            onClick = {
                                selectedAchievementDetail = AchievementDetailInfo(
                                    id = "sticker_user",
                                    title = "Sticker User",
                                    category = "FEATURES",
                                    desc = "Decorated a journal entry with mood stickers",
                                    fullDesc = "Adding personal flair! Using mood stickers brings playful expression and visual texture to your diary pages.",
                                    icon = TablerIcons.Notes,
                                    isUnlocked = stickerObj?.unlockedAt != null,
                                    unlockedAt = stickerObj?.unlockedAt
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AchievementGridCard(
                            title = "Theme Explorer",
                            desc = "Tried a new color theme",
                            icon = TablerIcons.Palette,
                            isUnlocked = themeObj?.unlockedAt != null,
                            cardBorderColor = cardBorderColor,
                            titleTextColor = titleTextColor,
                            subtextColor = subtextColor,
                            onClick = {
                                selectedAchievementDetail = AchievementDetailInfo(
                                    id = "theme_explorer",
                                    title = "Theme Explorer",
                                    category = "FEATURES",
                                    desc = "Tried out a new app color theme",
                                    fullDesc = "Personalizing your sanctuary! Changing the app theme creates a comfortable space tailored to your current mood.",
                                    icon = TablerIcons.Palette,
                                    isUnlocked = themeObj?.unlockedAt != null,
                                    unlockedAt = themeObj?.unlockedAt
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // REFLECTION GROUP
                    Text(
                        text = "REFLECTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = subtextColor,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val reflectionObj = achievements.find { it.id == "reflection_starter" }
                        val deepObj = achievements.find { it.id == "deep_reflection" }

                        AchievementGridCard(
                            title = "Reflection Starter",
                            desc = "Asked your AI coach f...",
                            icon = TablerIcons.Star,
                            isUnlocked = reflectionObj?.unlockedAt != null,
                            cardBorderColor = cardBorderColor,
                            titleTextColor = titleTextColor,
                            subtextColor = subtextColor,
                            onClick = {
                                selectedAchievementDetail = AchievementDetailInfo(
                                    id = "reflection_starter",
                                    title = "Reflection Starter",
                                    category = "REFLECTION",
                                    desc = "Generated your first AI reflection prompt",
                                    fullDesc = "Engaging with your inner coach! Using AI reflection prompts helps guide deeper self-discovery.",
                                    icon = TablerIcons.Star,
                                    isUnlocked = reflectionObj?.unlockedAt != null,
                                    unlockedAt = reflectionObj?.unlockedAt
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        AchievementGridCard(
                            title = "Deep Reflection User",
                            desc = "Five AI-analyzed...",
                            icon = TablerIcons.Activity,
                            isUnlocked = deepObj?.unlockedAt != null,
                            cardBorderColor = cardBorderColor,
                            titleTextColor = titleTextColor,
                            subtextColor = subtextColor,
                            onClick = {
                                selectedAchievementDetail = AchievementDetailInfo(
                                    id = "deep_reflection",
                                    title = "Deep Reflection User",
                                    category = "REFLECTION",
                                    desc = "Completed 5 or more AI-guided reflections",
                                    fullDesc = "A master of introspection! Consistently reviewing AI reflections provides transformative long-term self-awareness.",
                                    icon = TablerIcons.Activity,
                                    isUnlocked = deepObj?.unlockedAt != null,
                                    unlockedAt = deepObj?.unlockedAt
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // NOTIFICATION SETTINGS SECTION
        NotificationSettingsSection(
            appContainer = appContainer,
            periodTrackerEnabled = periodTrackerEnabled,
            darkTheme = darkTheme,
            cardBgColor = cardBgColor,
            cardBorderColor = cardBorderColor,
            titleTextColor = titleTextColor,
            subtextColor = subtextColor,
            iconBgColor = iconBgColor,
            iconTintColor = iconTintColor
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 4. ACCOUNT SECTION
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Account",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor
            )
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = cardBgColor,
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (darkTheme) Color(0xFF334155) else Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = TablerIcons.User,
                                contentDescription = null,
                                tint = subtextColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = if (userEmail.isNullOrBlank()) "No email" else userEmail!!,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleTextColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Signed in",
                                fontSize = 13.sp,
                                color = subtextColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = cardBorderColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showSignOutDialog = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = TablerIcons.Logout,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Sign out",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 5. ABOUT APP SECTION
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = cardBgColor,
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_transparent),
                        contentDescription = "Soul Sync Logo",
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Soul Sync",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleTextColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Version 1.0.0",
                        fontSize = 13.sp,
                        color = subtextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your personal wellness & self-reflection sanctuary",
                        fontSize = 12.sp,
                        color = subtextColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    text = "Sign out?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleTextColor
                )
            },
            text = {
                Text(
                    text = "You'll need to sign in again to access your entries.",
                    fontSize = 14.sp,
                    color = subtextColor
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        viewModel.signOut()
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Sign out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSignOutDialog = false },
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Text("Cancel", color = subtextColor)
                }
            },
            containerColor = cardBgColor
        )
    }

    if (selectedAchievementDetail != null) {
        val detail = selectedAchievementDetail!!
        AlertDialog(
            onDismissRequest = { selectedAchievementDetail = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            if (detail.isUnlocked) MaterialTheme.colorScheme.primaryContainer
                            else (if (darkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (detail.isUnlocked) detail.icon else TablerIcons.Lock,
                        contentDescription = null,
                        tint = if (detail.isUnlocked) MaterialTheme.colorScheme.primary else Color(0xFF94A3B8),
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = detail.category + " ACHIEVEMENT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = detail.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = detail.fullDesc,
                        fontSize = 14.sp,
                        color = subtextColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (detail.isUnlocked) MaterialTheme.colorScheme.primaryContainer else (if (darkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, if (detail.isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else cardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (detail.isUnlocked) TablerIcons.Check else TablerIcons.Lock,
                                contentDescription = null,
                                tint = if (detail.isUnlocked) MaterialTheme.colorScheme.primary else subtextColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (detail.isUnlocked) {
                                    val formattedDate = detail.unlockedAt?.let {
                                        java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(it))
                                    } ?: "Recently"
                                    "Unlocked on $formattedDate"
                                } else "Locked · Keep journaling to earn this badge!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (detail.isUnlocked) MaterialTheme.colorScheme.primary else subtextColor
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedAchievementDetail = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Got it", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = cardBgColor
        )
    }
}

data class AchievementDetailInfo(
    val id: String,
    val title: String,
    val category: String,
    val desc: String,
    val fullDesc: String,
    val icon: ImageVector,
    val isUnlocked: Boolean,
    val unlockedAt: Long?
)

@Composable
fun AchievementGridCard(
    title: String,
    desc: String,
    icon: ImageVector,
    isUnlocked: Boolean,
    cardBorderColor: Color,
    titleTextColor: Color,
    subtextColor: Color,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(154.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) MaterialTheme.colorScheme.primaryContainer else (if (LocalIsDarkTheme.current) Color(0xFF334155) else Color(0xFFF1F5F9))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) icon else TablerIcons.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) MaterialTheme.colorScheme.primary else Color(0xFF94A3B8),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) titleTextColor else titleTextColor.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 15.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                fontSize = 10.sp,
                color = subtextColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

fun getNameForBadge(id: String): String = when(id) {
    "first_steps" -> "First Steps"
    "rhythm_master" -> "Rhythm Master"
    "first_entry" -> "First Entry"
    "streak_3" -> "3 Day Streak"
    "streak_7" -> "7 Day Streak"
    "streak_14" -> "14 Day Consistency"
    "emotional_awareness" -> "Emotional Awareness"
    "pattern_breaker" -> "Pattern Breaker"
    "first_image" -> "First Image Added"
    "first_voice" -> "First Voice Note"
    "sticker_user" -> "Sticker User"
    "theme_explorer" -> "Theme Explorer"
    "reflection_starter" -> "Reflection Starter"
    "deep_reflection" -> "Deep Reflection User"
    else -> "Achievement"
}

@Composable
fun NotificationSettingsSection(
    appContainer: AppContainer,
    periodTrackerEnabled: Boolean,
    darkTheme: Boolean,
    cardBgColor: Color,
    cardBorderColor: Color,
    titleTextColor: Color,
    subtextColor: Color,
    iconBgColor: Color,
    iconTintColor: Color
) {
    val context = LocalContext.current
    val notifViewModel: NotificationViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NotificationViewModel(
                    appContainer.notificationPreferencesRepository,
                    appContainer.notificationScheduler
                ) as T
            }
        }
    )

    val state by notifViewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            notifViewModel.updateMasterEnabled(true)
            Toast.makeText(context, "Notifications enabled successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notifications permission denied. You can enable it in Settings.", Toast.LENGTH_LONG).show()
            notifViewModel.updateMasterEnabled(false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Notifications",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = titleTextColor
        )
        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = cardBgColor,
            border = BorderStroke(1.dp, cardBorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Master Notifications Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(iconBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = TablerIcons.Bell,
                                contentDescription = null,
                                tint = iconTintColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Enable Notifications",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleTextColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Receive timely gentle check-ins",
                                fontSize = 13.sp,
                                color = subtextColor
                            )
                        }
                    }
                    Switch(
                        checked = state.masterEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val check = ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.POST_NOTIFICATIONS
                                    )
                                    if (check == PackageManager.PERMISSION_GRANTED) {
                                        notifViewModel.updateMasterEnabled(true)
                                    } else {
                                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    notifViewModel.updateMasterEnabled(true)
                                }
                            } else {
                                notifViewModel.updateMasterEnabled(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = if (darkTheme) Color(0xFF334155) else Color(0xFFE2E8F0),
                            checkedBorderColor = MaterialTheme.colorScheme.primary,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }

                // If Master is enabled, expand to show granular settings
                if (state.masterEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = cardBorderColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. DAILY CHECK-INS GROUP
                    Text(
                        text = "DAILY CHECK-INS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = subtextColor,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Morning Mood Reminder Row
                    NotificationToggleTimeRow(
                        title = "Morning Mood Reminder",
                        subtitle = "Gentle prompt to log your start of day",
                        icon = TablerIcons.Sun,
                        checked = state.morningMoodEnabled,
                        time = state.morningMoodTime,
                        onCheckedChange = { notifViewModel.updateMorningMoodEnabled(it) },
                        onTimeClick = {
                            showTimePicker(context, state.morningMoodTime) {
                                notifViewModel.updateMorningMoodTime(it)
                            }
                        },
                        darkTheme = darkTheme,
                        titleTextColor = titleTextColor,
                        subtextColor = subtextColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Evening Journal Reminder Row
                    NotificationToggleTimeRow(
                        title = "Evening Journal Reminder",
                        subtitle = "Reflection prompt to write your thoughts",
                        icon = TablerIcons.Moon,
                        checked = state.eveningJournalEnabled,
                        time = state.eveningJournalTime,
                        onCheckedChange = { notifViewModel.updateEveningJournalEnabled(it) },
                        onTimeClick = {
                            showTimePicker(context, state.eveningJournalTime) {
                                notifViewModel.updateEveningJournalTime(it)
                            }
                        },
                        darkTheme = darkTheme,
                        titleTextColor = titleTextColor,
                        subtextColor = subtextColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Unfinished Drafts
                    NotificationToggleRow(
                        title = "Unfinished Drafts",
                        subtitle = "Remind me when journal entries are incomplete",
                        icon = TablerIcons.FileText,
                        checked = state.unfinishedDraftEnabled,
                        onCheckedChange = { notifViewModel.updateUnfinishedDraftEnabled(it) },
                        darkTheme = darkTheme,
                        titleTextColor = titleTextColor,
                        subtextColor = subtextColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gentle Return
                    NotificationToggleRow(
                        title = "Gentle Return Reminder",
                        subtitle = "Warm nudge if away for more than 3 days",
                        icon = TablerIcons.Clock,
                        checked = state.gentleReturnEnabled,
                        onCheckedChange = { notifViewModel.updateGentleReturnEnabled(it) },
                        darkTheme = darkTheme,
                        titleTextColor = titleTextColor,
                        subtextColor = subtextColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = cardBorderColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. INSIGHTS AND MEMORIES GROUP
                    Text(
                        text = "INSIGHTS & MEMORIES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = subtextColor,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Weekly Insight
                    NotificationToggleRow(
                        title = "Weekly Insight",
                        subtitle = "Summary of emotional and wellness patterns",
                        icon = TablerIcons.Award,
                        checked = state.weeklyInsightEnabled,
                        onCheckedChange = { notifViewModel.updateWeeklyInsightEnabled(it) },
                        darkTheme = darkTheme,
                        titleTextColor = titleTextColor,
                        subtextColor = subtextColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Monthly Reflection
                    NotificationToggleRow(
                        title = "Monthly Reflection",
                        subtitle = "Deep reflection lookback of past month",
                        icon = TablerIcons.Book,
                        checked = state.monthlyReflectionEnabled,
                        onCheckedChange = { notifViewModel.updateMonthlyReflectionEnabled(it) },
                        darkTheme = darkTheme,
                        titleTextColor = titleTextColor,
                        subtextColor = subtextColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Memory Resurfacing
                    NotificationToggleRow(
                        title = "Memory Resurfacing",
                        subtitle = "Revisit past moments from your journal",
                        icon = TablerIcons.History,
                        checked = state.memoryResurfacingEnabled,
                        onCheckedChange = { notifViewModel.updateMemoryResurfacingEnabled(it) },
                        darkTheme = darkTheme,
                        titleTextColor = titleTextColor,
                        subtextColor = subtextColor
                    )

                    // 3. CYCLE REMINDERS GROUP (only if period tracker enabled)
                    if (periodTrackerEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = cardBorderColor, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "HEALTH & CYCLE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = subtextColor,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        NotificationToggleRow(
                            title = "Cycle Reminders",
                            subtitle = "Alerts for predictions and cycle phases",
                            icon = TablerIcons.Droplet,
                            checked = state.cycleRemindersEnabled,
                            onCheckedChange = { notifViewModel.updateCycleRemindersEnabled(it) },
                            darkTheme = darkTheme,
                            titleTextColor = titleTextColor,
                            subtextColor = subtextColor
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = cardBorderColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))



                    Spacer(modifier = Modifier.height(16.dp))

                    // Active Days weekday selector (Row of 7 circular weekday chips)
                    Column {
                        Text(
                            text = "Active Days",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleTextColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                            weekdays.forEach { day ->
                                val isSelected = state.activeDays.contains(day)
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary 
                                            else (if (darkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color.Transparent else cardBorderColor,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            val currentSet = state.activeDays.toMutableSet()
                                            if (currentSet.contains(day)) {
                                                if (currentSet.size > 1) currentSet.remove(day) // Keep at least one active day
                                            } else {
                                                currentSet.add(day)
                                            }
                                            notifViewModel.updateActiveDays(currentSet)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.first().toString(),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else titleTextColor
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = cardBorderColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Quiet Hours Group
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Quiet Hours",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = titleTextColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Mute all notification deliveries during quiet intervals",
                                    fontSize = 12.sp,
                                    color = subtextColor
                                )
                            }
                            Switch(
                                checked = state.quietHoursEnabled,
                                onCheckedChange = { notifViewModel.updateQuietHoursEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = if (darkTheme) Color(0xFF334155) else Color(0xFFE2E8F0),
                                    checkedBorderColor = MaterialTheme.colorScheme.primary,
                                    uncheckedBorderColor = Color.Transparent
                                )
                            )
                        }

                        if (state.quietHoursEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Start Time
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            showTimePicker(context, state.quietHoursStart) {
                                                notifViewModel.updateQuietHoursStart(it)
                                            }
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (darkTheme) Color(0xFF0F172A) else Color(0xFFFAFAFA)),
                                    border = BorderStroke(1.dp, cardBorderColor)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Quiet Start", fontSize = 11.sp, color = subtextColor, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(state.quietHoursStart, fontSize = 15.sp, color = titleTextColor, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // End Time
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            showTimePicker(context, state.quietHoursEnd) {
                                                notifViewModel.updateQuietHoursEnd(it)
                                            }
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (darkTheme) Color(0xFF0F172A) else Color(0xFFFAFAFA)),
                                    border = BorderStroke(1.dp, cardBorderColor)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Quiet End", fontSize = 11.sp, color = subtextColor, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(state.quietHoursEnd, fontSize = 15.sp, color = titleTextColor, fontWeight = FontWeight.Bold)
                                    }
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
fun NotificationToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    darkTheme: Boolean,
    titleTextColor: Color,
    subtextColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = subtextColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleTextColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = subtextColor
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = if (darkTheme) Color(0xFF334155) else Color(0xFFE2E8F0),
                checkedBorderColor = MaterialTheme.colorScheme.primary,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun NotificationToggleTimeRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    time: String,
    onCheckedChange: (Boolean) -> Unit,
    onTimeClick: () -> Unit,
    darkTheme: Boolean,
    titleTextColor: Color,
    subtextColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = subtextColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleTextColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = subtextColor
                )
                if (checked) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onTimeClick() },
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = TablerIcons.Clock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatTimeDisplay(time),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = if (darkTheme) Color(0xFF334155) else Color(0xFFE2E8F0),
                checkedBorderColor = MaterialTheme.colorScheme.primary,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

fun showTimePicker(context: Context, currentTime: String, onTimeSelected: (String) -> Unit) {
    val parts = currentTime.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    
    android.app.TimePickerDialog(
        context,
        { _, h, m ->
            val formatted = String.format("%02d:%02d", h, m)
            onTimeSelected(formatted)
        },
        hour,
        minute,
        false // 12-hour mode
    ).show()
}

fun formatTimeDisplay(time: String): String {
    val parts = time.split(":")
    if (parts.size != 2) return time
    val h = parts[0].toIntOrNull() ?: return time
    val m = parts[1]
    val amPm = if (h >= 12) "PM" else "AM"
    val h12 = if (h == 0) 12 else if (h > 12) h - 12 else h
    return "$h12:$m $amPm"
}


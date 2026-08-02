package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import compose.icons.TablerIcons
import compose.icons.tablericons.*

data class CelebrationInfo(
    val id: String,
    val title: String,
    val category: String,
    val desc: String,
    val fullDesc: String,
    val icon: ImageVector
)

fun getCelebrationInfo(id: String): CelebrationInfo {
    return when (id) {
        "first_entry" -> CelebrationInfo(
            id = "first_entry",
            title = "First Entry",
            category = "STARTER",
            desc = "Wrote your first journal entry",
            fullDesc = "Your reflection journey begins! Writing your first diary entry lays the foundation for tracking your mood and emotional wellness over time.",
            icon = TablerIcons.Pencil
        )
        "streak_3" -> CelebrationInfo(
            id = "streak_3",
            title = "3 Day Streak",
            category = "STARTER",
            desc = "Logged entries for 3 consecutive days",
            fullDesc = "Building momentum! Journaling 3 days in a row starts establishing a regular habit for mindful self-reflection.",
            icon = TablerIcons.Sun
        )
        "streak_7" -> CelebrationInfo(
            id = "streak_7",
            title = "7 Day Streak",
            category = "STARTER",
            desc = "Logged entries for 7 consecutive days",
            fullDesc = "A full week of mindful reflection! Maintaining a 7-day journaling streak helps bring deep clarity and emotional focus.",
            icon = TablerIcons.CalendarEvent
        )
        "streak_14" -> CelebrationInfo(
            id = "streak_14",
            title = "14 Day Consistency",
            category = "GROWTH",
            desc = "Logged entries for 14 consecutive days",
            fullDesc = "Two full weeks of daily entries! Consistent journaling transforms brief reflections into lasting emotional resilience.",
            icon = TablerIcons.CalendarEvent
        )
        "emotional_awareness" -> CelebrationInfo(
            id = "emotional_awareness",
            title = "Emotional Awareness",
            category = "GROWTH",
            desc = "Logged 5 different mood states",
            fullDesc = "Expanding your emotional palette! Recognizing and logging various feelings helps develop higher emotional intelligence.",
            icon = TablerIcons.Heart
        )
        "pattern_breaker" -> CelebrationInfo(
            id = "pattern_breaker",
            title = "Pattern Breaker",
            category = "GROWTH",
            desc = "Discovered a recurring emotional pattern with AI",
            fullDesc = "Gaining deep insight! Using AI to identify recurring triggers empowers you to navigate emotional patterns constructively.",
            icon = TablerIcons.Eye
        )
        "first_image" -> CelebrationInfo(
            id = "first_image",
            title = "First Image Added",
            category = "FEATURES",
            desc = "Attached an image to a journal entry",
            fullDesc = "A picture is worth a thousand words! Preserving memories with photos enriches your personal diary history.",
            icon = TablerIcons.Photo
        )
        "first_voice" -> CelebrationInfo(
            id = "first_voice",
            title = "First Voice Note",
            category = "FEATURES",
            desc = "Recorded a voice note in a journal entry",
            fullDesc = "Speaking from the heart! Capturing raw voice thoughts brings authentic emotion to your journal entries.",
            icon = TablerIcons.Microphone
        )
        "sticker_user" -> CelebrationInfo(
            id = "sticker_user",
            title = "Sticker User",
            category = "FEATURES",
            desc = "Decorated a journal entry with mood stickers",
            fullDesc = "Adding personal flair! Using mood stickers brings playful expression and visual texture to your diary pages.",
            icon = TablerIcons.Notes
        )
        "theme_explorer" -> CelebrationInfo(
            id = "theme_explorer",
            title = "Theme Explorer",
            category = "FEATURES",
            desc = "Tried out a new app color theme",
            fullDesc = "Personalizing your sanctuary! Changing the app theme creates a comfortable space tailored to your current mood.",
            icon = TablerIcons.Palette
        )
        "reflection_starter" -> CelebrationInfo(
            id = "reflection_starter",
            title = "Reflection Starter",
            category = "REFLECTION",
            desc = "Generated your first AI reflection prompt",
            fullDesc = "Engaging with your inner coach! Using AI reflection prompts helps guide deeper self-discovery.",
            icon = TablerIcons.Star
        )
        "deep_reflection" -> CelebrationInfo(
            id = "deep_reflection",
            title = "Deep Reflection User",
            category = "REFLECTION",
            desc = "Completed 5 or more AI-guided reflections",
            fullDesc = "A master of introspection! Consistently reviewing AI reflections provides transformative long-term self-awareness.",
            icon = TablerIcons.Activity
        )
        "first_steps" -> CelebrationInfo(
            id = "first_steps",
            title = "First Steps",
            category = "STARTER",
            desc = "Took your first steps in SoulSync",
            fullDesc = "Welcome to SoulSync! Taking your first steps sets you on a path to greater mindfulness.",
            icon = TablerIcons.Heart
        )
        "rhythm_master" -> CelebrationInfo(
            id = "rhythm_master",
            title = "Rhythm Master",
            category = "GROWTH",
            desc = "Mastered the rhythm of your cycle",
            fullDesc = "Harmonizing with your body! Understanding your cycle builds self-compassion and wellness.",
            icon = TablerIcons.Activity
        )
        else -> CelebrationInfo(
            id = id,
            title = "Achievement Unlocked",
            category = "ACHIEVEMENT",
            desc = "Congratulations!",
            fullDesc = "You unlocked a new milestone. Keep up the amazing work!",
            icon = TablerIcons.Trophy
        )
    }
}

@Composable
fun CelebrationDialog(
    info: CelebrationInfo,
    onDismiss: () -> Unit
) {
    val scaleAnim = remember { Animatable(0.3f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(500, easing = EaseOutQuad)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f * alphaAnim.value)),
            contentAlignment = Alignment.Center
        ) {
            // Confetti Canvas Overlay
            ConfettiCanvas(modifier = Modifier.fillMaxSize())

            // Main Celebration Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .wrapContentHeight()
                    .scale(scaleAnim.value)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Category Badge
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = info.category,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    // Floating/Glowing Trophy Box
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = info.icon,
                                contentDescription = "Achievement Icon",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "ACHIEVEMENT UNLOCKED!",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = info.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = info.desc,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = info.fullDesc,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(14.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "Hooray! 🎉",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConfettiCanvas(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val particles = remember {
        List(45) {
            ConfettiParticle(
                x = (0..100).random() / 100f,
                y = (0..100).random() / 100f,
                speedY = (1..3).random() / 2f,
                speedX = (-1..1).random() / 4f,
                color = listOf(
                    Color(0xFFFFD700), // Gold
                    Color(0xFFFF6B6B), // Red/coral
                    Color(0xFF4DABF7), // Blue
                    Color(0xFF51CF66), // Green
                    Color(0xFFFCC419), // Yellow
                    Color(0xFFF06595)  // Pink
                ).random(),
                size = (6..14).random().toFloat(),
                rotationSpeed = (1..5).random() * 90f
            )
        }
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        particles.forEach { p ->
            val currentY = (p.y * height + progress * p.speedY * height) % height
            val currentX = (p.x * width + progress * p.speedX * width) % width
            val currentRotation = progress * p.rotationSpeed

            drawContext.canvas.save()
            drawContext.canvas.translate(currentX, currentY)
            drawContext.canvas.rotate(currentRotation)
            drawRect(
                color = p.color,
                size = androidx.compose.ui.geometry.Size(p.size, p.size / 2f)
            )
            drawContext.canvas.restore()
        }
    }
}

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val speedY: Float,
    val speedX: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float
)

package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Minimalist Light Palette ──────────────────────────────────────────
val PurplePrimary = Color(0xFFA855F7) // Vibrant Royal Purple Accent
val PurpleHighlight = Color(0xFFE9D5FF) // Light Violet Accent

// Preserved symbol names for backward compatibility
val BluePrimary = Color(0xFFA855F7)
val BlueHighlight = Color(0xFFE9D5FF)
val SageGreen = Color(0xFFA855F7)

val CharcoalTint = Color(0xFF1E293B) // Dark text for primary text
val SlateGrey = Color(0xFF64748B) // Soft grey for secondary text

val PrimaryCardTonal = Color(0xFFFFFFFF) // White surface for cards
val SecondaryHighlightTonal = Color(0xFFF8FAFC) // Off-white highlight
val NestedContainerTonal = Color(0xFFF1F5F9) // Slightly darker off-white container
val PremiumBackground = Color(0xFFFCFCFD) // Lightest grey/white background

val OutlineLight = Color(0xFFF1F5F9) // Subtle outline
val OutlineDark = Color(0xFFE2E8F0) // Slightly darker outline

val ErrorTerracotta = Color(0xFFEF4444)
val WarningAmber = Color(0xFFF59E0B)

// Keeping these for legacy fallbacks
val DeepPlum = Color(0xFFFCFCFD)
val DarkPlum = Color(0xFFFFFFFF)
val DarkSurfaceVariant = Color(0xFFF8FAFC)
val LightTextDark = Color(0xFF0F172A)
val MutedTextDark = Color(0xFF475569)
val ErrorCoralDark = Color(0xFFEF4444)
val SuccessGreen = Color(0xFF10B981)
val InfoBlue = Color(0xFF3B82F6)

// ─── Flat Pastel Mood Colors ───────────────────────────────────────────────────
object MoodColors {
    val Happy = Color(0xFFF59E0B) 
    val HappyBg = Color(0xFFFEF3C7) // Very light amber
    val Loved = Color(0xFFEC4899) 
    val LovedBg = Color(0xFFFCE7F3) // Very light pink
    val Neutral = Color(0xFF94A3B8) 
    val NeutralBg = Color(0xFFF1F5F9) // Very light slate
    val Anxious = Color(0xFF8B5CF6) 
    val AnxiousBg = Color(0xFFEDE9FE) // Very light violet
    val Sad = Color(0xFF3B82F6) 
    val SadBg = Color(0xFFDBEAFE) // Very light blue
    val Angry = Color(0xFFEF4444) 
    val AngryBg = Color(0xFFFEE2E2) // Very light red
    val Numb = Color(0xFF64748B) 
    val NumbBg = Color(0xFFF1F5F9) // Very light slate
}

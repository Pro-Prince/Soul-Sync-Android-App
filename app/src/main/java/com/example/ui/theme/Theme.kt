package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.runtime.staticCompositionLocalOf

fun getThemeColorScheme(themeName: String = "SOUL_PINK", isDarkMode: Boolean = false): ColorScheme {
    return if (isDarkMode) {
        val (primaryCol, containerCol, onContainerCol, secContainerCol) = when (themeName) {
            "WARM_ROSE" -> Quad(Color(0xFFFB7185), Color(0xFF881337), Color(0xFFFECDD3), Color(0xFF4C0519))
            "WARM_SAND" -> Quad(Color(0xFFD4A373), Color(0xFF78350F), Color(0xFFFDE68A), Color(0xFF451A03))
            "LAVENDER_CALM" -> Quad(Color(0xFFC4B5FD), Color(0xFF312E81), Color(0xFFEDE9FE), Color(0xFF1E1B4B))
            else -> Quad(Color(0xFFF472B6), Color(0xFF831843), Color(0xFFFBCFE8), Color(0xFF500724)) // SOUL_PINK default
        }
        darkColorScheme(
            primary = primaryCol,
            secondary = primaryCol,
            tertiary = primaryCol,
            tertiaryContainer = containerCol,
            primaryContainer = containerCol,
            secondaryContainer = secContainerCol,
            background = Color(0xFF0F172A),
            surface = Color(0xFF1E293B),
            surfaceVariant = Color(0xFF334155),
            onPrimary = Color(0xFF1E1B4B),
            onSecondary = Color(0xFF1E1B4B),
            onBackground = Color(0xFFF8FAFC),
            onSurface = Color(0xFFF8FAFC),
            onSurfaceVariant = Color(0xFF94A3B8),
            onPrimaryContainer = onContainerCol,
            outline = Color(0xFF334155),
            outlineVariant = Color(0xFF334155),
            error = Color(0xFFEF4444),
            onError = Color.White
        )
    } else {
        val (primaryCol, containerCol, onContainerCol, secContainerCol) = when (themeName) {
            "WARM_ROSE" -> Quad(Color(0xFFE11D48), Color(0xFFFFE4E6), Color(0xFF9F1239), Color(0xFFFFF1F2))
            "WARM_SAND" -> Quad(Color(0xFFB45309), Color(0xFFFEF3C7), Color(0xFF92400E), Color(0xFFFFFBEB))
            "LAVENDER_CALM" -> Quad(Color(0xFF8B5CF6), Color(0xFFF3E8FF), Color(0xFF581C87), Color(0xFFFAF5FF))
            else -> Quad(Color(0xFFF472B6), Color(0xFFFCE7F3), Color(0xFF9D174D), Color(0xFFFFF1F2)) // SOUL_PINK default
        }
        lightColorScheme(
            primary = primaryCol,
            secondary = primaryCol,
            tertiary = primaryCol,
            tertiaryContainer = containerCol,
            primaryContainer = containerCol,
            secondaryContainer = secContainerCol,
            background = Color(0xFFFAFAFA),
            surface = Color.White,
            surfaceVariant = Color(0xFFF1F5F9),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF1E1E1E),
            onSurface = Color(0xFF1E1E1E),
            onSurfaceVariant = Color(0xFF71717A),
            onPrimaryContainer = onContainerCol,
            outline = Color(0xFFF1F5F9),
            outlineVariant = Color(0xFFE2E8F0),
            error = ErrorTerracotta,
            onError = Color.White
        )
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

val LocalIsDarkTheme = staticCompositionLocalOf { false }

@Composable
fun SoulSyncTheme(
    themeName: String = "SOUL_PINK",
    forceDark: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = forceDark
    val colorScheme = getThemeColorScheme(themeName, isDark)
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity
            if (activity != null) {
                val window = activity.window
                
                // Edge-to-edge system bars configurations
                WindowCompat.setDecorFitsSystemWindows(window, false)
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT

                val windowInsetsController = WindowCompat.getInsetsController(window, view)
                // In Light mode, we want dark icons (true), in Dark mode, light icons (false)
                windowInsetsController.isAppearanceLightStatusBars = !isDark
                windowInsetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalIsDarkTheme provides isDark) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = SoulShapes,
            content = content
        )
    }
}


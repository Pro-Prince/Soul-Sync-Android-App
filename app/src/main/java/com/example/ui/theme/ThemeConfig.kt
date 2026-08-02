package com.example.ui.theme

import androidx.compose.ui.graphics.Color

sealed class ThemeConfig(
    val id: String,
    val primary: Color,
    val accent: Color,
    val surface: Color
) {
    object SoulPink : ThemeConfig("SOUL_PINK", BluePrimary, BlueHighlight, NestedContainerTonal)

    companion object {
        fun fromId(id: String): ThemeConfig {
            return SoulPink
        }
    }
}


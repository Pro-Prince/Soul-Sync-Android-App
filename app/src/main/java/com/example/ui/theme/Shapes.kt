package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

val SoulShapes = Shapes(
    extraSmall = RoundedCornerShape(Spacing.small),
    small = RoundedCornerShape(Spacing.inputCorner),
    medium = RoundedCornerShape(Spacing.cardCorner),
    large = RoundedCornerShape(Spacing.cardCornerLarge),
    extraLarge = RoundedCornerShape(Spacing.sheetCorner)
)

// Convenience aliases for direct usage
val CardShape = RoundedCornerShape(Spacing.cardCorner)
val CardShapeLarge = RoundedCornerShape(Spacing.cardCornerLarge)
val PillShape = RoundedCornerShape(Spacing.buttonCorner)
val ChipShape = RoundedCornerShape(Spacing.chipCorner)
val InputShape = RoundedCornerShape(Spacing.inputCorner)
val SheetShape = RoundedCornerShape(topStart = Spacing.sheetCorner, topEnd = Spacing.sheetCorner)

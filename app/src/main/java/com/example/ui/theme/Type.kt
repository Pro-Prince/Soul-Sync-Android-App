package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val DMSerifDisplay = FontFamily(
    Font(googleFont = GoogleFont("DM Serif Display"), fontProvider = provider)
)

val DMSans = FontFamily(
    Font(googleFont = GoogleFont("DM Sans"), fontProvider = provider),
    Font(googleFont = GoogleFont("DM Sans"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("DM Sans"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("DM Sans"), fontProvider = provider, weight = FontWeight.Bold)
)

val Lora = FontFamily(
    Font(googleFont = GoogleFont("Lora"), fontProvider = provider),
    Font(googleFont = GoogleFont("Lora"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Lora"), fontProvider = provider, style = androidx.compose.ui.text.font.FontStyle.Italic)
)

val Typography = Typography(
    displayLarge = TextStyle(fontFamily = DMSerifDisplay, fontWeight = FontWeight.Normal, fontSize = 42.sp, lineHeight = 52.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = DMSerifDisplay, fontWeight = FontWeight.Normal, fontSize = 36.sp, lineHeight = 44.sp),
    displaySmall = TextStyle(fontFamily = DMSerifDisplay, fontWeight = FontWeight.Normal, fontSize = 30.sp, lineHeight = 38.sp),
    
    headlineLarge = TextStyle(fontFamily = DMSerifDisplay, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = DMSerifDisplay, fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = DMSerifDisplay, fontWeight = FontWeight.Normal, fontSize = 20.sp, lineHeight = 28.sp),
    
    titleLarge = TextStyle(fontFamily = DMSerifDisplay, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 30.sp),
    titleMedium = TextStyle(fontFamily = DMSerifDisplay, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 26.sp),
    titleSmall = TextStyle(fontFamily = DMSerifDisplay, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    
    bodyLarge = TextStyle(fontFamily = Lora, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 20.sp),
    
    labelLarge = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 22.sp),
    labelMedium = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp)
)

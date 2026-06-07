package com.example.pyxis.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.pyxis.R

// ── K2D font family ────────────────────────────────────────────────────────────
// Place these font files in res/font/:
//   k2d_regular.ttf   k2d_medium.ttf   k2d_semibold.ttf   k2d_bold.ttf
// Download from: https://fonts.google.com/specimen/K2D

val K2D = FontFamily(
    Font(R.font.k2d_regular,  FontWeight.Normal),
    Font(R.font.k2d_medium,   FontWeight.Medium),
    Font(R.font.k2d_semibold, FontWeight.SemiBold),
    Font(R.font.k2d_bold,     FontWeight.Bold)
)

val PyxisTypography = Typography(
    displayLarge  = TextStyle(fontFamily = K2D, fontWeight = FontWeight.Bold,   fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = K2D, fontWeight = FontWeight.Bold,   fontSize = 45.sp),
    displaySmall  = TextStyle(fontFamily = K2D, fontWeight = FontWeight.Bold,   fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = K2D, fontWeight = FontWeight.SemiBold, fontSize = 32.sp),
    headlineMedium= TextStyle(fontFamily = K2D, fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = K2D, fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleLarge    = TextStyle(fontFamily = K2D, fontWeight = FontWeight.Medium, fontSize = 22.sp),
    titleMedium   = TextStyle(fontFamily = K2D, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    titleSmall    = TextStyle(fontFamily = K2D, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodyLarge     = TextStyle(fontFamily = K2D, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium    = TextStyle(fontFamily = K2D, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall     = TextStyle(fontFamily = K2D, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge    = TextStyle(fontFamily = K2D, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium   = TextStyle(fontFamily = K2D, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall    = TextStyle(fontFamily = K2D, fontWeight = FontWeight.Medium, fontSize = 11.sp),
)

// ── Colours — matching Figma dark background (#161B1E region) ─────────────────

private val DarkColors = darkColorScheme(
    background        = Color(0xFF111518),
    surface           = Color(0xFF1C2126),
    surfaceVariant    = Color(0xFF252C33),
    primary           = Color(0xFFEF745C),   // ember accent from preset 1
    onPrimary         = Color(0xFFFFFFFF),
    primaryContainer  = Color(0xFF3D1A12),
    onPrimaryContainer= Color(0xFFFFDAD2),
    secondary         = Color(0xFF9E8F88),
    onSecondary       = Color(0xFF1F1B19),
    secondaryContainer= Color(0xFF252221),
    onSurface         = Color(0xFFE3E2E0),
    onSurfaceVariant  = Color(0xFFC8C6C3),
    error             = Color(0xFFCF6679),
    errorContainer    = Color(0xFF3B1219),
)

private val LightColors = lightColorScheme(
    primary           = Color(0xFFEF745C),
    onPrimary         = Color(0xFFFFFFFF),
    primaryContainer  = Color(0xFFFFDAD2),
    onPrimaryContainer= Color(0xFF3D1A12),
)

@Composable
fun PyxisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = PyxisTypography,
        content     = content
    )
}
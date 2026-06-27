package com.emeth.kernel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

val BackgroundLight = Color(0xFFF2F6FA)
val SurfaceLight = Color(0xFFFFFFFF)
val TextPrimaryLight = Color(0xFF1C1C1E)
val TextSecondaryLight = Color(0xFF8E8E93)
val AccentBlue = Color(0xFF007AFF)

val BackgroundDark = Color(0xFF090B0E)
val SurfaceDark = Color(0xFF20252B)
val TextPrimaryDark = Color(0xFFFFFFFF)
val TextSecondaryDark = Color(0xFF8E8E93)

private val LightColors = lightColorScheme(
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    primary = AccentBlue,
    secondary = TextSecondaryLight
)

private val DarkColors = darkColorScheme(
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    primary = AccentBlue,
    secondary = TextSecondaryDark
)

val EmethTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )
)

@Composable
fun EmethTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = EmethTypography,
        content = content
    )
}

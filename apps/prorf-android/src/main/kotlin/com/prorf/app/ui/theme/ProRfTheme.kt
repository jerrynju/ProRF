package com.prorf.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** App-level theme state, provided by MainActivity and consumed by SettingsScreen. */
data class ProRfThemeState(
    val isDarkTheme: Boolean,
    val toggleTheme: () -> Unit,
)

val LocalProRfTheme = staticCompositionLocalOf {
    ProRfThemeState(isDarkTheme = false, toggleTheme = {})
}

private val PrimaryBlue = Color(0xFF2F80ED)
private val PrimaryBlueDark = Color(0xFF5B9FF0)
private val SecondaryGreen = Color(0xFF27AE60)
private val SecondaryGreenDark = Color(0xFF4DC47D)
private val ErrorRed = Color(0xFFEB5757)

private val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3EEFF),
    onPrimaryContainer = Color(0xFF001B50),
    secondary = SecondaryGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5F5E5),
    onSecondaryContainer = Color(0xFF00391E),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFE8E8),
    onErrorContainer = Color(0xFF5C0000),
    background = Color(0xFFF4F6FA),
    onBackground = Color(0xFF0D1117),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0D1117),
    surfaceVariant = Color(0xFFEFF2F8),
    onSurfaceVariant = Color(0xFF5A6478),
    outline = Color(0xFFB0B8C8),
    outlineVariant = Color(0xFFDDE1EA),
)

private val DarkColors = darkColorScheme(
    primary = PrimaryBlueDark,
    onPrimary = Color(0xFF001B50),
    primaryContainer = Color(0xFF0A3057),
    onPrimaryContainer = Color(0xFFAED4FF),
    secondary = SecondaryGreenDark,
    onSecondary = Color(0xFF00391E),
    secondaryContainer = Color(0xFF004A28),
    onSecondaryContainer = Color(0xFFA8F0C8),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF4D1010),
    onErrorContainer = Color(0xFFFFCFCF),
    background = Color(0xFF0F1117),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF1A1D28),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF252830),
    onSurfaceVariant = Color(0xFF8E98AC),
    outline = Color(0xFF3A3F50),
    outlineVariant = Color(0xFF2C303C),
)

// Engineering-focused typography: clean, readable, technical
private val ProRfTypography = Typography(
    // Large display — workflow name, screen titles
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp,
    ),
    // Section headers, top bar titles
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.1).sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    // Computed values, primary numerical output
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
    ),
    // Labels, tags, badges, category headers
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp,
    ),
)

@Composable
fun ProRfTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ProRfTypography,
        content = content,
    )
}

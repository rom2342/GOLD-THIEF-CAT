package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PrivacyColorScheme = darkColorScheme(
    primary = ShieldCyan,
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF005044),
    onPrimaryContainer = Color(0xFF73F8DF),
    secondary = ShieldElectricBlue,
    onSecondary = Color(0xFF00344F),
    secondaryContainer = Color(0xFF004C70),
    onSecondaryContainer = Color(0xFFBFE9FF),
    tertiary = ShieldAmber,
    background = CyberBackground,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CyberBorder,
    error = ShieldWarningRed,
    onError = Color.White
)

@Composable
fun CamMicBlockerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PrivacyColorScheme,
        typography = Typography,
        content = content
    )
}

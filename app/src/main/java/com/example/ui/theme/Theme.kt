package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TurfGreenLight,
    onPrimary = Color.Black,
    primaryContainer = TurfGreenDark,
    onPrimaryContainer = TurfGreenContainer,
    secondary = BoundaryGold,
    onSecondary = Color.Black,
    secondaryContainer = BoundaryGoldDark,
    onSecondaryContainer = BoundaryGoldContainer,
    tertiary = CricketBallRedLight,
    onTertiary = Color.Black,
    tertiaryContainer = CricketBallRed,
    onTertiaryContainer = CricketBallRedContainer,
    background = StadiumNightDark,
    onBackground = Color(0xFFE8F5E9),
    surface = StadiumSurfaceDark,
    onSurface = Color(0xFFE8F5E9),
    surfaceVariant = StadiumSurfaceCardDark,
    onSurfaceVariant = Color(0xFFB9D5C3),
    outline = StadiumBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = TurfGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = TurfGreenContainer,
    onPrimaryContainer = OnTurfGreenContainer,
    secondary = BoundaryGoldDark,
    onSecondary = Color.White,
    secondaryContainer = BoundaryGoldContainer,
    onSecondaryContainer = Color(0xFF261900),
    tertiary = CricketBallRed,
    onTertiary = Color.White,
    tertiaryContainer = CricketBallRedContainer,
    onTertiaryContainer = OnCricketBallRedContainer,
    background = StadiumNightLight,
    onBackground = Color(0xFF0F1E15),
    surface = StadiumSurfaceLight,
    onSurface = Color(0xFF0F1E15),
    surfaceVariant = StadiumSurfaceCardLight,
    onSurfaceVariant = Color(0xFF2A4837),
    outline = StadiumBorderLight
)

@Composable
fun GullyCricketTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted cricket theme colors
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.fumacanorte.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = CanopyEmeraldLight,
    onPrimary = OnCanopyEmeraldLight,
    primaryContainer = CanopyContainerLight,
    onPrimaryContainer = OnCanopyContainerLight,
    secondary = RiverSlateLight,
    onSecondary = OnRiverSlateLight,
    secondaryContainer = RiverContainerLight,
    onSecondaryContainer = OnRiverContainerLight,
    tertiary = SunAmberLight,
    onTertiary = OnSunAmberLight,
    tertiaryContainer = SunContainerLight,
    onTertiaryContainer = OnSunContainerLight,
    background = MistSurfaceLight,
    onBackground = OnMistSurfaceLight,
    surface = MistSurfaceLight,
    onSurface = OnMistSurfaceLight,
    surfaceVariant = MistSurfaceContainerLight,
    onSurfaceVariant = OnMistSurfaceVariantLight,
    surfaceContainerLow = MistSurfaceContainerLowLight,
    surfaceContainer = MistSurfaceContainerLight,
    surfaceContainerHigh = MistSurfaceContainerHighLight,
    outline = OutlineMistLight,
    error = ErrorTerracottaLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

private val DarkColorScheme = darkColorScheme(
    primary = CanopyEmeraldDark,
    onPrimary = OnCanopyEmeraldDark,
    primaryContainer = CanopyContainerDark,
    onPrimaryContainer = OnCanopyContainerDark,
    secondary = RiverSlateDark,
    onSecondary = OnRiverSlateDark,
    secondaryContainer = RiverContainerDark,
    onSecondaryContainer = OnRiverContainerDark,
    tertiary = SunAmberDark,
    onTertiary = OnSunAmberDark,
    tertiaryContainer = SunContainerDark,
    onTertiaryContainer = OnSunContainerDark,
    background = NightSurfaceDark,
    onBackground = OnNightSurfaceDark,
    surface = NightSurfaceDark,
    onSurface = OnNightSurfaceDark,
    surfaceVariant = NightSurfaceContainerDark,
    onSurfaceVariant = OnNightSurfaceVariantDark,
    surfaceContainerLow = NightSurfaceContainerLowDark,
    surfaceContainer = NightSurfaceContainerDark,
    surfaceContainerHigh = NightSurfaceContainerHighDark,
    outline = OutlineNightDark,
    error = ErrorTerracottaDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

@Composable
fun FumacaNorteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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
        typography = FumacaNorteTypography,
        content = content
    )
}

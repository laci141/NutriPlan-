package com.nutriplan.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.nutriplan.app.domain.model.ThemeMode

/**
 * A NutriPlan Material 3 témája világos és sötét változattal.
 */

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = LightSurface,
    primaryContainer = GreenContainer,
    onPrimaryContainer = OnGreenContainer,
    secondary = GreenPrimaryDark,
    onSecondary = LightSurface,
    background = LightBackground,
    onBackground = Color0F172A,
    surface = LightSurface,
    onSurface = Color0F172A,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color334155,
    error = ErrorRed,
    onError = LightSurface
)

private val DarkColors = darkColorScheme(
    primary = GreenPrimaryLight,
    onPrimary = DarkBackground,
    primaryContainer = DarkGreenContainer,
    onPrimaryContainer = OnDarkGreenContainer,
    secondary = GreenPrimary,
    onSecondary = LightSurface,
    background = DarkBackground,
    onBackground = ColorF1F5F9,
    surface = DarkSurface,
    onSurface = ColorF1F5F9,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = ColorCBD5E1,
    error = ErrorRed,
    onError = LightSurface
)

/**
 * A téma komponens, amely a kiválasztott mód szerint dönt világos/sötét között.
 */
@Composable
fun NutriPlanTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    // A "rendszer" mód esetén az operációs rendszer beállítását követjük
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = NutriPlanTypography,
        content = content
    )
}

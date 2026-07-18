package com.nutriplan.app.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.nutriplan.app.domain.model.ThemeMode

/**
 * A NutriPlan Material 3 témája világos és sötét változattal,
 * valamint Android 12+ esetén dinamikus (rendszer háttérképből származó) színekkel.
 */

// Világos téma – márka-zöld alapokon, lágy felületekkel
private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = LightSurface,
    primaryContainer = GreenContainer,
    onPrimaryContainer = OnGreenContainer,
    secondary = GreenPrimaryDark,
    onSecondary = LightSurface,
    tertiary = AmberAccent,
    background = LightBackground,
    onBackground = Color0F172A,
    surface = LightSurface,
    onSurface = Color0F172A,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color334155,
    outline = Color94A3B8,
    error = ErrorRed,
    onError = LightSurface
)

// Sötét téma – mélykék/szürke tónusok (NEM tiszta fekete), kellő kontraszttal
private val DarkColors = darkColorScheme(
    primary = GreenPrimaryLight,
    onPrimary = DarkBackground,
    primaryContainer = DarkGreenContainer,
    onPrimaryContainer = OnDarkGreenContainer,
    secondary = GreenPrimary,
    onSecondary = LightSurface,
    tertiary = AmberAccent,
    background = DarkBackground,
    onBackground = ColorF1F5F9,
    surface = DarkSurface,
    onSurface = ColorF1F5F9,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = ColorCBD5E1,
    outline = Color64748B,
    error = ErrorRed,
    onError = LightSurface
)

/**
 * A téma komponens.
 *
 * @param themeMode a felhasználó által választott mód (világos/sötét/rendszer)
 * @param dynamicColor Android 12+ esetén a rendszerszintű dinamikus színek használata
 */
@Composable
fun NutriPlanTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // A "rendszer" mód az operációs rendszer beállítását követi
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val colors = when {
        // Android 12 (API 31) felett a rendszer háttérképből generált színek
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = NutriPlanTypography,
        shapes = NutriPlanShapes,
        content = content
    )
}

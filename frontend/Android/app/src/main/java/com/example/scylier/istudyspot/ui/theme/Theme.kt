package com.example.scylier.istudyspot.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

object ThemeState {
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)

    fun toggle() {
        themeMode = when (themeMode) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
        }
    }
}

data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val onGradient: Color,
    val onGradientVariant: Color
)

val LightExtendedColors = ExtendedColors(
    success = Success,
    onSuccess = Color.White,
    successContainer = SuccessContainer,
    onSuccessContainer = OnSuccessContainer,
    warning = Warning,
    onWarning = Color.White,
    warningContainer = WarningContainer,
    onWarningContainer = OnWarningContainer,
    info = Info,
    onInfo = Color.White,
    infoContainer = InfoContainer,
    onInfoContainer = OnInfoContainer,
    gradientStart = GradientStart,
    gradientEnd = GradientEnd,
    onGradient = Color.White,
    onGradientVariant = Color.White.copy(alpha = 0.85f)
)

val DarkExtendedColors = ExtendedColors(
    success = DarkSuccess,
    onSuccess = Color(0xFF052E16),
    successContainer = DarkSuccessContainer,
    onSuccessContainer = DarkOnSuccessContainer,
    warning = DarkWarning,
    onWarning = Color(0xFF451A03),
    warningContainer = DarkWarningContainer,
    onWarningContainer = DarkOnWarningContainer,
    info = DarkInfo,
    onInfo = Color(0xFF0C1E3A),
    infoContainer = DarkInfoContainer,
    onInfoContainer = DarkOnInfoContainer,
    gradientStart = DarkGradientStart,
    gradientEnd = DarkGradientEnd,
    onGradient = Color.White,
    onGradientVariant = Color.White.copy(alpha = 0.85f)
)

val LocalExtendedColors = compositionLocalOf { LightExtendedColors }
val LocalThemeMode = compositionLocalOf { ThemeMode.SYSTEM }
val LocalThemeToggle = compositionLocalOf<() -> Unit> { {} }

@Composable
fun ThemeProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalThemeMode provides ThemeState.themeMode,
        LocalThemeToggle provides { ThemeState.toggle() }
    ) {
        content()
    }
}

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = SurfaceDim,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    error = Error,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimaryContainer,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = Color.White,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkSurfaceBright,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceContainer,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    errorContainer = DarkErrorContainer,
)

@Composable
fun IStudySpotTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = IStudySpotTypography,
            content = content
        )
    }
}

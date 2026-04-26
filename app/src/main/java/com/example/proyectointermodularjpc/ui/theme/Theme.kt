package com.example.proyectointermodularjpc.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.navigation.NavController

// Esquema de colores claro (Light)
private val LightColorScheme = lightColorScheme(
    // Colores primarios
    primary = BlueCorporate,
    onPrimary = WhitePure,
    primaryContainer = BlueCorporateLight,
    onPrimaryContainer = BlueCorporateDark,
    
    // Colores secundarios
    secondary = GreyMedium,
    onSecondary = WhitePure,
    secondaryContainer = GreyLight,
    onSecondaryContainer = GreyDark,
    
    // Colores terciarios (para variación)
    tertiary = SuccessGreen,
    onTertiary = WhitePure,
    tertiaryContainer = SuccessGreenLight,
    onTertiaryContainer = SuccessGreenDark,
    
    // Colores de error
    error = ErrorRed,
    onError = WhitePure,
    errorContainer = ErrorRedLight,
    onErrorContainer = ErrorRedDark,
    
    // Fondos y superficies
    background = WhitePure,
    onBackground = TextDark,
    surface = GreyNeutral,
    onSurface = TextDark,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextMedium,
    
    // Bordes y divisores
    outline = GreyLight,
    outlineVariant = OutlineVariant,
    
    // Scrim
    scrim = Color(0xFF000000).copy(alpha = 0.32f)
)

// Esquema de colores oscuro (Dark)
private val DarkColorScheme = darkColorScheme(
    // Colores primarios
    primary = BlueCorporateLight,
    onPrimary = BlueCorporateDark,
    primaryContainer = BlueCorporateDark,
    onPrimaryContainer = BlueCorporateLight,
    
    // Colores secundarios
    secondary = GreyLight,
    onSecondary = GreyDark,
    secondaryContainer = GreyMedium,
    onSecondaryContainer = GreyLight,
    
    // Colores terciarios
    tertiary = SuccessGreenLight,
    onTertiary = SuccessGreenDark,
    tertiaryContainer = SuccessGreenDark,
    onTertiaryContainer = SuccessGreenLight,
    
    // Colores de error
    error = ErrorRedLight,
    onError = ErrorRedDark,
    errorContainer = ErrorRedDark,
    onErrorContainer = ErrorRedLight,
    
    // Fondos y superficies
    background = Color(0xFF121212),
    onBackground = WhitePure,
    surface = Color(0xFF1E1E1E),
    onSurface = WhitePure,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = TextLight,
    
    // Bordes y divisores
    outline = GreyMedium,
    outlineVariant = GreyDark,
    
    // Scrim
    scrim = Color(0xFF000000).copy(alpha = 0.32f)
)

@Composable
fun ProyectoIntermodularJPCTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Acceso rápido a colores sin tema para casos específicos
object AppColors {
    // Primarios
    val primaryBlue = BlueCorporate
    val primaryBlueDark = BlueCorporateDark
    val primaryBlueLight = BlueCorporateLight
    
    // Neutros
    val white = WhitePure
    val greyNeutral = GreyNeutral
    val greyLight = GreyLight
    val greyMedium = GreyMedium
    val greyDark = GreyDark
    
    // Estados
    val success = SuccessGreen
    val successLight = SuccessGreenLight
    val successDark = SuccessGreenDark
    
    val error = ErrorRed
    val errorLight = ErrorRedLight
    val errorDark = ErrorRedDark
    
    val warning = WarningYellow
    val warningLight = WarningYellowLight
    val warningDark = WarningYellowDark
    
    // Texto
    val textDark = TextDark
    val textMedium = TextMedium
    val textLight = TextLight
}

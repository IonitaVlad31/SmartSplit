package com.example.smartsplit.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CrimsonRed,
    onPrimary = PureWhite,
    primaryContainer = DarkRed,
    onPrimaryContainer = PureWhite,
    
    secondary = SoftRed,
    onSecondary = PureWhite,
    secondaryContainer = LightGray,
    onSecondaryContainer = OffWhite,
    
    tertiary = CrimsonRed,
    onTertiary = PureWhite,
    tertiaryContainer = DarkGray,
    onTertiaryContainer = PureWhite,

    background = DeepBlack,
    onBackground = OffWhite,
    surface = DarkGray,
    onSurface = OffWhite,
    
    surfaceVariant = LightGray,
    onSurfaceVariant = OffWhite,
    
    error = SoftRed,
    errorContainer = DarkRed,
    onError = PureWhite,
    onErrorContainer = PureWhite
)

@Composable
fun SmartSplitTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
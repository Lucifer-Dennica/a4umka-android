package com.example.a4umka.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SovenokColorScheme = lightColorScheme(
    primary = Coral,
    onPrimary = Color.White,
    primaryContainer = CoralLight,
    onPrimaryContainer = DarkBrown,

    secondary = Mint,
    onSecondary = Color.White,
    secondaryContainer = MintLight,
    onSecondaryContainer = DarkBrown,

    tertiary = Lavender,
    onTertiary = Color.White,
    tertiaryContainer = LavenderLight,
    onTertiaryContainer = DarkBrown,

    background = Cream,
    onBackground = DarkBrown,

    surface = Color.White,
    onSurface = DarkBrown,

    error = ErrorRed,
    onError = Color.White
)

@Composable
fun SovenokTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SovenokColorScheme,
        content = content
    )
}

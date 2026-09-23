package br.com.rotacerta.colaborador.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val BrandNavy = Color(0xFF0F4C81)
val BrandNavyDark = Color(0xFF0A355B)
val BrandAmber = Color(0xFFFFC857)
val SuccessGreen = Color(0xFF1B8A5A)
val WarningOrange = Color(0xFFE08A00)
val DangerRed = Color(0xFFC62828)

private val LightColors = lightColorScheme(
    primary = BrandNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5E4F5),
    onPrimaryContainer = BrandNavyDark,
    secondary = BrandAmber,
    onSecondary = Color(0xFF3A2A00),
    secondaryContainer = Color(0xFFFFEFC7),
    onSecondaryContainer = Color(0xFF3A2A00),
    background = Color(0xFFF5F7FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFE9EEF4),
    onSurfaceVariant = Color(0xFF4A5563),
    outlineVariant = Color(0xFFD7DEE7),
    error = DangerRed,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9CC7F0),
    onPrimary = Color(0xFF00315A),
    primaryContainer = BrandNavy,
    onPrimaryContainer = Color(0xFFD5E4F5),
    secondary = BrandAmber,
    onSecondary = Color(0xFF3A2A00),
    background = Color(0xFF0F141A),
    surface = Color(0xFF161D25),
    surfaceVariant = Color(0xFF222B36),
    onSurfaceVariant = Color(0xFFB8C3D0),
    error = Color(0xFFFF8A80),
)

private val AppTypography = Typography().run {
    copy(
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, letterSpacing = 0.1.sp),
    )
}

@Composable
fun RotaCertaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}

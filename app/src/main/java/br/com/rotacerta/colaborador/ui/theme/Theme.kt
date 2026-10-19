package br.com.rotacerta.colaborador.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tokens visuais compartilhados com o painel web (Rota-certa-Frontend-Angular).
 * Cada valor vem do SCSS indicado — ao mudar lá, mude aqui (ver docs/DESIGN-TOKENS.md).
 */
object RcColors {
    // app-menu.component.scss — topbar
    val Topbar = Color(0xFF1E40AF)          // .topbar background / .menu-item.active
    val TopbarText = Color(0xFFDBEAFE)      // .topbar-menu button
    val TopbarSubtle = Color(0xFFBFDBFE)    // .user-role
    val Accent = Color(0xFF3B82F6)          // .logo-dot / .avatar / .route-item .line / .map-point.blue

    // app-menu.component.scss + dashboard.component.scss — layout escuro
    val Background = Color(0xFF0F172A)      // .layout-container / .content / .vehicle-card / .fake-map
    val Panel = Color(0xFF1E2937)           // .panel / .stat-card / .menu-item:hover / grid do mapa
    val Divider = Color(0xFF334155)         // .alert-item border
    val TextStrong = Color(0xFFF1F5F9)      // h1 / h2 dos cards
    val Text = Color(0xFFE2E8F0)            // .content / .panel
    val TextMenu = Color(0xFFCBD5E1)        // .menu-item
    val TextLabel = Color(0xFF94A3B8)       // .stat-card span / .progress-label
    val TextMuted = Color(0xFF64748B)       // .menu-title / .date / small

    // Badges e destaques (dashboard.component.scss)
    val SuccessBg = Color(0xFF166534)
    val Success = Color(0xFF4ADE80)
    val WarningBg = Color(0xFF854D0E)
    val Warning = Color(0xFFFACC15)
    // Sem equivalente no web: mesma escala Tailwind (red-900 / red-400) para "ausente"
    val DangerBg = Color(0xFF7F1D1D)
    val Danger = Color(0xFFF87171)

    // Tema Angular Material "indigo-pink" (botões color="primary" / "warn")
    val MatPrimary = Color(0xFF3F51B5)
    val MatWarn = Color(0xFFF44336)

    // Formulários: mat-card branco (employee-detail / list-card)
    val CardWhite = Color(0xFFFFFFFF)
    val CardTitle = Color(0xFF111827)
    val CardText = Color(0xFF374151)
    val White = Color(0xFFFFFFFF)
}

private val ColorScheme = darkColorScheme(
    primary = RcColors.MatPrimary,
    onPrimary = RcColors.White,
    primaryContainer = RcColors.Topbar,
    onPrimaryContainer = RcColors.White,
    secondary = RcColors.Accent,
    onSecondary = RcColors.White,
    secondaryContainer = RcColors.Topbar,
    onSecondaryContainer = RcColors.White,
    background = RcColors.Background,
    onBackground = RcColors.Text,
    surface = RcColors.Panel,
    onSurface = RcColors.Text,
    surfaceVariant = RcColors.Panel,
    onSurfaceVariant = RcColors.TextLabel,
    outline = RcColors.TextMuted,
    outlineVariant = RcColors.Divider,
    error = RcColors.MatWarn,
    onError = RcColors.White,
    // Snackbar no padrão do MatSnackBar (fundo #323232, texto branco)
    inverseSurface = Color(0xFF323232),
    inverseOnSurface = RcColors.White,
    inversePrimary = RcColors.Accent,
)

// Roboto (index.html carrega Roboto 300/400/500); no Android é a fonte padrão do sistema.
private val Roboto = FontFamily.Default

private val AppTypography = Typography(
    headlineSmall = TextStyle(fontFamily = Roboto, fontSize = 28.sp, fontWeight = FontWeight.SemiBold), // h1 1.75rem
    titleLarge = TextStyle(fontFamily = Roboto, fontSize = 24.sp, fontWeight = FontWeight.SemiBold),     // list-header h2
    titleMedium = TextStyle(fontFamily = Roboto, fontSize = 17.sp, fontWeight = FontWeight.SemiBold),    // panel h3 1.05rem
    titleSmall = TextStyle(fontFamily = Roboto, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),     // strong 0.95rem
    bodyLarge = TextStyle(fontFamily = Roboto, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = Roboto, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = Roboto, fontSize = 13.sp),                                        // small 0.8rem
    labelLarge = TextStyle(fontFamily = Roboto, fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = Roboto, fontSize = 12.sp, fontWeight = FontWeight.Medium),      // .badge 0.75rem
    labelSmall = TextStyle(fontFamily = Roboto, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp), // .menu-title
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),  // mat-button / mat-card (Material indigo-pink)
    small = RoundedCornerShape(6.dp),       // .topbar-menu button
    medium = RoundedCornerShape(8.dp),      // .menu-item
    large = RoundedCornerShape(12.dp),      // .panel / .stat-card
)

/** Tema único escuro, igual ao painel web (não segue o tema do sistema). */
@Composable
fun RotaCertaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColorScheme, typography = AppTypography, shapes = AppShapes, content = content)
}

package br.com.rotacerta.colaborador.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.rotacerta.colaborador.data.model.BoardingStatus
import br.com.rotacerta.colaborador.data.model.RouteStatus
import br.com.rotacerta.colaborador.ui.theme.RcColors

// ---------------------------------------------------------------- topbar (.topbar)

/** Barra superior igual à do painel: fundo #1e40af, ponto azul + "Rotacerta", usuário e avatar. */
@Composable
fun RcTopBar(
    userName: String? = null,
    userRole: String? = null,
    navigation: (@Composable () -> Unit)? = null,
    title: String? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .shadow(4.dp)
            .background(RcColors.Topbar)
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (navigation != null) {
            navigation()
            Spacer(Modifier.width(4.dp))
        }
        if (title != null) {
            Text(title, color = RcColors.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        } else {
            RcLogo(Modifier.weight(1f))
        }
        if (userName != null) {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 12.dp)) {
                Text(userName, color = RcColors.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                if (userRole != null) {
                    Text(userRole, color = RcColors.TopbarSubtle, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            RcAvatar(userName)
        }
    }
}

@Composable
fun RcLogo(modifier: Modifier = Modifier, fontSize: Int = 20) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(RcColors.Accent, CircleShape))
        Text(
            "Rotacerta",
            color = RcColors.White,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
fun RcAvatar(name: String, size: Int = 36) {
    Box(Modifier.size(size.dp).background(RcColors.Accent, CircleShape), contentAlignment = Alignment.Center) {
        Text(initials(name), color = RcColors.White, fontWeight = FontWeight.SemiBold, fontSize = (size * 0.4f).sp)
    }
}

fun initials(name: String) =
    name.split(' ').filter { it.isNotBlank() }.let { parts ->
        (parts.firstOrNull()?.take(1).orEmpty() + if (parts.size > 1) parts.last().take(1) else "").uppercase()
    }

// ---------------------------------------------------------------- dashboard (.page-header / .panel / .stat-card)

@Composable
fun PageHeader(title: String, subtitle: String?, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(title, color = RcColors.TextStrong, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
        if (subtitle != null) Text(subtitle, color = RcColors.TextMuted, fontSize = 15.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

/** Equivalente ao `.panel` (fundo #1e2937, raio 12, título h3 + legenda à direita). */
@Composable
fun Panel(
    modifier: Modifier = Modifier,
    title: String? = null,
    caption: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier.fillMaxWidth().background(RcColors.Panel, RoundedCornerShape(12.dp)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (title != null) {
            Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = RcColors.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                if (caption != null) Text(caption, color = RcColors.TextMuted, fontSize = 13.sp)
            }
        }
        content()
    }
}

/** Equivalente ao `.stat-card`: rótulo em minúsculas, valor grande e nota. */
@Composable
fun StatCard(label: String, value: String, note: String?, modifier: Modifier = Modifier, valueColor: Color = RcColors.TextStrong, noteColor: Color = RcColors.TextMuted) {
    Column(modifier.background(RcColors.Panel, RoundedCornerShape(12.dp)).padding(horizontal = 18.dp, vertical = 16.dp)) {
        Text(label, color = RcColors.TextLabel, fontSize = 13.sp, modifier = Modifier.padding(bottom = 6.dp))
        Text(value, color = valueColor, fontSize = 26.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (note != null) Text(note, color = noteColor, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp), maxLines = 2)
    }
}

/** Equivalente ao `.vehicle-card` (cartão interno #0f172a, raio 10). */
@Composable
fun InnerCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth().background(RcColors.Background, RoundedCornerShape(10.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

@Composable
fun ProgressRow(label: String, progress: Float, color: Color = RcColors.Accent) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = RcColors.TextLabel, fontSize = 14.sp)
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = color,
            trackColor = color.copy(alpha = 0.25f),
        )
    }
}

/** `.menu-title`: 11sp, caixa alta, #64748b. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), color = RcColors.TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp, modifier = modifier)
}

// ---------------------------------------------------------------- badges (.badge.success / .warning)

enum class BadgeKind(val bg: Color, val fg: Color) {
    SUCCESS(RcColors.SuccessBg, RcColors.Success),
    WARNING(RcColors.WarningBg, RcColors.Warning),
    DANGER(RcColors.DangerBg, RcColors.Danger),
    INFO(RcColors.Topbar, Color(0xFFDBEAFE)),
}

@Composable
fun Badge(text: String, kind: BadgeKind, modifier: Modifier = Modifier) {
    Text(
        text,
        color = kind.fg,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier.background(kind.bg, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

data class StatusStyle(val label: String, val kind: BadgeKind)

// Textos em minúsculas, como no painel ("em rota", "atrasado").
fun RouteStatus.style(): StatusStyle = when (this) {
    RouteStatus.WAITING -> StatusStyle("aguardando saída", BadgeKind.INFO)
    RouteStatus.IN_ROUTE -> StatusStyle("em rota", BadgeKind.SUCCESS)
    RouteStatus.DELAYED -> StatusStyle("atrasado", BadgeKind.WARNING)
}

/** Rótulo curto para o card de estatística ("presença hoje"). */
fun BoardingStatus.shortLabel(): String = when (this) {
    BoardingStatus.PENDING -> "pendente"
    BoardingStatus.CONFIRMED -> "sim"
    BoardingStatus.ABSENT -> "não"
}

fun BoardingStatus.style(): StatusStyle = when (this) {
    BoardingStatus.PENDING -> StatusStyle("não confirmado", BadgeKind.WARNING)
    BoardingStatus.CONFIRMED -> StatusStyle("confirmado", BadgeKind.SUCCESS)
    BoardingStatus.ABSENT -> StatusStyle("ausente", BadgeKind.DANGER)
}

fun eventLabel(type: String): String = when (type) {
    "departure" -> "Saída"
    "arrival" -> "Chegada"
    "stop" -> "Parada"
    "delay" -> "Atraso"
    "incident" -> "Ocorrência"
    else -> type.replaceFirstChar { it.uppercase() }
}

// ---------------------------------------------------------------- listas (.alert-item / linhas de info)

/** `.alert-item`: ícone, título em destaque, legenda #64748b e divisória #334155. */
@Composable
fun AlertItem(icon: ImageVector, tint: Color, title: String, caption: String?, body: String? = null, showDivider: Boolean = true) {
    Column {
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.padding(top = 2.dp).size(24.dp))
            Column(Modifier.padding(start = 12.dp)) {
                Text(title, color = RcColors.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                if (body != null) Text(body, color = RcColors.TextMenu, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
                if (caption != null) Text(caption, color = RcColors.TextMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
        if (showDivider) HorizontalDivider(color = RcColors.Divider, thickness = 1.dp)
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = RcColors.Accent, modifier = Modifier.size(22.dp))
        Column(Modifier.padding(start = 14.dp).weight(1f)) {
            Text(label, color = RcColors.TextLabel, fontSize = 13.sp)
            Text(value, color = RcColors.Text, fontSize = 16.sp)
        }
        trailing()
    }
}

// ---------------------------------------------------------------- botões (mat-raised-button / mat-stroked-button)

@Composable
fun RcButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    color: Color = RcColors.MatPrimary,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.heightIn(min = 44.dp),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = RcColors.White,
            disabledContainerColor = color.copy(alpha = 0.35f),
            disabledContentColor = RcColors.White.copy(alpha = 0.7f),
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = RcColors.White)
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun RcStrokedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    color: Color = RcColors.Accent,
    borderColor: Color = RcColors.Divider,
    containerColor: Color = Color.Transparent,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 44.dp),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor, contentColor = color),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ---------------------------------------------------------------- formulários (mat-card branco)

/** Equivalente ao `mat-card` dos formulários do painel (fundo branco, título #111827). */
@Composable
fun FormCard(title: String, modifier: Modifier = Modifier, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        color = RcColors.CardWhite,
        shadowElevation = 2.dp,
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, color = RcColors.CardTitle, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) Text(subtitle, color = RcColors.CardText, fontSize = 14.sp)
            content()
        }
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    placeholder: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        trailingIcon = trailing,
        singleLine = true,
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboardType),
        shape = RoundedCornerShape(4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = RcColors.CardTitle,
            unfocusedTextColor = RcColors.CardTitle,
            focusedBorderColor = RcColors.MatPrimary,
            unfocusedBorderColor = Color(0xFFBDBDBD),
            focusedLabelColor = RcColors.MatPrimary,
            unfocusedLabelColor = RcColors.CardText,
            cursorColor = RcColors.MatPrimary,
            focusedLeadingIconColor = RcColors.MatPrimary,
            unfocusedLeadingIconColor = RcColors.CardText,
            focusedTrailingIconColor = RcColors.CardText,
            unfocusedTrailingIconColor = RcColors.CardText,
            focusedPlaceholderColor = Color(0xFF9CA3AF),
            unfocusedPlaceholderColor = Color(0xFF9CA3AF),
            focusedContainerColor = RcColors.CardWhite,
            unfocusedContainerColor = RcColors.CardWhite,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

// ---------------------------------------------------------------- estados

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = RcColors.Accent) }
}

@Composable
fun MessageState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.CloudOff,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(56.dp), tint = RcColors.TextMuted)
        Text(title, color = RcColors.TextStrong, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp), textAlign = TextAlign.Center)
        Text(message, color = RcColors.TextMenu, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
        if (actionLabel != null && onAction != null) {
            RcButton(actionLabel, onAction, modifier = Modifier.padding(top = 20.dp))
        }
    }
}

/** Item clicável no estilo `.menu-item` (usado na navegação inferior). */
@Composable
fun MenuItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) RcColors.Topbar else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) RcColors.White else RcColors.TextMenu, modifier = Modifier.size(22.dp))
        Text(label, color = if (selected) RcColors.White else RcColors.TextMenu, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
    }
}


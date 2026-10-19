package br.com.rotacerta.colaborador.ui.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import br.com.rotacerta.colaborador.data.model.TripEvent
import br.com.rotacerta.colaborador.ui.components.AlertItem
import br.com.rotacerta.colaborador.ui.components.LoadingState
import br.com.rotacerta.colaborador.ui.components.MessageState
import br.com.rotacerta.colaborador.ui.components.PageHeader
import br.com.rotacerta.colaborador.ui.components.Panel
import br.com.rotacerta.colaborador.ui.components.eventLabel
import br.com.rotacerta.colaborador.ui.theme.RcColors
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AlertsScreen(state: TripUiState) {
    val trip = state.selectedTrip
    when {
        trip == null && state.loading -> LoadingState()
        trip == null || trip.events.isEmpty() -> MessageState(
            "Nenhum alerta",
            "Atrasos, saídas e ocorrências da sua rota aparecem aqui.",
            icon = Icons.Filled.Notifications,
        )
        else -> Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PageHeader("Alertas", trip.route.name)
            Panel(title = "Alertas recentes", caption = "${trip.events.size} eventos") {
                Column {
                    trip.events.forEachIndexed { i, ev ->
                        val (icon, tint) = eventVisual(ev.eventType)
                        AlertItem(
                            icon = icon,
                            tint = tint,
                            title = eventLabel(ev.eventType),
                            body = ev.description.takeIf { it.isNotBlank() },
                            caption = relativeTime(ev),
                            showDivider = i < trip.events.lastIndex,
                        )
                    }
                }
            }
        }
    }
}

// Cores dos ícones como no painel: warning = warn (vermelho), check_circle = #4ade80.
private fun eventVisual(type: String): Pair<ImageVector, Color> = when (type) {
    "departure" -> Icons.Filled.DirectionsBus to RcColors.Accent
    "arrival" -> Icons.Filled.CheckCircle to RcColors.Success
    "stop" -> Icons.Filled.PauseCircle to RcColors.TextLabel
    "delay" -> Icons.Filled.Schedule to RcColors.Warning
    "incident" -> Icons.Filled.Warning to RcColors.MatWarn
    else -> Icons.Filled.Notifications to RcColors.TextLabel
}

/** "há 5 min" / "ontem · 18:15", como nos alertas do dashboard web. */
private fun relativeTime(event: TripEvent): String = runCatching {
    val at = OffsetDateTime.parse(event.timestamp).atZoneSameInstant(ZoneId.systemDefault())
    val now = OffsetDateTime.now().atZoneSameInstant(ZoneId.systemDefault())
    val minutes = Duration.between(at, now).toMinutes()
    val time = at.format(DateTimeFormatter.ofPattern("HH:mm"))
    when {
        minutes < 1 -> "agora"
        minutes < 60 -> "há $minutes min"
        at.toLocalDate() == now.toLocalDate() -> "hoje · $time"
        at.toLocalDate() == now.toLocalDate().minusDays(1) -> "ontem · $time"
        else -> at.format(DateTimeFormatter.ofPattern("dd/MM · HH:mm"))
    }
}.getOrDefault(event.timestamp)

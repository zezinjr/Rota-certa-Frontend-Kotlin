package br.com.rotacerta.colaborador.ui.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import br.com.rotacerta.colaborador.data.model.TripEvent
import br.com.rotacerta.colaborador.ui.components.LoadingState
import br.com.rotacerta.colaborador.ui.components.MessageState
import br.com.rotacerta.colaborador.ui.components.SectionCard
import br.com.rotacerta.colaborador.ui.components.eventLabel
import br.com.rotacerta.colaborador.ui.theme.BrandNavy
import br.com.rotacerta.colaborador.ui.theme.DangerRed
import br.com.rotacerta.colaborador.ui.theme.SuccessGreen
import br.com.rotacerta.colaborador.ui.theme.WarningOrange
import androidx.compose.ui.graphics.Color
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AlertsScreen(state: TripUiState) {
    val trip = state.selectedTrip
    when {
        trip == null && state.loading -> LoadingState()
        trip == null || trip.events.isEmpty() -> MessageState(
            "Nenhum aviso",
            "Atrasos, saídas e ocorrências da sua rota aparecem aqui.",
            icon = Icons.Outlined.NotificationsNone,
        )
        else -> LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text("Avisos da ${trip.route.name}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
            }
            items(trip.events, key = { it.id }) { EventCard(it) }
        }
    }
}

@Composable
private fun EventCard(event: TripEvent) {
    val (icon, color) = eventVisual(event.eventType)
    SectionCard {
        Row(verticalAlignment = Alignment.Top) {
            Box(Modifier.size(40.dp).background(color.copy(alpha = 0.14f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Column(Modifier.padding(start = 12.dp)) {
                Text(eventLabel(event.eventType), style = MaterialTheme.typography.titleSmall, color = color)
                if (event.description.isNotBlank()) Text(event.description, style = MaterialTheme.typography.bodyMedium)
                Text(
                    formatTimestamp(event.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

private fun eventVisual(type: String): Pair<ImageVector, Color> = when (type) {
    "departure" -> Icons.Outlined.DirectionsBus to SuccessGreen
    "arrival" -> Icons.Outlined.CheckCircle to BrandNavy
    "stop" -> Icons.Outlined.PauseCircle to BrandNavy
    "delay" -> Icons.Outlined.Schedule to WarningOrange
    "incident" -> Icons.Outlined.WarningAmber to DangerRed
    else -> Icons.Outlined.NotificationsNone to BrandNavy
}

private fun formatTimestamp(iso: String): String = runCatching {
    OffsetDateTime.parse(iso).atZoneSameInstant(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm"))
}.getOrDefault(iso)

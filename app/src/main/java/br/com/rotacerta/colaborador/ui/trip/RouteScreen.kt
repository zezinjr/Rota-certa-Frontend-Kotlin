package br.com.rotacerta.colaborador.ui.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.rotacerta.colaborador.data.model.Trip
import br.com.rotacerta.colaborador.platform.PlatformActions
import br.com.rotacerta.colaborador.ui.components.LoadingState
import br.com.rotacerta.colaborador.ui.components.MessageState
import br.com.rotacerta.colaborador.ui.components.SectionCard
import br.com.rotacerta.colaborador.ui.components.StatusPill
import br.com.rotacerta.colaborador.ui.components.style
import br.com.rotacerta.colaborador.ui.theme.BrandAmber
import br.com.rotacerta.colaborador.ui.theme.BrandNavy
import br.com.rotacerta.colaborador.ui.theme.SuccessGreen

@Composable
fun RouteScreen(state: TripUiState, actions: PlatformActions) {
    val trip = state.selectedTrip
    when {
        trip != null -> RouteContent(trip, actions)
        state.loading -> LoadingState()
        else -> MessageState("Sem rota", "Você ainda não está vinculado a uma rota.")
    }
}

@Composable
private fun RouteContent(trip: Trip, actions: PlatformActions) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column {
            Text(trip.route.name, style = MaterialTheme.typography.titleLarge)
            val s = trip.route.status.style()
            StatusPill(s.label, s.color, Modifier.padding(top = 6.dp))
        }

        SectionCard {
            Box(Modifier.clip(RoundedCornerShape(12.dp))) { RouteMap(trip) }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Legend(BrandAmber, "Seu ponto")
                Legend(Color.White, "Paradas")
                Legend(SuccessGreen, "Van (última posição)")
            }
        }

        SectionCard(title = "Paradas") {
            trip.stops.forEachIndexed { index, stop ->
                TimelineItem(
                    title = stop.title,
                    subtitle = stop.subtitle,
                    badge = "${stop.sequence + 1}",
                    highlighted = stop.isMyStop,
                    isFirst = index == 0,
                    isLast = false,
                )
            }
            trip.destination?.let {
                TimelineItem(
                    title = trip.company.name,
                    subtitle = it.label,
                    badge = null,
                    highlighted = false,
                    isFirst = trip.stops.isEmpty(),
                    isLast = true,
                )
            }
        }

        trip.destination?.let { dest ->
            OutlinedButton(onClick = { actions.openMap(dest.latitude, dest.longitude, trip.company.name) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("  Abrir destino no mapa")
            }
        }
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).background(BrandNavy, CircleShape).padding(2.dp).background(color, CircleShape))
        Text(" $label", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TimelineItem(
    title: String,
    subtitle: String?,
    badge: String?,
    highlighted: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(Modifier.width(36.dp).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
            // Linha vertical da timeline
            Column(Modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.width(3.dp).height(10.dp).background(if (isFirst) Color.Transparent else BrandNavy.copy(alpha = 0.3f)))
                Box(Modifier.width(3.dp).weight(1f).background(if (isLast) Color.Transparent else BrandNavy.copy(alpha = 0.3f)))
            }
            Surface(
                modifier = Modifier.padding(top = 4.dp).size(28.dp),
                shape = if (badge == null) RoundedCornerShape(8.dp) else CircleShape,
                color = when {
                    badge == null -> BrandNavy
                    highlighted -> BrandAmber
                    else -> MaterialTheme.colorScheme.surface
                },
                border = androidx.compose.foundation.BorderStroke(2.dp, BrandNavy),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (badge != null) {
                        Text(badge, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BrandNavy)
                    } else {
                        Icon(Icons.Outlined.Business, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        Column(Modifier.padding(start = 12.dp, bottom = 16.dp, top = 6.dp).weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f, fill = false))
                if (highlighted) {
                    Surface(color = BrandAmber, shape = RoundedCornerShape(50), modifier = Modifier.padding(start = 8.dp)) {
                        Text("Seu ponto", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
            }
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

package br.com.rotacerta.colaborador.ui.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.rotacerta.colaborador.data.model.Trip
import br.com.rotacerta.colaborador.ui.components.Badge
import br.com.rotacerta.colaborador.ui.components.BadgeKind
import br.com.rotacerta.colaborador.ui.components.LoadingState
import br.com.rotacerta.colaborador.ui.components.MessageState
import br.com.rotacerta.colaborador.ui.components.PageHeader
import br.com.rotacerta.colaborador.ui.components.Panel
import br.com.rotacerta.colaborador.ui.components.RcStrokedButton
import br.com.rotacerta.colaborador.ui.components.style
import br.com.rotacerta.colaborador.ui.theme.RcColors

@Composable
fun RouteScreen(state: TripUiState, onOpenMap: () -> Unit) {
    val trip = state.selectedTrip
    when {
        trip != null -> RouteContent(trip, onOpenMap)
        state.loading -> LoadingState()
        else -> MessageState("Sem rota", "Você ainda não está vinculado a uma rota.")
    }
}

@Composable
private fun RouteContent(trip: Trip, onOpenMap: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PageHeader(trip.route.name, "${trip.stops.size} paradas · destino ${trip.company.name}")

        Panel(title = "Mapa da rota", caption = trip.route.status.style().label) {
            Box(Modifier.clip(RoundedCornerShape(10.dp))) {
                LocalTripMap.current(TripMapRequest(trip, Modifier.fillMaxWidth().height(240.dp), interactive = false))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Legend(RcColors.Warning, "seu ponto")
                Legend(RcColors.Accent, "paradas")
                Legend(RcColors.Success, "van")
            }
            RcStrokedButton("Abrir mapa", onOpenMap, Modifier.fillMaxWidth(), icon = Icons.Filled.Map)
        }

        Panel(title = "Paradas", caption = "ordem de embarque") {
            Column {
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
        }

    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(14.dp).border(3.dp, Color.White, CircleShape).padding(3.dp).background(color, CircleShape))
        Text(" $label", color = RcColors.TextLabel, fontSize = 13.sp)
    }
}

/** Linha vertical #3b82f6 de 3px, como `.route-item .line` do dashboard. */
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
        Box(Modifier.width(32.dp).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.width(3.dp).height(8.dp).background(if (isFirst) Color.Transparent else RcColors.Accent))
                Box(Modifier.width(3.dp).weight(1f).background(if (isLast) Color.Transparent else RcColors.Accent))
            }
            Box(
                Modifier
                    .padding(top = 4.dp)
                    .size(26.dp)
                    .background(Color.White, if (badge == null) RoundedCornerShape(6.dp) else CircleShape)
                    .padding(3.dp)
                    .background(
                        when {
                            badge == null -> RcColors.Topbar
                            highlighted -> RcColors.Warning
                            else -> RcColors.Accent
                        },
                        if (badge == null) RoundedCornerShape(4.dp) else CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (badge != null) {
                    Text(badge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (highlighted) RcColors.Background else Color.White)
                } else {
                    Icon(Icons.Filled.Business, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                }
            }
        }
        Column(Modifier.padding(start = 12.dp, bottom = 18.dp, top = 6.dp).weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = RcColors.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f, fill = false))
                if (highlighted) Badge("seu ponto", BadgeKind.WARNING, Modifier.padding(start = 8.dp))
            }
            subtitle?.let { Text(it, color = RcColors.TextMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp)) }
        }
    }
}

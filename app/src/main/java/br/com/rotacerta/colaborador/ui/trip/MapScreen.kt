package br.com.rotacerta.colaborador.ui.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.rotacerta.colaborador.data.model.LiveStatus
import br.com.rotacerta.colaborador.data.model.Trip
import br.com.rotacerta.colaborador.platform.PlatformActions
import br.com.rotacerta.colaborador.ui.components.Badge
import br.com.rotacerta.colaborador.ui.components.LoadingState
import br.com.rotacerta.colaborador.ui.components.MessageState
import br.com.rotacerta.colaborador.ui.components.RcButton
import br.com.rotacerta.colaborador.ui.components.style
import br.com.rotacerta.colaborador.ui.theme.RcColors

/** Mapa em tela cheia dentro do app (Google Maps quando configurado). */
@Composable
fun MapScreen(state: TripUiState, actions: PlatformActions, focus: MapFocus, onBack: () -> Unit) {
    val trip = state.selectedTrip
    when {
        trip != null -> MapContent(trip, state.selectedLive, actions, focus, onBack)
        state.loading -> LoadingState()
        else -> MessageState("Sem rota", "Você ainda não está vinculado a uma rota.", actionLabel = "Voltar", onAction = onBack)
    }
}

@Composable
private fun MapContent(trip: Trip, live: LiveStatus?, actions: PlatformActions, focus: MapFocus, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(RcColors.Background)) {
        LocalTripMap.current(TripMapRequest(trip, Modifier.fillMaxSize(), interactive = true, focus = focus, live = live))

        // Topo: voltar + nome/status da rota
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FilledIconButton(
                onClick = onBack,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = RcColors.Topbar, contentColor = RcColors.White),
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
            Row(
                Modifier
                    .weight(1f)
                    .background(RcColors.Panel.copy(alpha = 0.95f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    trip.route.name,
                    color = RcColors.Text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                val s = trip.route.status.style()
                Badge(s.label, s.kind, Modifier.padding(start = 8.dp))
            }
        }

        // Rodapé: meu ponto + rota a pé no app do Google Maps
        trip.myStop?.let { stop ->
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(RcColors.Panel.copy(alpha = 0.97f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                live?.let { liveHeadline(trip, it) }?.let { h ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LiveDot()
                        Column(Modifier.padding(start = 10.dp).weight(1f)) {
                            Text(h.title, color = RcColors.TextStrong, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            h.detail?.let { Text(it, color = RcColors.TextLabel, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        }
                        h.highlight?.let { Text(it, color = h.accent, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
                    }
                    HorizontalDivider(color = RcColors.Divider)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(28.dp).background(Color.White, CircleShape).padding(3.dp).background(RcColors.Warning, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Text("${stop.sequence + 1}", color = RcColors.Background, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Text("Seu ponto de embarque", color = RcColors.TextLabel, fontSize = 12.sp)
                        Text(stop.title, color = RcColors.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        stop.subtitle?.let { Text(it, color = RcColors.TextMuted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    MapLegend(RcColors.Warning, "seu ponto")
                    MapLegend(RcColors.Accent, "paradas")
                    MapLegend(RcColors.Success, "van")
                }
                RcButton(
                    "Rota a pé até o ponto",
                    { actions.openMap(stop.latitude, stop.longitude, stop.title) },
                    Modifier.fillMaxWidth(),
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                )
            }
        }
    }
}

@Composable
private fun MapLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).background(Color.White, CircleShape).padding(2.dp).background(color, CircleShape))
        Text(" $label", color = RcColors.TextLabel, fontSize = 12.sp)
    }
}

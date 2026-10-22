package br.com.rotacerta.colaborador.ui.trip

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.rotacerta.colaborador.data.model.LivePhase
import br.com.rotacerta.colaborador.data.model.LiveStatus
import br.com.rotacerta.colaborador.data.model.Trip
import br.com.rotacerta.colaborador.ui.components.RcButton
import br.com.rotacerta.colaborador.ui.theme.RcColors
import java.util.Locale

/** Textos da situação ao vivo, usados no card da Viagem e no mapa em tela cheia. */
data class LiveHeadline(val title: String, val highlight: String?, val detail: String?, val accent: Color)

fun liveHeadline(trip: Trip, live: LiveStatus): LiveHeadline? {
    if (!live.isLive) return null
    val eta = live.eta
    return when (live.phase) {
        LivePhase.APPROACHING -> LiveHeadline(
            "Sua van está a caminho",
            eta?.let { "~${it.minutes} min" },
            eta?.let { "${stopsBeforeText(it.stopsBefore)} · ${distanceText(it.distanceMeters)}" },
            RcColors.Success,
        )
        LivePhase.ARRIVING -> LiveHeadline(
            "Sua van está chegando!",
            eta?.let { if (it.minutes <= 1) "agora" else "~${it.minutes} min" },
            trip.myStop?.let { "Vá para o ponto: ${it.title}" },
            RcColors.Warning,
        )
        LivePhase.PASSED -> LiveHeadline(
            "A van já passou pelo seu ponto",
            null,
            "Ela segue para ${trip.company.name}.",
            RcColors.TextLabel,
        )
        LivePhase.AT_DESTINATION -> LiveHeadline("A van chegou à empresa", null, trip.company.name, RcColors.Accent)
        LivePhase.WAITING -> null
    }
}

fun stopsBeforeText(stops: Int): String = when (stops) {
    0 -> "você é a próxima parada"
    1 -> "1 parada antes de você"
    else -> "$stops paradas antes de você"
}

fun distanceText(meters: Int): String =
    if (meters < 1000) "$meters m" else String.format(Locale.forLanguageTag("pt-BR"), "%.1f km", meters / 1000.0)

/** Card "estilo Uber": van ao vivo, previsão de chegada e atalho para o mapa. */
@Composable
fun LiveTripCard(trip: Trip, live: LiveStatus, onOpenMap: () -> Unit, modifier: Modifier = Modifier) {
    val headline = liveHeadline(trip, live) ?: return
    Column(
        modifier
            .fillMaxWidth()
            .background(RcColors.Panel, RoundedCornerShape(12.dp))
            .border(1.dp, headline.accent.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LiveDot()
            Text("  AO VIVO", color = RcColors.Success, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            trip.vehicle?.let {
                Text("  ·  ${it.plateNumber}", color = RcColors.TextMuted, fontSize = 12.sp)
            }
        }
        Text(
            headline.title,
            color = RcColors.TextStrong,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 10.dp),
        )
        headline.highlight?.let {
            Text(it, color = headline.accent, fontSize = 34.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
        }
        headline.detail?.let {
            Text(it, color = RcColors.TextLabel, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
        }
        RcButton(
            "Acompanhar no mapa",
            onOpenMap,
            Modifier.fillMaxWidth().padding(top = 14.dp),
            icon = Icons.Filled.Map,
        )
    }
}

/** Ponto verde pulsando, como o indicador "ao vivo" de apps de mobilidade. */
@Composable
fun LiveDot() {
    val pulse by rememberInfiniteTransition(label = "live").animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "live-alpha",
    )
    Box(Modifier.size(10.dp).alpha(pulse).background(RcColors.Success, CircleShape))
}

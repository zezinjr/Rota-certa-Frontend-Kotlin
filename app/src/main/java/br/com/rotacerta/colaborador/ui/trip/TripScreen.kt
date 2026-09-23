package br.com.rotacerta.colaborador.ui.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.rotacerta.colaborador.data.formatCountdown
import br.com.rotacerta.colaborador.data.model.BoardingStatus
import br.com.rotacerta.colaborador.data.model.Trip
import br.com.rotacerta.colaborador.data.nextDeparture
import br.com.rotacerta.colaborador.data.weekdayLabel
import br.com.rotacerta.colaborador.platform.PlatformActions
import br.com.rotacerta.colaborador.ui.components.InfoRow
import br.com.rotacerta.colaborador.ui.components.LoadingState
import br.com.rotacerta.colaborador.ui.components.MessageState
import br.com.rotacerta.colaborador.ui.components.SectionCard
import br.com.rotacerta.colaborador.ui.components.StatusPill
import br.com.rotacerta.colaborador.ui.components.style
import br.com.rotacerta.colaborador.ui.theme.BrandNavy
import br.com.rotacerta.colaborador.ui.theme.BrandNavyDark
import br.com.rotacerta.colaborador.ui.theme.DangerRed
import br.com.rotacerta.colaborador.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TripScreen(
    state: TripUiState,
    actions: PlatformActions,
    onRefresh: () -> Unit,
    onSelectTrip: (Long) -> Unit,
    onBoarding: (BoardingStatus) -> Unit,
    onOpenRoute: () -> Unit,
) {
    when {
        state.loading && state.trips.isEmpty() -> LoadingState()
        state.error != null -> MessageState("Não foi possível carregar", state.error, actionLabel = "Tentar novamente", onAction = onRefresh)
        state.trips.isEmpty() -> MessageState(
            "Você ainda não está em nenhuma rota",
            "Fale com o RH ou com a gestão de transporte da sua empresa para ser incluído em uma rota.",
            icon = Icons.Outlined.EventBusy,
            actionLabel = "Atualizar",
            onAction = onRefresh,
        )
        else -> TripContent(state, state.selectedTrip!!, actions, onRefresh, onSelectTrip, onBoarding, onOpenRoute)
    }
}

@Composable
private fun TripContent(
    state: TripUiState,
    trip: Trip,
    actions: PlatformActions,
    onRefresh: () -> Unit,
    onSelectTrip: (Long) -> Unit,
    onBoarding: (BoardingStatus) -> Unit,
    onOpenRoute: () -> Unit,
) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) { while (true) { delay(30_000); now = LocalDateTime.now() } }
    val departure = nextDeparture(trip.schedules, now)
    val firstName = state.profile?.name?.substringBefore(' ')

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Cabeçalho com status da rota e próxima saída
        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(BrandNavy, BrandNavyDark)))
                .padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (firstName != null) "Olá, $firstName" else "Minha viagem",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                    )
                    Text(trip.company.name, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Atualizar", tint = Color.White)
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("PRÓXIMA SAÍDA", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
            if (departure != null) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        departure.at.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "  ${dayLabel(departure.at, now)} · ${formatCountdown(departure.until)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
            } else {
                Text("Sem horários cadastrados", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            Spacer(Modifier.height(10.dp))
            val routeStyle = trip.route.status.style()
            Box(Modifier.background(Color.White, RoundedCornerShape(50))) {
                StatusPill(routeStyle.label, routeStyle.color)
            }
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (state.trips.size > 1) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.trips.forEach { t ->
                        FilterChip(
                            selected = t.id == trip.id,
                            onClick = { onSelectTrip(t.id) },
                            label = { Text(t.route.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        )
                    }
                }
            }

            BoardingCard(trip, state.updatingBoarding, onBoarding)

            SectionCard(title = "Meu ponto de embarque") {
                val stop = trip.myStop
                if (stop == null) {
                    Text("Nenhum ponto definido para você nesta rota.")
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(44.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.Place, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Column(Modifier.padding(start = 12.dp).weight(1f)) {
                            Text(stop.title, style = MaterialTheme.typography.titleMedium)
                            stop.subtitle?.let {
                                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                "Parada ${stop.sequence + 1} de ${trip.stops.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { actions.openMap(stop.latitude, stop.longitude, stop.title) }) {
                            Icon(Icons.Outlined.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("  Como chegar")
                        }
                        TextButton(onClick = onOpenRoute) { Text("Ver rota", maxLines = 1) }
                    }
                }
            }

            SectionCard(title = "Veículo e motorista") {
                trip.vehicle?.let {
                    InfoRow(Icons.Outlined.DirectionsBus, it.model, "Placa ${it.plateNumber}" + (it.capacity?.let { c -> " · $c lugares" } ?: ""))
                }
                trip.driver?.let { driver ->
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InfoRow(Icons.Outlined.Person, "Motorista", driver.name, Modifier.weight(1f))
                        FilledTonalIconButton(onClick = { actions.dial(driver.phone) }) {
                            Icon(Icons.Outlined.Call, contentDescription = "Ligar para o motorista")
                        }
                    }
                }
                if (trip.vehicle == null && trip.driver == null) Text("Veículo ainda não definido.")
            }

            if (trip.schedules.isNotEmpty()) {
                SectionCard(title = "Horários da rota") {
                    trip.schedules.forEach { s ->
                        InfoRow(Icons.Outlined.AccessTime, s.weekdays.joinToString(" · ") { weekdayLabel(it) }, s.time)
                    }
                }
            }

            state.lastUpdated?.let {
                Text(
                    "Atualizado às ${it.format(DateTimeFormatter.ofPattern("HH:mm:ss"))} · atualiza automaticamente",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun BoardingCard(trip: Trip, updating: Boolean, onBoarding: (BoardingStatus) -> Unit) {
    val style = trip.boardingStatus.style()
    SectionCard(title = "Você vai embarcar hoje?") {
        StatusPill(style.label, style.color)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val confirmed = trip.boardingStatus == BoardingStatus.CONFIRMED
            val absent = trip.boardingStatus == BoardingStatus.ABSENT
            Button(
                onClick = { onBoarding(BoardingStatus.CONFIRMED) },
                enabled = !updating,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                colors = if (confirmed) ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                else ButtonDefaults.buttonColors(),
            ) {
                Icon(Icons.Outlined.Done, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Sim, vou", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(
                onClick = { onBoarding(BoardingStatus.ABSENT) },
                enabled = !updating,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                colors = if (absent) ButtonDefaults.outlinedButtonColors(containerColor = DangerRed.copy(alpha = 0.1f), contentColor = DangerRed)
                else ButtonDefaults.outlinedButtonColors(),
            ) {
                Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Não vou", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (trip.boardingStatus != BoardingStatus.PENDING) {
            TextButton(onClick = { onBoarding(BoardingStatus.PENDING) }, enabled = !updating) { Text("Desfazer resposta") }
        }
    }
}

private fun dayLabel(at: LocalDateTime, now: LocalDateTime): String = when (at.toLocalDate()) {
    now.toLocalDate() -> "hoje"
    now.toLocalDate().plusDays(1) -> "amanhã"
    else -> at.format(DateTimeFormatter.ofPattern("dd/MM"))
}

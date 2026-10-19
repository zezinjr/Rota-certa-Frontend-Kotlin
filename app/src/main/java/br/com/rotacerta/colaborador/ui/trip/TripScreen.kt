package br.com.rotacerta.colaborador.ui.trip

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.rotacerta.colaborador.data.formatCountdown
import br.com.rotacerta.colaborador.data.model.BoardingStatus
import br.com.rotacerta.colaborador.data.model.Trip
import br.com.rotacerta.colaborador.data.nextDeparture
import br.com.rotacerta.colaborador.data.weekdayLabel
import br.com.rotacerta.colaborador.platform.PlatformActions
import br.com.rotacerta.colaborador.ui.components.Badge
import br.com.rotacerta.colaborador.ui.components.BadgeKind
import br.com.rotacerta.colaborador.ui.components.InfoRow
import br.com.rotacerta.colaborador.ui.components.InnerCard
import br.com.rotacerta.colaborador.ui.components.LoadingState
import br.com.rotacerta.colaborador.ui.components.MenuItem
import br.com.rotacerta.colaborador.ui.components.MessageState
import br.com.rotacerta.colaborador.ui.components.PageHeader
import br.com.rotacerta.colaborador.ui.components.Panel
import br.com.rotacerta.colaborador.ui.components.ProgressRow
import br.com.rotacerta.colaborador.ui.components.RcButton
import br.com.rotacerta.colaborador.ui.components.RcStrokedButton
import br.com.rotacerta.colaborador.ui.components.StatCard
import br.com.rotacerta.colaborador.ui.components.shortLabel
import br.com.rotacerta.colaborador.ui.components.style
import br.com.rotacerta.colaborador.ui.theme.RcColors
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

private val PT_BR: Locale = Locale.forLanguageTag("pt-BR")

/** Mesmo formato do cabeçalho do dashboard web: "Quinta-feira, 08 de maio de 2025 · 07:43". */
fun headerDate(now: LocalDateTime): String =
    now.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy '·' HH:mm", PT_BR))
        .replaceFirstChar { it.uppercase() }

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
            icon = Icons.Filled.EventBusy,
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
    val routeStyle = trip.route.status.style()
    val boardingStyle = trip.boardingStatus.style()
    val stop = trip.myStop

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            PageHeader("Minha viagem", headerDate(now), Modifier.weight(1f))
            IconButton(onClick = onRefresh) { Icon(Icons.Filled.Refresh, contentDescription = "Atualizar", tint = RcColors.TextLabel) }
        }

        if (state.trips.size > 1) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.trips.forEach { t ->
                    MenuItem(Icons.Filled.Route, t.route.name, t.id == trip.id, { onSelectTrip(t.id) }, Modifier.padding(horizontal = 4.dp))
                }
            }
        }

        // .stats-grid (2 colunas no celular)
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                "próxima saída",
                departure?.at?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "--:--",
                departure?.let { "${dayLabel(it.at, now)} · ${formatCountdown(it.until)}" } ?: "sem horários",
                Modifier.weight(1f).fillMaxHeight(),
            )
            StatCard(
                "status da rota",
                routeStyle.label,
                trip.route.name,
                Modifier.weight(1f).fillMaxHeight(),
                valueColor = routeStyle.kind.fg,
            )
        }
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                "meu ponto",
                stop?.let { "${it.sequence + 1} de ${trip.stops.size}" } ?: "--",
                stop?.title ?: "não definido",
                Modifier.weight(1f).fillMaxHeight(),
            )
            StatCard(
                "presença hoje",
                trip.boardingStatus.shortLabel(),
                if (trip.boardingStatus == BoardingStatus.CONFIRMED) "motorista avisado" else "responda abaixo",
                Modifier.weight(1f).fillMaxHeight(),
                valueColor = boardingStyle.kind.fg,
                noteColor = if (trip.boardingStatus == BoardingStatus.CONFIRMED) RcColors.Success else RcColors.TextMuted,
            )
        }

        BoardingPanel(trip, state.updatingBoarding, onBoarding)

        Panel(title = "Meu ponto de embarque") {
            if (stop == null) {
                Text("Nenhum ponto definido para você nesta rota.", color = RcColors.TextMenu)
            } else {
                InnerCard {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(stop.title, color = RcColors.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            stop.subtitle?.let { Text(it, color = RcColors.TextMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp)) }
                        }
                        Badge("parada ${stop.sequence + 1}", BadgeKind.INFO, Modifier.padding(start = 8.dp))
                    }
                    ProgressRow(
                        "Posição na rota: ${stop.sequence + 1} de ${trip.stops.size} paradas",
                        (stop.sequence + 1f) / trip.stops.size.coerceAtLeast(1),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RcStrokedButton("Como chegar", { actions.openMap(stop.latitude, stop.longitude, stop.title) }, Modifier.weight(1f), icon = Icons.Filled.Map)
                    RcStrokedButton("Ver rota", onOpenRoute, Modifier.weight(1f), icon = Icons.Filled.Route)
                }
            }
        }

        Panel(
            title = "Veículo e motorista",
            caption = state.lastUpdated?.let { "atualizado às ${it.format(DateTimeFormatter.ofPattern("HH:mm"))}" },
        ) {
            trip.vehicle?.let { v ->
                InnerCard {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(v.model, color = RcColors.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                v.plateNumber + (v.capacity?.let { " · $it lugares" } ?: ""),
                                color = RcColors.TextMuted,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        Badge(routeStyle.label, routeStyle.kind)
                    }
                    trip.events.firstOrNull()?.let { ev ->
                        Text("Último evento: ${ev.description.ifBlank { ev.eventType }}", color = RcColors.TextLabel, fontSize = 14.sp)
                    }
                }
            }
            trip.driver?.let { driver ->
                InfoRow(Icons.Filled.Person, "Motorista", driver.name) {
                    FilledIconButton(
                        onClick = { actions.dial(driver.phone) },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = RcColors.Accent, contentColor = RcColors.White),
                    ) { Icon(Icons.Filled.Call, contentDescription = "Ligar para o motorista") }
                }
            }
            if (trip.vehicle == null && trip.driver == null) Text("Veículo ainda não definido.", color = RcColors.TextMenu)
        }

        if (trip.schedules.isNotEmpty()) {
            Panel(title = "Horários da rota", caption = "dias úteis") {
                trip.schedules.forEach { s ->
                    InfoRow(Icons.Filled.Schedule, s.weekdays.joinToString(" · ") { weekdayLabel(it) }, s.time)
                }
            }
        }
    }
}

@Composable
private fun BoardingPanel(trip: Trip, updating: Boolean, onBoarding: (BoardingStatus) -> Unit) {
    val style = trip.boardingStatus.style()
    val confirmed = trip.boardingStatus == BoardingStatus.CONFIRMED
    val absent = trip.boardingStatus == BoardingStatus.ABSENT
    Panel(title = "Você vai embarcar hoje?") {
        Badge(style.label, style.kind)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RcButton(
                "Sim, vou",
                { onBoarding(BoardingStatus.CONFIRMED) },
                Modifier.weight(1f),
                icon = Icons.Filled.Check,
                enabled = !updating,
                color = if (confirmed) RcColors.SuccessBg else RcColors.MatPrimary,
            )
            RcStrokedButton(
                "Não vou",
                { onBoarding(BoardingStatus.ABSENT) },
                Modifier.weight(1f),
                icon = Icons.Filled.Close,
                enabled = !updating,
                color = if (absent) RcColors.Danger else RcColors.MatWarn,
                borderColor = if (absent) RcColors.Danger else RcColors.Divider,
                containerColor = if (absent) RcColors.DangerBg else Color.Transparent,
            )
        }
        if (trip.boardingStatus != BoardingStatus.PENDING) {
            TextButton(onClick = { onBoarding(BoardingStatus.PENDING) }, enabled = !updating) {
                Text("Desfazer resposta", color = RcColors.Accent)
            }
        }
    }
}

private fun dayLabel(at: LocalDateTime, now: LocalDateTime): String = when (at.toLocalDate()) {
    now.toLocalDate() -> "hoje"
    now.toLocalDate().plusDays(1) -> "amanhã"
    else -> at.format(DateTimeFormatter.ofPattern("dd/MM"))
}

package br.com.rotacerta.colaborador.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.rotacerta.colaborador.data.TrackingState
import br.com.rotacerta.colaborador.data.model.BoardingStatus
import br.com.rotacerta.colaborador.data.model.DriverRoute
import br.com.rotacerta.colaborador.data.model.PersonBrief
import br.com.rotacerta.colaborador.ui.components.Badge
import br.com.rotacerta.colaborador.ui.components.BadgeKind
import br.com.rotacerta.colaborador.ui.components.ConfirmDialog
import br.com.rotacerta.colaborador.ui.components.InfoRow
import br.com.rotacerta.colaborador.ui.components.LoadingState
import br.com.rotacerta.colaborador.ui.components.MenuItem
import br.com.rotacerta.colaborador.ui.components.MessageState
import br.com.rotacerta.colaborador.ui.components.PageHeader
import br.com.rotacerta.colaborador.ui.components.Panel
import br.com.rotacerta.colaborador.ui.components.RcAvatar
import br.com.rotacerta.colaborador.ui.components.RcButton
import br.com.rotacerta.colaborador.ui.components.RcStrokedButton
import br.com.rotacerta.colaborador.ui.components.StatCard
import br.com.rotacerta.colaborador.ui.components.style
import br.com.rotacerta.colaborador.ui.theme.RcColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

// ---------------------------------------------------------------- Rota do dia

@Composable
fun DriverRouteScreen(
    state: DriverUiState,
    tracking: TrackingState,
    onRefresh: () -> Unit,
    onSelectRoute: (Long) -> Unit,
    onConsent: (Boolean) -> Unit,
    onStart: (DriverRoute) -> Unit,
    onStop: () -> Unit,
) {
    val route = state.selectedRoute
    when {
        route != null -> DriverRouteContent(state, route, tracking, onRefresh, onSelectRoute, onConsent, onStart, onStop)
        state.loading -> LoadingState()
        state.error != null -> MessageState("Não foi possível carregar as rotas", state.error, actionLabel = "Tentar novamente", onAction = onRefresh)
        else -> MessageState(
            "Nenhuma rota para você",
            "Quando a gestão vincular você ao veículo de uma rota, ela aparece aqui.",
            icon = Icons.Filled.DirectionsBus,
            actionLabel = "Atualizar",
            onAction = onRefresh,
        )
    }
}

@Composable
private fun DriverRouteContent(
    state: DriverUiState,
    route: DriverRoute,
    tracking: TrackingState,
    onRefresh: () -> Unit,
    onSelectRoute: (Long) -> Unit,
    onConsent: (Boolean) -> Unit,
    onStart: (DriverRoute) -> Unit,
    onStop: () -> Unit,
) {
    var confirmStop by remember { mutableStateOf(false) }
    if (confirmStop) {
        ConfirmDialog(
            message = "Encerrar a rota?",
            description = "A chegada é registrada e o app para de enviar a localização da van.",
            confirmLabel = "Encerrar",
            onConfirm = { confirmStop = false; onStop() },
            onCancel = { confirmStop = false },
        )
    }
    val running = tracking.active && tracking.routeId == route.id
    val confirmed = route.passengers.count { it.status == BoardingStatus.CONFIRMED }
    val absent = route.passengers.count { it.status == BoardingStatus.ABSENT }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            PageHeader("Rota do dia", route.vehicle?.let { "${it.model.orEmpty()} · ${it.plateNumber}".trimStart(' ', '·') }, Modifier.weight(1f))
            IconButton(onClick = onRefresh) { Icon(Icons.Filled.Refresh, contentDescription = "Atualizar", tint = RcColors.TextLabel) }
        }

        if (state.routes.size > 1) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.routes.forEach { r ->
                    MenuItem(Icons.Filled.Route, r.name, r.id == route.id, { onSelectRoute(r.id) }, Modifier.padding(horizontal = 4.dp))
                }
            }
        }

        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("paradas", "${route.stops.size}", route.name, Modifier.weight(1f).fillMaxHeight())
            StatCard(
                "confirmados", "$confirmed de ${route.passengers.size}",
                if (absent > 0) "$absent não vão hoje" else "passageiros",
                Modifier.weight(1f).fillMaxHeight(), valueColor = RcColors.Success,
            )
        }

        if (!running) {
            // Mesmo aviso da página de rastreamento: nada de mexer no celular dirigindo.
            Text(
                "Segurança: configure tudo e toque em Iniciar rota antes de sair. Depois deixe o celular no suporte; " +
                    "o app continua enviando a localização com a tela apagada.",
                color = RcColors.Warning,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().background(RcColors.WarningBg, RoundedCornerShape(12.dp)).padding(16.dp),
            )
            Panel(title = "Compartilhar localização") {
                Row(
                    Modifier.fillMaxWidth().clickable { onConsent(!state.consent) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = state.consent,
                        onCheckedChange = onConsent,
                        colors = CheckboxDefaults.colors(checkedColor = RcColors.Accent, uncheckedColor = RcColors.TextLabel),
                    )
                    Text(
                        "Concordo em compartilhar minha localização com os passageiros desta rota enquanto ela estiver em andamento.",
                        color = RcColors.Text,
                        fontSize = 14.sp,
                    )
                }
                RcButton("Iniciar rota", { onStart(route) }, Modifier.fillMaxWidth(), icon = Icons.Filled.PlayArrow, enabled = state.consent && !tracking.active)
                if (tracking.active) {
                    Text("Outra rota está em andamento: encerre-a antes.", color = RcColors.TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            RcButton("Encerrar rota", { confirmStop = true }, Modifier.fillMaxWidth(), icon = Icons.Filled.Stop, color = RcColors.MatWarn)
        }

        TrackingPanel(tracking.takeIf { it.routeId == route.id } ?: TrackingState())

        Panel(title = "Paradas", caption = "${route.stops.size} no total") {
            route.stops.sortedBy { it.sequence }.forEach { stop ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${stop.sequence + 1}",
                        color = RcColors.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.background(RcColors.Topbar, RoundedCornerShape(20.dp)).padding(horizontal = 9.dp, vertical = 3.dp),
                    )
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(stop.title, color = RcColors.TextStrong, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        stop.subtitle?.let { Text(it, color = RcColors.TextMuted, fontSize = 13.sp) }
                    }
                    Text(passengersLabel(stop.passengerCount), color = RcColors.TextLabel, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun TrackingPanel(tracking: TrackingState) {
    val (label, kind) = when {
        !tracking.active -> "parado" to BadgeKind.INFO
        tracking.sent > 0 -> "ao vivo" to BadgeKind.SUCCESS
        else -> "aguardando GPS" to BadgeKind.WARNING
    }
    Panel(title = if (tracking.active) "Enviando localização" else "Rastreamento desligado") {
        Badge(label, kind)
        StatusLine("Posições enviadas", "${tracking.sent}")
        StatusLine("Último envio", tracking.lastSentAtMillis?.let { clock(it) } ?: "—")
        StatusLine("Precisão do GPS", tracking.lastFix?.accuracyMeters?.let { "${it.roundToInt()} m" } ?: "—")
        StatusLine("Velocidade", tracking.lastFix?.speedMps?.let { "${(it * 3.6).roundToInt()} km/h" } ?: "—")
        tracking.error?.let { Text(it, color = RcColors.Danger, fontSize = 14.sp) }
    }
}

@Composable
private fun StatusLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, color = RcColors.TextLabel, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = RcColors.Text, fontSize = 14.sp)
    }
}

private val CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.forLanguageTag("pt-BR"))
private fun clock(millis: Long) = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(CLOCK)
private fun passengersLabel(n: Int) = if (n == 1) "1 passageiro" else "$n passageiros"

// ---------------------------------------------------------------- Passageiros

@Composable
fun DriverPassengersScreen(state: DriverUiState, onRefresh: () -> Unit) {
    val route = state.selectedRoute
    if (route == null) {
        if (state.loading) LoadingState() else MessageState("Sem passageiros", "Nenhuma rota vinculada a você.", actionLabel = "Atualizar", onAction = onRefresh)
        return
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            PageHeader("Passageiros", route.name, Modifier.weight(1f))
            IconButton(onClick = onRefresh) { Icon(Icons.Filled.Refresh, contentDescription = "Atualizar", tint = RcColors.TextLabel) }
        }
        val byStop = route.passengers.groupBy { it.routeStopId }
        val stops = route.stops.sortedBy { it.sequence }
        stops.forEach { stop ->
            val list = byStop[stop.id].orEmpty()
            if (list.isNotEmpty()) {
                Panel(title = "${stop.sequence + 1}. ${stop.title}", caption = passengersLabel(list.size)) {
                    list.sortedBy { it.name }.forEach { p -> PassengerRow(p.name, p.status) }
                }
            }
        }
        val withoutStop = route.passengers.filter { p -> stops.none { it.id == p.routeStopId } }
        if (withoutStop.isNotEmpty()) {
            Panel(title = "Sem ponto definido") { withoutStop.forEach { p -> PassengerRow(p.name, p.status) } }
        }
        if (route.passengers.isEmpty()) {
            Text("Nenhum passageiro cadastrado nesta rota.", color = RcColors.TextMuted, fontSize = 14.sp)
        }
        Text(
            "As confirmações são feitas pelos colaboradores no app e atualizam a cada 30 segundos.",
            color = RcColors.TextMuted,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun PassengerRow(name: String, status: BoardingStatus) {
    val style = status.style()
    Row(verticalAlignment = Alignment.CenterVertically) {
        RcAvatar(name, size = 32)
        Text(name, color = RcColors.Text, fontSize = 15.sp, modifier = Modifier.padding(start = 12.dp).weight(1f))
        Badge(style.label, style.kind)
    }
}

// ---------------------------------------------------------------- Perfil

@Composable
fun DriverProfileScreen(person: PersonBrief?, onChangePassword: () -> Unit, onLogout: () -> Unit) {
    var confirmLogout by remember { mutableStateOf(false) }
    if (confirmLogout) {
        ConfirmDialog(
            message = "Deseja sair da conta?",
            description = "Se houver uma rota em andamento, ela é encerrada.",
            confirmLabel = "Sair",
            onConfirm = onLogout,
            onCancel = { confirmLogout = false },
        )
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RcAvatar(person?.name ?: "?", size = 64)
                Column(Modifier.padding(start = 16.dp).weight(1f)) {
                    Text(person?.name.orEmpty(), color = RcColors.TextStrong, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(person?.companyName.orEmpty(), color = RcColors.TextMuted, fontSize = 14.sp)
                    Badge("motorista", BadgeKind.INFO, Modifier.padding(top = 8.dp))
                }
            }
        }
        Panel(title = "Meus dados") {
            person?.email?.let { InfoRow(Icons.Filled.Email, "E-mail de acesso", it) }
            InfoRow(Icons.Filled.Business, "Empresa", person?.companyName.orEmpty())
            InfoRow(Icons.Filled.Badge, "Perfil", "Motorista")
        }
        Text("Dados incorretos? Procure a gestão de transporte da empresa.", color = RcColors.TextMuted, fontSize = 13.sp)
        RcStrokedButton("Alterar senha", onChangePassword, Modifier.fillMaxWidth(), icon = Icons.Filled.Lock)
        RcButton("Sair da conta", { confirmLogout = true }, Modifier.fillMaxWidth(), icon = Icons.AutoMirrored.Filled.Logout, color = RcColors.MatWarn)
    }
}

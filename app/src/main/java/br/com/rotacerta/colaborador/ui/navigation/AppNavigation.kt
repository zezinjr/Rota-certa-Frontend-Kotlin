package br.com.rotacerta.colaborador.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.rotacerta.colaborador.data.AppContainer
import br.com.rotacerta.colaborador.data.session.SessionState
import br.com.rotacerta.colaborador.platform.PlatformActions
import br.com.rotacerta.colaborador.ui.components.MenuItem
import br.com.rotacerta.colaborador.ui.components.RcTopBar
import br.com.rotacerta.colaborador.ui.login.ChangePasswordScreen
import br.com.rotacerta.colaborador.ui.login.ChangePasswordViewModel
import br.com.rotacerta.colaborador.ui.login.ForgotPasswordScreen
import br.com.rotacerta.colaborador.ui.login.ForgotPasswordViewModel
import br.com.rotacerta.colaborador.ui.login.LoginScreen
import br.com.rotacerta.colaborador.ui.login.LoginViewModel
import br.com.rotacerta.colaborador.ui.profile.ProfileScreen
import br.com.rotacerta.colaborador.ui.theme.RcColors
import br.com.rotacerta.colaborador.ui.trip.AlertsScreen
import br.com.rotacerta.colaborador.ui.trip.RouteScreen
import br.com.rotacerta.colaborador.ui.trip.TripScreen
import br.com.rotacerta.colaborador.ui.trip.TripViewModel

/** Raiz do app: escolhe o fluxo de acordo com o estado da sessão. */
@Composable
fun RotaCertaRoot(container: AppContainer, actions: PlatformActions) {
    val session by container.session.state.collectAsState()
    when (val s = session) {
        SessionState.LoggedOut -> AuthFlow(container)
        is SessionState.PasswordChangeRequired -> ChangePasswordScreen(
            viewModel = viewModel(key = "forced-${s.person.id}") { ChangePasswordViewModel(container.repository) },
            forced = true,
        )
        is SessionState.LoggedIn -> MainFlow(container, actions, s.person.id)
    }
}

@Composable
private fun AuthFlow(container: AppContainer) {
    val nav = rememberNavController()
    NavHost(nav, startDestination = "login") {
        composable("login") {
            LoginScreen(
                viewModel = viewModel { LoginViewModel(container.repository) },
                onForgotPassword = { nav.navigate("forgot") },
            )
        }
        composable("forgot") {
            ForgotPasswordScreen(
                viewModel = viewModel { ForgotPasswordViewModel(container.repository) },
                onBack = { nav.popBackStack() },
            )
        }
    }
}

// Mesmos ícones (Material Icons preenchidos) do menu lateral do painel.
private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    TRIP("viagem", "Viagem", Icons.Filled.DirectionsBus),
    ROUTE("rota", "Rota", Icons.Filled.Route),
    ALERTS("avisos", "Alertas", Icons.Filled.Notifications),
    PROFILE("perfil", "Perfil", Icons.Filled.Person),
}

@Composable
private fun MainFlow(container: AppContainer, actions: PlatformActions, personId: Long) {
    val tripViewModel: TripViewModel = viewModel(key = "trips-$personId") { TripViewModel(container.repository) }
    val state by tripViewModel.state.collectAsState()
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); tripViewModel.messageShown() }
    }

    fun go(route: String) = nav.navigate(route) {
        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    val person = (container.session.state.collectAsState().value as? SessionState.LoggedIn)?.person

    Scaffold(
        containerColor = RcColors.Background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            if (Tab.entries.any { it.route == current }) {
                RcTopBar(userName = person?.name?.substringBefore(' '), userRole = "Colaborador")
            }
        },
        bottomBar = {
            // Estilo do menu lateral (.sidebar / .menu-item): fundo #0f172a, item ativo #1e40af.
            if (Tab.entries.any { it.route == current }) {
                Column(Modifier.background(RcColors.Background)) {
                    HorizontalDivider(color = RcColors.Panel, thickness = 1.dp)
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Tab.entries.forEach { tab ->
                            MenuItem(tab.icon, tab.label, current == tab.route, { go(tab.route) }, Modifier.weight(1f))
                        }
                    }
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = Tab.TRIP.route, modifier = Modifier.padding(padding)) {
            composable(Tab.TRIP.route) {
                TripScreen(
                    state = state,
                    actions = actions,
                    onRefresh = { tripViewModel.refresh() },
                    onSelectTrip = tripViewModel::selectTrip,
                    onBoarding = tripViewModel::setBoarding,
                    onOpenRoute = { go(Tab.ROUTE.route) },
                )
            }
            composable(Tab.ROUTE.route) { RouteScreen(state, actions) }
            composable(Tab.ALERTS.route) { AlertsScreen(state) }
            composable(Tab.PROFILE.route) {
                ProfileScreen(
                    profile = state.profile,
                    onChangePassword = { nav.navigate("alterar-senha") },
                    onLogout = tripViewModel::logout,
                )
            }
            composable("alterar-senha") {
                ChangePasswordScreen(
                    viewModel = viewModel { ChangePasswordViewModel(container.repository) },
                    forced = false,
                    onBack = { nav.popBackStack() },
                    onDone = { nav.popBackStack() },
                )
            }
        }
    }
}

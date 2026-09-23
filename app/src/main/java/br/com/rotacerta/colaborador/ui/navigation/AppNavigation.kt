package br.com.rotacerta.colaborador.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.rotacerta.colaborador.data.AppContainer
import br.com.rotacerta.colaborador.data.session.SessionState
import br.com.rotacerta.colaborador.platform.PlatformActions
import br.com.rotacerta.colaborador.ui.login.ChangePasswordScreen
import br.com.rotacerta.colaborador.ui.login.ChangePasswordViewModel
import br.com.rotacerta.colaborador.ui.login.ForgotPasswordScreen
import br.com.rotacerta.colaborador.ui.login.ForgotPasswordViewModel
import br.com.rotacerta.colaborador.ui.login.LoginScreen
import br.com.rotacerta.colaborador.ui.login.LoginViewModel
import br.com.rotacerta.colaborador.ui.profile.ProfileScreen
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

private enum class Tab(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    TRIP("viagem", "Viagem", Icons.Outlined.DirectionsBus, Icons.Filled.DirectionsBus),
    ROUTE("rota", "Rota", Icons.Outlined.Route, Icons.Filled.Route),
    ALERTS("avisos", "Avisos", Icons.Outlined.Notifications, Icons.Filled.Notifications),
    PROFILE("perfil", "Perfil", Icons.Outlined.Person, Icons.Filled.Person),
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (Tab.entries.any { it.route == current }) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Tab.entries.forEach { tab ->
                        val selected = current == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { go(tab.route) },
                            icon = { Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                        )
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

package br.com.rotacerta.colaborador.ui.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.rotacerta.colaborador.data.RotaCertaRepository
import br.com.rotacerta.colaborador.data.TrackingState
import br.com.rotacerta.colaborador.data.model.DriverRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DriverUiState(
    val loading: Boolean = true,
    val routes: List<DriverRoute> = emptyList(),
    val selectedRouteId: Long? = null,
    val consent: Boolean = false,
    val error: String? = null,
) {
    val selectedRoute: DriverRoute? get() = routes.firstOrNull { it.id == selectedRouteId } ?: routes.firstOrNull()
}

/**
 * Área do motorista: rotas do dia (com passageiros e confirmações) e o envio do GPS.
 * As rotas são recarregadas a cada 30 s com o app aberto, para as confirmações chegarem.
 */
class DriverViewModel(
    private val repository: RotaCertaRepository,
    val tracking: StateFlow<TrackingState>,
    private val foreground: StateFlow<Boolean> = MutableStateFlow(true),
    private val pollIntervalMillis: Long = 30_000,
) : ViewModel() {

    private val _state = MutableStateFlow(DriverUiState())
    val state: StateFlow<DriverUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                foreground.first { it }
                refresh(silent = _state.value.routes.isNotEmpty())
                delay(pollIntervalMillis)
            }
        }
    }

    fun refresh(silent: Boolean = false) {
        if (!silent) _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            repository.myRoutes()
                .onSuccess { routes ->
                    _state.update { s ->
                        // Com uma rota em andamento, ela fica selecionada.
                        val active = tracking.value.routeId.takeIf { tracking.value.active }
                        s.copy(loading = false, routes = routes, error = null, selectedRouteId = active ?: s.selectedRouteId)
                    }
                }
                .onFailure { e -> _state.update { it.copy(loading = false, error = if (it.routes.isEmpty()) e.message else null) } }
        }
    }

    fun selectRoute(id: Long) {
        if (tracking.value.active) return // não troca de rota no meio do caminho
        _state.update { it.copy(selectedRouteId = id) }
    }

    fun setConsent(value: Boolean) = _state.update { it.copy(consent = value) }

    fun logout() = repository.logout()
}

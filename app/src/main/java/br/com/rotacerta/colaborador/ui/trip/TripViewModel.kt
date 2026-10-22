package br.com.rotacerta.colaborador.ui.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.rotacerta.colaborador.data.RotaCertaRepository
import br.com.rotacerta.colaborador.data.model.BoardingStatus
import br.com.rotacerta.colaborador.data.model.EmployeeProfile
import br.com.rotacerta.colaborador.data.model.LiveStatus
import br.com.rotacerta.colaborador.data.model.Trip
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime

data class TripUiState(
    val loading: Boolean = true,
    val trips: List<Trip> = emptyList(),
    val selectedTripId: Long? = null,
    val error: String? = null,
    val updatingBoarding: Boolean = false,
    val message: String? = null,
    val lastUpdated: LocalTime? = null,
    val profile: EmployeeProfile? = null,
    val profileError: String? = null,
    /** Posição da van e previsão da viagem selecionada (atualiza a cada poucos segundos). */
    val live: LiveStatus? = null,
) {
    val selectedTrip: Trip? get() = trips.firstOrNull { it.id == selectedTripId } ?: trips.firstOrNull()

    /** Situação ao vivo só da viagem que está na tela. */
    val selectedLive: LiveStatus? get() = live?.takeIf { it.tripId == selectedTrip?.id }
}

/**
 * Estado compartilhado pelas abas (Viagem, Rota, Alertas, Perfil).
 * Viagens a cada 30 s e posição da van a cada 5 s, só com o app em primeiro plano.
 */
class TripViewModel(
    private val repository: RotaCertaRepository,
    private val foreground: StateFlow<Boolean> = MutableStateFlow(true),
    private val pollIntervalMillis: Long = 30_000,
    private val livePollMillis: Long = 5_000,
) : ViewModel() {

    private val _state = MutableStateFlow(TripUiState())
    val state: StateFlow<TripUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                foreground.first { it }
                refresh(silent = _state.value.trips.isNotEmpty())
                delay(pollIntervalMillis)
            }
        }
        viewModelScope.launch {
            while (true) {
                foreground.first { it }
                refreshLive()
                delay(livePollMillis)
            }
        }
    }

    /** Posição da van; falhas são ignoradas (a próxima consulta tenta de novo). */
    private suspend fun refreshLive() {
        val trip = _state.value.selectedTrip ?: return
        repository.live(trip.id).onSuccess { live ->
            _state.update { if (it.selectedTrip?.id == live.tripId) it.copy(live = live) else it }
        }
    }

    fun refresh(silent: Boolean = false) {
        if (!silent) _state.update { it.copy(loading = true, error = null) }
        // Se o perfil falhou antes (ex.: backend fora do ar), tenta de novo junto com as viagens.
        if (_state.value.profile == null) loadProfile()
        viewModelScope.launch {
            repository.trips()
                .onSuccess { trips ->
                    _state.update { it.copy(loading = false, trips = trips, error = null, lastUpdated = LocalTime.now()) }
                }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, error = if (it.trips.isEmpty()) e.message else null, message = if (it.trips.isEmpty()) null else e.message) }
                }
        }
    }

    fun loadProfile() {
        _state.update { it.copy(profileError = null) }
        viewModelScope.launch {
            repository.profile()
                .onSuccess { p -> _state.update { it.copy(profile = p, profileError = null) } }
                .onFailure { e -> _state.update { it.copy(profileError = e.message ?: "Erro ao carregar o perfil.") } }
        }
    }

    fun selectTrip(id: Long) {
        _state.update { it.copy(selectedTripId = id, live = null) }
        viewModelScope.launch { refreshLive() }
    }

    fun setBoarding(status: BoardingStatus) {
        val trip = _state.value.selectedTrip ?: return
        if (trip.boardingStatus == status) return
        _state.update { it.copy(updatingBoarding = true) }
        viewModelScope.launch {
            repository.updateBoarding(trip.id, status)
                .onSuccess { updated ->
                    _state.update { s ->
                        s.copy(
                            updatingBoarding = false,
                            trips = s.trips.map { if (it.id == updated.id) updated else it },
                            message = when (status) {
                                BoardingStatus.CONFIRMED -> "Presença confirmada. O motorista foi avisado."
                                BoardingStatus.ABSENT -> "Tudo certo, avisamos que você não vai embarcar."
                                BoardingStatus.PENDING -> "Resposta desfeita."
                            },
                        )
                    }
                }
                .onFailure { e -> _state.update { it.copy(updatingBoarding = false, message = e.message) } }
        }
    }

    fun messageShown() = _state.update { it.copy(message = null) }

    fun logout() = repository.logout()
}

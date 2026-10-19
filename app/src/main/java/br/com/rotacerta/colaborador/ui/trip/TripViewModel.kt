package br.com.rotacerta.colaborador.ui.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.rotacerta.colaborador.data.RotaCertaRepository
import br.com.rotacerta.colaborador.data.model.BoardingStatus
import br.com.rotacerta.colaborador.data.model.EmployeeProfile
import br.com.rotacerta.colaborador.data.model.Trip
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
) {
    val selectedTrip: Trip? get() = trips.firstOrNull { it.id == selectedTripId } ?: trips.firstOrNull()
}

/** Estado compartilhado pelas abas (Viagem, Rota, Avisos, Perfil). Atualiza a cada 30 s. */
class TripViewModel(
    private val repository: RotaCertaRepository,
    private val pollIntervalMillis: Long = 30_000,
) : ViewModel() {

    private val _state = MutableStateFlow(TripUiState())
    val state: StateFlow<TripUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                refresh(silent = _state.value.trips.isNotEmpty())
                delay(pollIntervalMillis)
            }
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

    fun selectTrip(id: Long) = _state.update { it.copy(selectedTripId = id) }

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

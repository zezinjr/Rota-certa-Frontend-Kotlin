package com.rotacerta.mobile.feature.collaborator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rotacerta.mobile.core.location.Geo
import com.rotacerta.mobile.core.network.ApiResult
import com.rotacerta.mobile.core.util.UiState
import com.rotacerta.mobile.data.dto.BoardingStatus
import com.rotacerta.mobile.data.dto.CollaboratorRouteDto
import com.rotacerta.mobile.data.repository.CollaboratorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Distância do ônibus até a parada do colaborador e chegada estimada. */
data class BusProximity(val distanceMeters: Double, val etaMinutes: Int?)

data class CollaboratorUiState(
    val routes: UiState<List<CollaboratorRouteDto>> = UiState.Loading,
    val selected: CollaboratorRouteDto? = null,
    /** Vínculo cuja resposta está sendo enviada. */
    val sendingId: Long? = null,
    /** "Alterar resposta": mostra de novo o card de confirmação, sem mudar nada no backend. */
    val editingAnswer: Boolean = false,
) {
    val proximity: BusProximity?
        get() {
            val route = selected ?: return null
            val stop = route.stop ?: return null
            val bus = route.lastLocation ?: return null
            // Preferência: previsão do backend pelo trajeto das ruas, com as paradas antes da sua.
            val eta = route.eta
            if (eta != null && eta.isLive && eta.distanceMeters != null) {
                return BusProximity(eta.distanceMeters.toDouble(), eta.minutes)
            }
            // Sem ela (backend antigo ou rota sem trajeto): linha reta pela velocidade atual.
            val distance = Geo.distanceMeters(bus.latitude, bus.longitude, stop.latitude, stop.longitude)
            return BusProximity(distance, Geo.etaMinutes(distance, bus.speed))
        }
}

/** Compartilhado pelas abas Mapa, Agenda e Perfil do colaborador. */
@HiltViewModel
class CollaboratorViewModel @Inject constructor(
    private val repository: CollaboratorRepository,
) : ViewModel() {
    private val selectedId = MutableStateFlow<Long?>(null)
    private val sendingId = MutableStateFlow<Long?>(null)
    private val editing = MutableStateFlow(false)

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    val state: StateFlow<CollaboratorUiState> =
        combine(repository.routes, selectedId, sendingId, editing) { routes, selId, sending, edit ->
            val list = (routes as? UiState.Success)?.data.orEmpty()
            CollaboratorUiState(
                routes = routes,
                selected = list.firstOrNull { it.id == selId } ?: list.firstOrNull(),
                sendingId = sending,
                editingAnswer = edit,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollaboratorUiState())

    init {
        viewModelScope.launch { repository.ensureLoaded() }
    }

    fun refresh() = viewModelScope.launch { repository.refresh() }

    fun select(routePassengerId: Long) {
        selectedId.value = routePassengerId
        editing.value = false
    }

    fun editAnswer(value: Boolean) {
        editing.value = value
    }

    fun respond(routePassengerId: Long, willBoard: Boolean) {
        if (sendingId.value != null) return
        viewModelScope.launch {
            sendingId.value = routePassengerId
            val status = if (willBoard) BoardingStatus.CONFIRMED else BoardingStatus.ABSENT
            when (val result = repository.updateBoardingStatus(routePassengerId, status)) {
                is ApiResult.Success -> {
                    editing.value = false
                    _messages.tryEmit(
                        if (willBoard) "Motorista recebeu sua confirmação." else "Tudo bem! O motorista foi avisado que você não irá.",
                    )
                }
                is ApiResult.Failure -> _messages.tryEmit(result.message)
            }
            sendingId.value = null
        }
    }
}

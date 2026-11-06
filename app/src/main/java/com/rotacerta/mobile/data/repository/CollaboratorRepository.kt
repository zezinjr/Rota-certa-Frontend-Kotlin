package com.rotacerta.mobile.data.repository

import com.rotacerta.mobile.core.network.ApiResult
import com.rotacerta.mobile.core.network.apiCall
import com.rotacerta.mobile.core.network.map
import com.rotacerta.mobile.core.realtime.RealtimeClient
import com.rotacerta.mobile.core.realtime.RealtimeEvent
import com.rotacerta.mobile.core.session.SessionStore
import com.rotacerta.mobile.core.util.UiState
import com.rotacerta.mobile.data.api.CollaboratorApi
import com.rotacerta.mobile.data.dto.BoardingStatusRequest
import com.rotacerta.mobile.data.dto.CollaboratorRouteDto
import com.rotacerta.mobile.data.dto.NotificationKind
import com.rotacerta.mobile.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fonte única das rotas do colaborador (abas Mapa, Agenda e Perfil),
 * atualizada pelo WebSocket: posição do ônibus, status da rota e embarque.
 */
@Singleton
class CollaboratorRepository @Inject constructor(
    private val api: CollaboratorApi,
    realtimeClient: RealtimeClient,
    sessionStore: SessionStore,
    @param:ApplicationScope private val scope: CoroutineScope,
) {
    private val _routes = MutableStateFlow<UiState<List<CollaboratorRouteDto>>>(UiState.Idle)
    val routes: StateFlow<UiState<List<CollaboratorRouteDto>>> = _routes.asStateFlow()

    init {
        scope.launch { realtimeClient.events.collect(::apply) }
        scope.launch { sessionStore.session.collect { if (it == null) _routes.value = UiState.Idle } }
    }

    /** Carrega se ainda não carregou (ou se a última tentativa falhou). */
    suspend fun ensureLoaded() {
        if (_routes.value is UiState.Idle || _routes.value is UiState.Error) refresh()
    }

    suspend fun refresh() {
        if (_routes.value !is UiState.Success) _routes.value = UiState.Loading
        when (val result = apiCall("Não foi possível carregar suas rotas.") { api.myRoutes() }) {
            is ApiResult.Success -> _routes.value = UiState.Success(result.data)
            // Com dados na tela, mantém o que já tem em vez de trocar por erro.
            is ApiResult.Failure -> if (_routes.value !is UiState.Success) _routes.value = UiState.Error(result.message)
        }
    }

    suspend fun updateBoardingStatus(routePassengerId: Long, status: String): ApiResult<Unit> =
        apiCall("Não foi possível enviar sua resposta.") {
            api.updateBoardingStatus(routePassengerId, BoardingStatusRequest(status))
        }.also { result ->
            if (result is ApiResult.Success) replace(result.data)
        }.map { }

    private fun replace(updated: CollaboratorRouteDto) = updateItems { item ->
        if (item.id == updated.id) updated else item
    }

    private fun apply(event: RealtimeEvent) {
        when (event) {
            is RealtimeEvent.VehicleLocation -> {
                updateItems { item ->
                    if (item.route.id == event.location.routeId) item.copy(lastLocation = event.location) else item
                }
                refreshEta(event.location.routeId)
            }
            is RealtimeEvent.RouteEventOccurred -> updateItems { item ->
                if (item.route.id == event.routeId) item.copy(route = item.route.copy(routeStatus = event.routeStatus)) else item
            }
            is RealtimeEvent.BoardingStatusChanged -> updateItems { item ->
                if (item.id == event.routePassengerId) item.copy(boardingStatus = event.status) else item
            }
            // Reconectou: busca de novo o que pode ter mudado enquanto estava sem conexão.
            is RealtimeEvent.Connected -> if (_routes.value is UiState.Success) scope.launch { refresh() }
            // Ponto de embarque mudou (regra de proximidade aplicada pelo gestor): recarrega a rota.
            is RealtimeEvent.NotificationReceived ->
                if (event.notification.kind == NotificationKind.PICKUP_CHANGED) scope.launch { refresh() }
        }
    }

    /** Busca no backend a previsão pelo trajeto das ruas (o WebSocket só traz a posição do ônibus). */
    private fun refreshEta(routeId: Long) {
        val items = (_routes.value as? UiState.Success)?.data.orEmpty().filter { it.route.id == routeId }
        items.forEach { item ->
            scope.launch {
                val result = apiCall("Não foi possível atualizar a previsão.") { api.eta(item.id) }
                if (result is ApiResult.Success) {
                    updateItems { if (it.id == item.id) it.copy(eta = result.data.eta) else it }
                }
            }
        }
    }

    private fun updateItems(transform: (CollaboratorRouteDto) -> CollaboratorRouteDto) {
        _routes.update { state ->
            if (state is UiState.Success) UiState.Success(state.data.map(transform)) else state
        }
    }
}

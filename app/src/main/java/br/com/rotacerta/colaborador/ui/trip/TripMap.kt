package br.com.rotacerta.colaborador.ui.trip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import br.com.rotacerta.colaborador.data.model.Trip

/** Onde o mapa deve começar: a rota inteira ou o ponto de embarque do colaborador. */
enum class MapFocus { WHOLE_ROUTE, MY_STOP }

data class TripMapRequest(
    val trip: Trip,
    val modifier: Modifier,
    /** false = só visualização (card na aba Rota); true = arrastar/zoom/minha localização. */
    val interactive: Boolean,
    val focus: MapFocus = MapFocus.WHOLE_ROUTE,
)

/**
 * Mapa usado pelas telas. O Android fornece o Google Maps (MainActivity) quando há
 * chave configurada; sem chave (ou fora do Android) fica o mapa esquemático [RouteMap].
 */
val LocalTripMap = staticCompositionLocalOf<@Composable (TripMapRequest) -> Unit> {
    { request -> RouteMap(request.trip, request.modifier) }
}

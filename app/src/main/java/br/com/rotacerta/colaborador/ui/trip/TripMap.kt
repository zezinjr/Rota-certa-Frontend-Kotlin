package br.com.rotacerta.colaborador.ui.trip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import br.com.rotacerta.colaborador.data.model.LiveStatus
import br.com.rotacerta.colaborador.data.model.PathProgress
import br.com.rotacerta.colaborador.data.model.Trip

/** Onde o mapa deve começar: a rota inteira ou o ponto de embarque do colaborador. */
enum class MapFocus { WHOLE_ROUTE, MY_STOP }

data class TripMapRequest(
    val trip: Trip,
    val modifier: Modifier,
    /** false = só visualização (card na aba Rota); true = arrastar/zoom/minha localização. */
    val interactive: Boolean,
    val focus: MapFocus = MapFocus.WHOLE_ROUTE,
    /** Posição ao vivo da van (a cada 5 s); sem ela, usa a última posição conhecida da viagem. */
    val live: LiveStatus? = null,
)

/** Ponto do trajeto em (latitude, longitude). */
typealias GeoPoint = Pair<Double, Double>

/** Trajeto considerado pelo backend: paradas em ordem + empresa no final. */
fun Trip.routePath(): List<GeoPoint> =
    stops.map { it.latitude to it.longitude } + listOfNotNull(destination?.let { it.latitude to it.longitude })

fun TripMapRequest.vanPosition(): GeoPoint? =
    live?.vehicle?.let { it.latitude to it.longitude } ?: trip.lastPosition?.let { it.latitude to it.longitude }

/** Direção da van em graus (0 = norte), quando o GPS informa. */
fun TripMapRequest.vanHeading(): Float? = live?.vehicle?.heading?.toFloat()

/**
 * Divide o trajeto na posição da van: (trecho já percorrido, trecho restante).
 * Sem progresso conhecido, tudo é "restante".
 */
fun splitPath(path: List<GeoPoint>, progress: PathProgress?): Pair<List<GeoPoint>, List<GeoPoint>> {
    if (progress == null || path.size < 2) return emptyList<GeoPoint>() to path
    val i = progress.segment.coerceIn(0, path.size - 2)
    val f = progress.fraction.coerceIn(0.0, 1.0)
    val a = path[i]
    val b = path[i + 1]
    val cut = (a.first + (b.first - a.first) * f) to (a.second + (b.second - a.second) * f)
    return (path.subList(0, i + 1) + cut) to (listOf(cut) + path.subList(i + 1, path.size))
}

/**
 * Mapa usado pelas telas. O Android fornece o Google Maps (MainActivity) quando há
 * chave configurada; sem chave (ou fora do Android) fica o mapa esquemático [RouteMap].
 */
val LocalTripMap = staticCompositionLocalOf<@Composable (TripMapRequest) -> Unit> {
    { request -> RouteMap(request) }
}

package br.com.rotacerta.colaborador.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import br.com.rotacerta.colaborador.R
import br.com.rotacerta.colaborador.ui.theme.RcColors
import br.com.rotacerta.colaborador.ui.trip.MapFocus
import br.com.rotacerta.colaborador.ui.trip.TripMapRequest
import br.com.rotacerta.colaborador.ui.trip.routePath
import br.com.rotacerta.colaborador.ui.trip.splitPath
import br.com.rotacerta.colaborador.ui.trip.vanHeading
import br.com.rotacerta.colaborador.ui.trip.vanPosition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

/** Renderizador entregue às telas via LocalTripMap (ver MainActivity). */
val GoogleTripMapRenderer: @Composable (TripMapRequest) -> Unit = { request -> GoogleTripMap(request) }

/**
 * Google Maps dentro do app: paradas numeradas (amarelo = ponto do colaborador),
 * empresa, van na última posição conhecida e o trajeto ligando as paradas.
 */
@Composable
fun GoogleTripMap(request: TripMapRequest) {
    val trip = request.trip
    val context = LocalContext.current
    val density = LocalDensity.current

    val stops = trip.stops.map { LatLng(it.latitude, it.longitude) }
    val destination = trip.destination?.let { LatLng(it.latitude, it.longitude) }
    val van = request.vanPosition()?.let { LatLng(it.first, it.second) }
    val isLive = request.live?.isLive == true
    val myStop = trip.myStop?.let { LatLng(it.latitude, it.longitude) }
    val allPoints = stops + listOfNotNull(destination, van)

    var hasLocation by remember { mutableStateOf(hasLocationPermission(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        hasLocation = result.values.any { it }
    }
    val showMyLocation = request.interactive && hasLocation

    val cameraState = rememberCameraPositionState(key = "trip-${trip.id}-${request.focus}") {
        position = CameraPosition.fromLatLngZoom(myStop ?: allPoints.firstOrNull() ?: MANAUS, 12f)
    }
    var mapLoaded by remember { mutableStateOf(false) }
    // Enquadra a rota (ou o meu ponto) quando o mapa termina de carregar.
    LaunchedEffect(mapLoaded, trip.id, request.focus) {
        if (!mapLoaded || allPoints.isEmpty()) return@LaunchedEffect
        val update = if (request.focus == MapFocus.MY_STOP && myStop != null) {
            CameraUpdateFactory.newLatLngZoom(myStop, 16f)
        } else {
            val bounds = LatLngBounds.builder().apply { allPoints.forEach { include(it) } }.build()
            CameraUpdateFactory.newLatLngBounds(bounds, with(density) { 48.dp.roundToPx() })
        }
        cameraState.move(update)
    }

    // Com a van ao vivo, a câmera acompanha van + meu ponto até o usuário arrastar o mapa.
    var following by remember { mutableStateOf(true) }
    LaunchedEffect(cameraState.isMoving) {
        if (cameraState.isMoving && cameraState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) following = false
    }
    LaunchedEffect(van, following, mapLoaded, isLive) {
        if (!request.interactive || !isLive || !following || !mapLoaded || van == null) return@LaunchedEffect
        val target = listOfNotNull(van, myStop.takeIf { request.live?.eta != null })
        val update = if (target.size == 1) {
            CameraUpdateFactory.newLatLngZoom(van, 15f)
        } else {
            CameraUpdateFactory.newLatLngBounds(
                LatLngBounds.builder().apply { target.forEach { include(it) } }.build(),
                with(density) { 64.dp.roundToPx() },
            )
        }
        runCatching { cameraState.animate(update, 900) }
    }

    val mapStyle = remember { MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark) }

    Box(request.modifier) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraState,
            properties = MapProperties(isMyLocationEnabled = showMyLocation, mapStyleOptions = mapStyle),
            uiSettings = MapUiSettings(
                compassEnabled = false,
                indoorLevelPickerEnabled = false,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = showMyLocation,
                rotationGesturesEnabled = false,
                scrollGesturesEnabled = request.interactive,
                tiltGesturesEnabled = false,
                zoomControlsEnabled = false,
                zoomGesturesEnabled = request.interactive,
            ),
            // Em tela cheia, reserva espaço para a barra do topo e o card do rodapé.
            contentPadding = if (request.interactive) PaddingValues(top = 72.dp, bottom = 230.dp) else PaddingValues(0.dp),
            onMapLoaded = { mapLoaded = true },
        ) {
            // Trajeto (paradas → empresa); trecho já percorrido pela van em cinza.
            val (done, remaining) = splitPath(trip.routePath(), request.live?.pathProgress)
            listOf(done to RcColors.TextMuted, remaining to RcColors.Accent).forEach { (points, color) ->
                if (points.size >= 2) {
                    Polyline(
                        points = points.map { LatLng(it.first, it.second) },
                        color = color,
                        width = with(density) { 4.dp.toPx() },
                        jointType = JointType.ROUND,
                        startCap = RoundCap(),
                        endCap = RoundCap(),
                    )
                }
            }
            trip.stops.forEach { stop ->
                key(stop.id) {
                    MarkerComposable(
                        stop.id, stop.isMyStop,
                        state = remember { MarkerState(position = LatLng(stop.latitude, stop.longitude)) },
                        title = if (stop.isMyStop) "Seu ponto · ${stop.title}" else stop.title,
                        snippet = stop.subtitle,
                        anchor = Offset(0.5f, 0.5f),
                        zIndex = if (stop.isMyStop) 2f else 1f,
                    ) { StopPin(stop.sequence + 1, stop.isMyStop) }
                }
            }
            if (destination != null) {
                MarkerComposable(
                    "destino",
                    state = remember { MarkerState(position = destination) },
                    title = trip.company.name,
                    snippet = trip.destination?.label,
                    anchor = Offset(0.5f, 0.5f),
                ) { DestinationPin() }
            }
            if (van != null) {
                val vanState = remember { MarkerState(position = van) }
                // Desliza da posição anterior até a nova durante o intervalo entre atualizações (~5 s).
                LaunchedEffect(van) {
                    val from = vanState.position
                    if (!isLive || haversineMeters(from, van) > 2_000) {
                        vanState.position = van
                    } else {
                        animate(0f, 1f, animationSpec = tween(4_500, easing = LinearEasing)) { t, _ ->
                            vanState.position = LatLng(
                                from.latitude + (van.latitude - from.latitude) * t,
                                from.longitude + (van.longitude - from.longitude) * t,
                            )
                        }
                    }
                }
                val heading = request.vanHeading()
                MarkerComposable(
                    "van", heading != null,
                    state = vanState,
                    title = trip.vehicle?.let { "${it.model} · ${it.plateNumber}" } ?: "Van",
                    snippet = if (isLive) "Ao vivo" else "Última posição informada",
                    anchor = Offset(0.5f, 0.5f),
                    flat = heading != null,
                    rotation = heading ?: 0f,
                    zIndex = 3f,
                ) { VanPin(showDirection = heading != null) }
            }
        }

        if (request.interactive && isLive && !following) {
            FilledIconButton(
                onClick = { following = true },
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 124.dp, end = 12.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = RcColors.Topbar, contentColor = RcColors.White),
            ) { Icon(Icons.Filled.Navigation, contentDescription = "Acompanhar a van") }
        }

        if (request.interactive && !hasLocation) {
            FilledIconButton(
                onClick = {
                    permissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 76.dp, end = 12.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = RcColors.Panel, contentColor = RcColors.Accent),
            ) { Icon(Icons.Filled.MyLocation, contentDescription = "Mostrar minha localização") }
        }
    }
}

// Pinos no mesmo estilo dos pontos do mapa do painel (.map-point: borda branca de 3px).

@Composable
private fun StopPin(number: Int, highlighted: Boolean) {
    Box(
        Modifier
            .size(if (highlighted) 34.dp else 28.dp)
            .background(Color.White, CircleShape)
            .padding(3.dp)
            .background(if (highlighted) RcColors.Warning else RcColors.Accent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$number",
            color = if (highlighted) RcColors.Background else Color.White,
            fontSize = if (highlighted) 14.sp else 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun DestinationPin() {
    Box(
        Modifier
            .size(30.dp)
            .background(Color.White, RoundedCornerShape(7.dp))
            .padding(3.dp)
            .background(RcColors.Topbar, RoundedCornerShape(5.dp)),
        contentAlignment = Alignment.Center,
    ) { Icon(Icons.Filled.Business, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) }
}

/** Van: seta (apontando para o norte; o marcador gira com a direção) ou ônibus sem direção. */
@Composable
private fun VanPin(showDirection: Boolean) {
    Box(
        Modifier
            .size(36.dp)
            .background(Color.White, CircleShape)
            .padding(3.dp)
            .background(RcColors.SuccessBg, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (showDirection) Icons.Filled.Navigation else Icons.Filled.DirectionsBus,
            contentDescription = null,
            tint = RcColors.Success,
            modifier = Modifier.size(20.dp),
        )
    }
}

private fun haversineMeters(a: LatLng, b: LatLng): Double {
    val r = 6_371_000.0
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLng = Math.toRadians(b.longitude - a.longitude)
    val h = Math.sin(dLat / 2).let { it * it } +
        Math.cos(Math.toRadians(a.latitude)) * Math.cos(Math.toRadians(b.latitude)) * Math.sin(dLng / 2).let { it * it }
    return 2 * r * Math.asin(Math.sqrt(h))
}

private val MANAUS = LatLng(-3.119, -60.021)

private fun hasLocationPermission(context: Context): Boolean =
    listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

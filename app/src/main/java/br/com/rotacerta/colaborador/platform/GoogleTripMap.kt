package br.com.rotacerta.colaborador.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.RoundCap
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
    val van = trip.lastPosition?.let { LatLng(it.latitude, it.longitude) }
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
            if (stops.size + listOfNotNull(destination).size >= 2) {
                Polyline(
                    points = stops + listOfNotNull(destination),
                    color = RcColors.Accent,
                    width = with(density) { 4.dp.toPx() },
                    jointType = JointType.ROUND,
                    startCap = RoundCap(),
                    endCap = RoundCap(),
                )
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
                LaunchedEffect(van) { vanState.position = van } // acompanha a atualização a cada 30 s
                MarkerComposable(
                    "van",
                    state = vanState,
                    title = trip.vehicle?.let { "${it.model} · ${it.plateNumber}" } ?: "Van",
                    snippet = "Última posição informada",
                    anchor = Offset(0.5f, 0.5f),
                    zIndex = 3f,
                ) { VanPin() }
            }
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

@Composable
private fun VanPin() {
    Box(
        Modifier
            .size(34.dp)
            .background(Color.White, CircleShape)
            .padding(3.dp)
            .background(RcColors.SuccessBg, CircleShape),
        contentAlignment = Alignment.Center,
    ) { Icon(Icons.Filled.DirectionsBus, contentDescription = null, tint = RcColors.Success, modifier = Modifier.size(18.dp)) }
}

private val MANAUS = LatLng(-3.119, -60.021)

private fun hasLocationPermission(context: Context): Boolean =
    listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

package com.rotacerta.mobile.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Item de GET /me/routes/: vínculo do colaborador (RoutePassenger) com uma rota. */
@Serializable
data class CollaboratorRouteDto(
    /** ID do vínculo (route_passenger_id), usado no PATCH de presença. */
    val id: Long,
    @SerialName("boarding_status") val boardingStatus: String,
    val route: CollaboratorRouteInfoDto,
    val stop: CollaboratorStopDto? = null,
    @SerialName("last_location") val lastLocation: VehicleLocationDto? = null,
    val schedules: List<ScheduleDto> = emptyList(),
    /** Previsão de chegada ao ponto calculada no backend, pelo trajeto das ruas (core/eta.py). */
    val eta: CollaboratorEtaDto? = null,
)

/** Fases de [CollaboratorEtaDto.phase]. */
object EtaPhase {
    const val WAITING = "waiting"
    const val APPROACHING = "approaching"
    const val ARRIVING = "arriving"
    const val PASSED = "passed"
    const val AT_DESTINATION = "at_destination"
}

@Serializable
data class CollaboratorEtaDto(
    val phase: String = EtaPhase.WAITING,
    @SerialName("is_live") val isLive: Boolean = false,
    val minutes: Int? = null,
    @SerialName("distance_meters") val distanceMeters: Int? = null,
    @SerialName("stops_before") val stopsBefore: Int? = null,
)

/** Resposta de GET me/routes/{id}/eta/. */
@Serializable
data class CollaboratorEtaResponseDto(
    @SerialName("route_passenger_id") val routePassengerId: Long,
    val eta: CollaboratorEtaDto,
)

@Serializable
data class CollaboratorRouteInfoDto(
    val id: Long,
    val name: String,
    @SerialName("company_name") val companyName: String,
    @SerialName("route_status") val routeStatus: String = RouteStatus.WAITING,
    val vehicle: CollaboratorVehicleDto? = null,
    @SerialName("driver_name") val driverName: String? = null,
)

@Serializable
data class CollaboratorVehicleDto(
    @SerialName("plate_number") val plateNumber: String,
    val model: String,
)

@Serializable
data class CollaboratorStopDto(
    val id: Long,
    val sequence: Int,
    val title: String,
    val subtitle: String,
    val latitude: Double,
    val longitude: Double,
)

/** Horário de operação da empresa: dias ("MON".."SUN") e hora "HH:mm". */
@Serializable
data class ScheduleDto(val weekdays: List<String> = emptyList(), val time: String)

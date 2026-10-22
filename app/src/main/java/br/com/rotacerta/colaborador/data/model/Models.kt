package br.com.rotacerta.colaborador.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---- Autenticação (/api/auth/*) ----

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class PersonBrief(
    val id: Long,
    val name: String,
    val type: String,
    val email: String? = null,
    @SerialName("company_id") val companyId: Long,
    @SerialName("company_name") val companyName: String,
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("must_change_password") val mustChangePassword: Boolean,
    val person: PersonBrief,
)

@Serializable
data class RefreshRequest(val refresh: String)

@Serializable
data class RefreshResponse(@SerialName("access_token") val accessToken: String)

@Serializable
data class ChangePasswordRequest(
    @SerialName("current_password") val currentPassword: String,
    @SerialName("new_password") val newPassword: String,
)

@Serializable
data class PasswordResetRequest(
    val email: String,
    val code: String? = null,
    @SerialName("new_password") val newPassword: String? = null,
)

@Serializable
data class PasswordResetResponse(
    val status: String? = null,
    val valid: Boolean? = null,
    /** Só vem preenchido com DEBUG/PASSWORD_RESET_RETURN_CODE_IN_RESPONSE no backend. */
    val verificationCode: String? = null,
)

// ---- Colaborador (/api/me/*) ----

@Serializable
data class AddressInfo(
    val id: Long,
    val label: String? = null,
    val latitude: Double,
    val longitude: Double,
)

@Serializable
data class EmployeeProfile(
    val id: Long,
    val name: String,
    val phone: String,
    @SerialName("employee_code") val employeeCode: String? = null,
    val email: String? = null,
    @SerialName("company_id") val companyId: Long,
    @SerialName("company_name") val companyName: String,
    @SerialName("home_address") val homeAddress: AddressInfo? = null,
)

@Serializable
data class Trip(
    val id: Long,
    @SerialName("boarding_status") val boardingStatus: BoardingStatus,
    val route: TripRoute,
    val company: CompanyRef,
    val vehicle: VehicleInfo? = null,
    val driver: DriverInfo? = null,
    @SerialName("my_stop") val myStop: StopInfo? = null,
    val stops: List<StopInfo> = emptyList(),
    val destination: AddressInfo? = null,
    /** Trajeto calculado pelo backend: pelas ruas (Google) ou linha reta entre as paradas. */
    val path: TripPath? = null,
    val schedules: List<ScheduleInfo> = emptyList(),
    @SerialName("last_position") val lastPosition: Position? = null,
    val events: List<TripEvent> = emptyList(),
)

@Serializable
data class TripRoute(val id: Long, val name: String, val status: RouteStatus)

@Serializable
data class CompanyRef(val id: Long, val name: String)

@Serializable
data class VehicleInfo(
    val id: Long,
    @SerialName("plate_number") val plateNumber: String,
    val model: String,
    val capacity: Int? = null,
)

@Serializable
data class DriverInfo(val id: Long, val name: String, val phone: String)

@Serializable
data class StopInfo(
    val id: Long,
    val sequence: Int,
    val title: String,
    val subtitle: String? = null,
    val latitude: Double,
    val longitude: Double,
    @SerialName("is_my_stop") val isMyStop: Boolean = false,
)

/** weekdays usa códigos MON..SUN; time no formato HH:mm. */
@Serializable
data class ScheduleInfo(val id: Long, val time: String, val weekdays: List<String>)

@Serializable
data class Position(val latitude: Double, val longitude: Double, val timestamp: String? = null)

@Serializable
data class TripEvent(
    val id: Long,
    @SerialName("event_type") val eventType: String,
    val description: String = "",
    val timestamp: String,
)

@Serializable
data class BoardingRequest(val status: BoardingStatus)

@Serializable
enum class BoardingStatus {
    @SerialName("pending") PENDING,
    @SerialName("confirmed") CONFIRMED,
    @SerialName("absent") ABSENT,
}

@Serializable
enum class RouteStatus {
    @SerialName("waiting") WAITING,
    @SerialName("in_route") IN_ROUTE,
    @SerialName("delayed") DELAYED,
}

// ---- Acompanhamento ao vivo (/api/me/trips/{id}/live/) ----

@Serializable
data class LiveStatus(
    @SerialName("trip_id") val tripId: Long,
    @SerialName("is_live") val isLive: Boolean = false,
    val phase: LivePhase = LivePhase.WAITING,
    val vehicle: LiveVehicle? = null,
    val eta: LiveEta? = null,
    @SerialName("next_stop_sequence") val nextStopSequence: Int? = null,
    @SerialName("path_progress") val pathProgress: PathProgress? = null,
)

@Serializable
data class LiveVehicle(
    val latitude: Double,
    val longitude: Double,
    val heading: Double? = null,
    val speed: Double? = null,
    @SerialName("age_seconds") val ageSeconds: Int = 0,
)

@Serializable
data class LiveEta(
    val minutes: Int,
    @SerialName("distance_meters") val distanceMeters: Int,
    @SerialName("stops_before") val stopsBefore: Int,
)

/** Trajeto da rota em polyline codificada (formato do Google, precisão 5). */
@Serializable
data class TripPath(
    val polyline: String,
    val source: String = "straight",
    val version: String? = null,
)

/** Onde a van está no trajeto: segmento e fração percorrida dele (no trajeto de versão [pathVersion]). */
@Serializable
data class PathProgress(
    val segment: Int,
    val fraction: Double,
    @SerialName("path_version") val pathVersion: String? = null,
)

@Serializable
enum class LivePhase {
    @SerialName("waiting") WAITING,
    @SerialName("approaching") APPROACHING,
    @SerialName("arriving") ARRIVING,
    @SerialName("passed") PASSED,
    @SerialName("at_destination") AT_DESTINATION,
}

package br.com.rotacerta.colaborador.data

import br.com.rotacerta.colaborador.data.model.BoardingRequest
import br.com.rotacerta.colaborador.data.model.BoardingStatus
import br.com.rotacerta.colaborador.data.model.ChangePasswordRequest
import br.com.rotacerta.colaborador.data.model.DriverRoute
import br.com.rotacerta.colaborador.data.model.EmployeeProfile
import br.com.rotacerta.colaborador.data.model.PreferencesRequest
import br.com.rotacerta.colaborador.data.model.LiveStatus
import br.com.rotacerta.colaborador.data.model.LocationRequest
import br.com.rotacerta.colaborador.data.model.LoginRequest
import br.com.rotacerta.colaborador.data.model.PasswordResetRequest
import br.com.rotacerta.colaborador.data.model.PasswordResetResponse
import br.com.rotacerta.colaborador.data.model.RouteEventRequest
import br.com.rotacerta.colaborador.data.model.Trip
import br.com.rotacerta.colaborador.data.remote.RotaCertaApi
import br.com.rotacerta.colaborador.data.remote.toUserMessage
import br.com.rotacerta.colaborador.data.session.SessionManager
import kotlinx.coroutines.CancellationException
import java.time.Instant
import java.util.Locale

class AppException(message: String) : Exception(message)

object PersonType {
    const val EMPLOYEE = "EMPLOYEE"
    const val DRIVER = "DRIVER"
}

private fun Double.coordinate(): String = String.format(Locale.US, "%.6f", this)

class RotaCertaRepository(
    private val api: RotaCertaApi,
    val session: SessionManager,
) {
    suspend fun login(email: String, password: String): Result<Unit> = call {
        val auth = api.login(LoginRequest(email.trim().lowercase(), password))
        // Colaborador e motorista usam este app, cada um com a sua área; o gestor usa o painel web.
        if (auth.person.type != PersonType.EMPLOYEE && auth.person.type != PersonType.DRIVER) {
            throw AppException("Gestores acessam o Rota Certa pelo painel web, no computador.")
        }
        session.onAuthenticated(auth)
    }

    suspend fun changePassword(current: String, new: String): Result<Unit> = call {
        session.onAuthenticated(api.changePassword(ChangePasswordRequest(current, new)))
    }

    suspend fun requestPasswordReset(email: String): Result<PasswordResetResponse> = call {
        api.requestPasswordReset(PasswordResetRequest(email = email.trim().lowercase()))
    }

    suspend fun completePasswordReset(email: String, code: String, newPassword: String): Result<Unit> = call {
        api.completePasswordReset(
            PasswordResetRequest(email = email.trim().lowercase(), code = code.trim(), newPassword = newPassword)
        )
        Unit
    }

    suspend fun profile(): Result<EmployeeProfile> = call { api.profile() }

    suspend fun setWhatsappOptIn(enabled: Boolean): Result<EmployeeProfile> = call {
        api.updatePreferences(PreferencesRequest(enabled))
    }

    suspend fun trips(): Result<List<Trip>> = call { api.trips() }

    suspend fun live(tripId: Long): Result<LiveStatus> = call { api.live(tripId) }

    suspend fun updateBoarding(tripId: Long, status: BoardingStatus): Result<Trip> = call {
        api.updateBoarding(tripId, BoardingRequest(status))
    }

    // ---- Motorista

    suspend fun myRoutes(): Result<List<DriverRoute>> = call { api.myRoutes() }

    suspend fun sendLocation(routeId: Long, fix: GpsFix): Result<Unit> = call {
        api.sendLocation(
            routeId,
            LocationRequest(
                latitude = fix.latitude.coordinate(),
                longitude = fix.longitude.coordinate(),
                heading = fix.heading,
                speed = fix.speedMps,
                recordedAt = Instant.ofEpochMilli(fix.timeMillis).toString(),
            ),
        )
    }

    suspend fun routeEvent(routeId: Long, type: String, description: String, fix: GpsFix): Result<Unit> = call {
        api.createRouteEvent(RouteEventRequest(routeId, type, description, fix.latitude.coordinate(), fix.longitude.coordinate()))
    }

    fun logout() = session.logout()

    private inline fun <T> call(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: AppException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(AppException(e.toUserMessage()))
    }
}

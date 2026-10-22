package br.com.rotacerta.colaborador.data

import br.com.rotacerta.colaborador.data.model.BoardingRequest
import br.com.rotacerta.colaborador.data.model.BoardingStatus
import br.com.rotacerta.colaborador.data.model.ChangePasswordRequest
import br.com.rotacerta.colaborador.data.model.EmployeeProfile
import br.com.rotacerta.colaborador.data.model.LiveStatus
import br.com.rotacerta.colaborador.data.model.LoginRequest
import br.com.rotacerta.colaborador.data.model.PasswordResetRequest
import br.com.rotacerta.colaborador.data.model.PasswordResetResponse
import br.com.rotacerta.colaborador.data.model.Trip
import br.com.rotacerta.colaborador.data.remote.RotaCertaApi
import br.com.rotacerta.colaborador.data.remote.toUserMessage
import br.com.rotacerta.colaborador.data.session.SessionManager
import kotlinx.coroutines.CancellationException

class AppException(message: String) : Exception(message)

class RotaCertaRepository(
    private val api: RotaCertaApi,
    val session: SessionManager,
) {
    suspend fun login(email: String, password: String): Result<Unit> = call {
        val auth = api.login(LoginRequest(email.trim().lowercase(), password))
        if (auth.person.type != "EMPLOYEE") {
            throw AppException("Este app é exclusivo para colaboradores. Motoristas usam o app do motorista.")
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

    suspend fun trips(): Result<List<Trip>> = call { api.trips() }

    suspend fun live(tripId: Long): Result<LiveStatus> = call { api.live(tripId) }

    suspend fun updateBoarding(tripId: Long, status: BoardingStatus): Result<Trip> = call {
        api.updateBoarding(tripId, BoardingRequest(status))
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

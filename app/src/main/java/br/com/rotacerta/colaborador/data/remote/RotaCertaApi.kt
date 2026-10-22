package br.com.rotacerta.colaborador.data.remote

import br.com.rotacerta.colaborador.data.model.AuthResponse
import br.com.rotacerta.colaborador.data.model.BoardingRequest
import br.com.rotacerta.colaborador.data.model.ChangePasswordRequest
import br.com.rotacerta.colaborador.data.model.EmployeeProfile
import br.com.rotacerta.colaborador.data.model.LiveStatus
import br.com.rotacerta.colaborador.data.model.LoginRequest
import br.com.rotacerta.colaborador.data.model.PasswordResetRequest
import br.com.rotacerta.colaborador.data.model.PasswordResetResponse
import br.com.rotacerta.colaborador.data.model.RefreshRequest
import br.com.rotacerta.colaborador.data.model.RefreshResponse
import br.com.rotacerta.colaborador.data.model.Trip
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Endpoints do backend Django (prefixo /api/ vem da URL do servidor configurada). */
interface RotaCertaApi {
    @POST("auth/login/")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    /** Síncrono de propósito: usado dentro do OkHttp Authenticator. */
    @POST("auth/refresh/")
    fun refresh(@Body body: RefreshRequest): Call<RefreshResponse>

    @POST("auth/change-password/")
    suspend fun changePassword(@Body body: ChangePasswordRequest): AuthResponse

    @POST("auth/password-reset/request/")
    suspend fun requestPasswordReset(@Body body: PasswordResetRequest): PasswordResetResponse

    @POST("auth/password-reset/reset/")
    suspend fun completePasswordReset(@Body body: PasswordResetRequest): PasswordResetResponse

    @GET("me/")
    suspend fun profile(): EmployeeProfile

    @GET("me/trips/")
    suspend fun trips(): List<Trip>

    @GET("me/trips/{id}/live/")
    suspend fun live(@Path("id") tripId: Long): LiveStatus

    @POST("me/trips/{id}/boarding/")
    suspend fun updateBoarding(@Path("id") tripId: Long, @Body body: BoardingRequest): Trip
}

package com.rotacerta.mobile.data.api

import com.rotacerta.mobile.data.dto.AddressPreviewDto
import com.rotacerta.mobile.data.dto.AddressPreviewRequest
import com.rotacerta.mobile.data.dto.AuthSessionResponse
import com.rotacerta.mobile.data.dto.BoardingStatusRequest
import com.rotacerta.mobile.data.dto.CepDto
import com.rotacerta.mobile.data.dto.ChangePasswordRequest
import com.rotacerta.mobile.data.dto.CollaboratorEtaResponseDto
import com.rotacerta.mobile.data.dto.CollaboratorRouteDto
import com.rotacerta.mobile.data.dto.DriverRouteDto
import com.rotacerta.mobile.data.dto.LocationUpdateRequest
import com.rotacerta.mobile.data.dto.LoginRequest
import com.rotacerta.mobile.data.dto.MyAddressDto
import com.rotacerta.mobile.data.dto.NavigationResponseDto
import com.rotacerta.mobile.data.dto.NotificationDto
import com.rotacerta.mobile.data.dto.NotificationPageDto
import com.rotacerta.mobile.data.dto.PasswordResetCompletePayload
import com.rotacerta.mobile.data.dto.PasswordResetIdentifier
import com.rotacerta.mobile.data.dto.PasswordResetRequestResponse
import com.rotacerta.mobile.data.dto.PasswordResetVerifyPayload
import com.rotacerta.mobile.data.dto.PasswordResetVerifyResponse
import com.rotacerta.mobile.data.dto.ReadAllResponse
import com.rotacerta.mobile.data.dto.RouteEventCreateRequest
import com.rotacerta.mobile.data.dto.RouteEventDto
import com.rotacerta.mobile.data.dto.SaveAddressRequest
import com.rotacerta.mobile.data.dto.StatusResponse
import com.rotacerta.mobile.data.dto.VehicleLocationDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @POST("auth/login/")
    suspend fun login(@Body body: LoginRequest): Response<AuthSessionResponse>

    @POST("auth/change-password/")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<AuthSessionResponse>

    @POST("auth/password-reset/request/")
    suspend fun requestPasswordReset(@Body body: PasswordResetIdentifier): Response<PasswordResetRequestResponse>

    @POST("auth/password-reset/verify/")
    suspend fun verifyPasswordReset(@Body body: PasswordResetVerifyPayload): Response<PasswordResetVerifyResponse>

    @POST("auth/password-reset/reset/")
    suspend fun completePasswordReset(@Body body: PasswordResetCompletePayload): Response<StatusResponse>
}

/** Endpoints do perfil motorista (RouteViewSet e afins). */
interface DriverApi {
    @GET("routes/my-routes/")
    suspend fun myRoutes(): Response<List<DriverRouteDto>>

    @GET("routes/{id}/driver-detail/")
    suspend fun routeDetail(@Path("id") routeId: Long): Response<DriverRouteDto>

    @GET("routes/{id}/events/")
    suspend fun routeEvents(@Path("id") routeId: Long): Response<List<RouteEventDto>>

    @GET("routes/{id}/navigation/")
    suspend fun navigation(
        @Path("id") routeId: Long,
        /** "lat,lng" da posição atual; null = parte do endereço do motorista. */
        @Query("origin") origin: String? = null,
        /** IDs das paradas já atendidas, separados por vírgula. */
        @Query("done_stops") doneStops: String? = null,
    ): Response<NavigationResponseDto>

    @POST("routes/{id}/location/")
    suspend fun sendLocation(
        @Path("id") routeId: Long,
        @Body body: LocationUpdateRequest,
    ): Response<VehicleLocationDto>

    @POST("route-events/")
    suspend fun createEvent(@Body body: RouteEventCreateRequest): Response<RouteEventDto>

    @PATCH("route-passengers/{id}/")
    suspend fun updateBoardingStatus(
        @Path("id") routePassengerId: Long,
        @Body body: BoardingStatusRequest,
    ): Response<Unit>
}

/** Endpoints do perfil colaborador (CollaboratorRouteViewSet). */
interface CollaboratorApi {
    @GET("me/routes/")
    suspend fun myRoutes(): Response<List<CollaboratorRouteDto>>

    @PATCH("me/routes/{id}/")
    suspend fun updateBoardingStatus(
        @Path("id") routePassengerId: Long,
        @Body body: BoardingStatusRequest,
    ): Response<CollaboratorRouteDto>

    /** Previsão de chegada ao ponto pelo trajeto das ruas, recalculada a cada posição do ônibus. */
    @GET("me/routes/{id}/eta/")
    suspend fun eta(@Path("id") routePassengerId: Long): Response<CollaboratorEtaResponseDto>
}

/** Endereço residencial do próprio usuário (validado no backend antes de gravar). */
interface AddressApi {
    @GET("me/address/")
    suspend fun current(): Response<MyAddressDto>

    @POST("me/address/preview/")
    suspend fun preview(@Body body: AddressPreviewRequest): Response<AddressPreviewDto>

    @PUT("me/address/")
    suspend fun save(@Body body: SaveAddressRequest): Response<MyAddressDto>

    @GET("me/address/cep/{cep}/")
    suspend fun cep(@Path("cep") cep: String): Response<CepDto>
}

interface NotificationApi {
    @GET("notifications/")
    suspend fun list(
        @Query("page") page: Int = 1,
        @Query("unread") unread: Boolean? = null,
    ): Response<NotificationPageDto>

    @POST("notifications/{id}/read/")
    suspend fun markRead(@Path("id") id: Long): Response<NotificationDto>

    @POST("notifications/read-all/")
    suspend fun markAllRead(): Response<ReadAllResponse>
}

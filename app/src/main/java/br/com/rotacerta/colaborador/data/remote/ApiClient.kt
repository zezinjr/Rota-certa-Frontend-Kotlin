package br.com.rotacerta.colaborador.data.remote

import br.com.rotacerta.colaborador.data.model.RefreshRequest
import br.com.rotacerta.colaborador.data.session.SessionManager
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

val AppJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
}

/**
 * Monta o Retrofit. A URL real do servidor é resolvida a cada requisição
 * ([ServerUrlInterceptor]) para permitir trocar o IP do backend pela tela de login.
 */
object ApiClient {
    private const val PLACEHOLDER_BASE = "http://rotacerta.local/"

    fun create(session: SessionManager, debugLogging: Boolean = false): RotaCertaApi {
        val json = "application/json".toMediaType()

        // Cliente "cru" só para renovar token (sem Authenticator, evita recursão).
        val refreshApi = Retrofit.Builder()
            .baseUrl(PLACEHOLDER_BASE)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(ServerUrlInterceptor(session))
                    .build()
            )
            .addConverterFactory(AppJson.asConverterFactory(json))
            .build()
            .create(RotaCertaApi::class.java)

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(ServerUrlInterceptor(session))
            .addInterceptor(AuthHeaderInterceptor(session))
            .authenticator(TokenAuthenticator(session, refreshApi))
            .apply {
                if (debugLogging) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(PLACEHOLDER_BASE)
            .client(client)
            .addConverterFactory(AppJson.asConverterFactory(json))
            .build()
            .create(RotaCertaApi::class.java)
    }
}

private class ServerUrlInterceptor(private val session: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val relative = buildString {
            append(original.url.encodedPath.removePrefix("/"))
            original.url.encodedQuery?.let { append('?').append(it) }
        }
        val target = session.serverUrl.toHttpUrl().resolve(relative)
            ?: return chain.proceed(original)
        return chain.proceed(original.newBuilder().url(target).build())
    }
}

private class AuthHeaderInterceptor(private val session: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = session.accessToken
        if (token == null || request.url.encodedPath.contains("/auth/") && !request.isChangePassword()) {
            return chain.proceed(request)
        }
        return chain.proceed(request.newBuilder().header("Authorization", "Bearer $token").build())
    }
}

/** Em 401, tenta renovar o access token uma vez; se falhar, encerra a sessão. */
private class TokenAuthenticator(
    private val session: SessionManager,
    private val refreshApi: RotaCertaApi,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val request = response.request
        if (request.url.encodedPath.contains("/auth/") && !request.isChangePassword()) return null
        if (response.priorResponse != null) return null // já tentamos renovar

        val refresh = session.refreshToken ?: return null
        synchronized(this) {
            // Outra requisição pode já ter renovado enquanto esperávamos o lock.
            val current = session.accessToken
            val sent = request.header("Authorization")?.removePrefix("Bearer ")
            if (current != null && current != sent) {
                return request.newBuilder().header("Authorization", "Bearer $current").build()
            }
            val renewed = runCatching { refreshApi.refresh(RefreshRequest(refresh)).execute() }
                .getOrNull()
                ?.takeIf { it.isSuccessful }
                ?.body()
            if (renewed == null) {
                session.logout()
                return null
            }
            session.onAccessTokenRefreshed(renewed.accessToken)
            return request.newBuilder().header("Authorization", "Bearer ${renewed.accessToken}").build()
        }
    }
}

private fun Request.isChangePassword() = url.encodedPath.endsWith("/auth/change-password/")

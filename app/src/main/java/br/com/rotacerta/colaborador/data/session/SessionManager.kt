package br.com.rotacerta.colaborador.data.session

import br.com.rotacerta.colaborador.data.model.AuthResponse
import br.com.rotacerta.colaborador.data.model.PersonBrief
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface SessionState {
    data object LoggedOut : SessionState
    data class PasswordChangeRequired(val person: PersonBrief) : SessionState
    data class LoggedIn(val person: PersonBrief) : SessionState
}

/** Fonte única da verdade sobre "quem está logado"; a navegação reage a [state]. */
class SessionManager(private val store: SessionStore, defaultServerUrl: String) {

    private val _state = MutableStateFlow(restore())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    val defaultServerUrl: String = withTrailingSlash(defaultServerUrl)
    val serverUrl: String get() = store.serverUrl ?: defaultServerUrl
    val accessToken: String? get() = store.accessToken
    val refreshToken: String? get() = store.refreshToken

    fun updateServerUrl(url: String) {
        store.serverUrl = withTrailingSlash(url)
    }

    // Sem a barra final, "…/api" + "me/trips/" viraria "…/me/trips/" (perdendo o /api).
    private fun withTrailingSlash(url: String): String = url.trim().let { if (it.endsWith("/")) it else "$it/" }

    fun onAuthenticated(auth: AuthResponse) {
        store.accessToken = auth.accessToken
        store.refreshToken = auth.refreshToken
        store.mustChangePassword = auth.mustChangePassword
        store.person = auth.person
        _state.value = restore()
    }

    fun onAccessTokenRefreshed(token: String) {
        store.accessToken = token
    }

    fun logout() {
        store.clearSession()
        _state.value = SessionState.LoggedOut
    }

    private fun restore(): SessionState {
        val person = store.person
        return when {
            person == null || store.refreshToken == null -> SessionState.LoggedOut
            store.mustChangePassword -> SessionState.PasswordChangeRequired(person)
            else -> SessionState.LoggedIn(person)
        }
    }
}

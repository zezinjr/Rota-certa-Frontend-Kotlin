package br.com.rotacerta.colaborador.data

import br.com.rotacerta.colaborador.data.remote.ApiClient
import br.com.rotacerta.colaborador.data.session.SessionManager
import br.com.rotacerta.colaborador.data.session.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow

/** Injeção de dependência manual (simples o suficiente para o protótipo). */
class AppContainer(store: SessionStore, defaultServerUrl: String, debugLogging: Boolean = false) {
    val session = SessionManager(store, defaultServerUrl)
    val repository = RotaCertaRepository(ApiClient.create(session, debugLogging), session)

    /** App visível na tela? A MainActivity atualiza; as consultas periódicas pausam em segundo plano. */
    val appInForeground = MutableStateFlow(true)
}

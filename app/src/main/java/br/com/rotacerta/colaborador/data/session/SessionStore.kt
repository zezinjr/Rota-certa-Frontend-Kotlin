package br.com.rotacerta.colaborador.data.session

import br.com.rotacerta.colaborador.data.model.PersonBrief

/** Armazenamento persistente da sessão (Android: SharedPreferences). */
interface SessionStore {
    var serverUrl: String?
    var accessToken: String?
    var refreshToken: String?
    var mustChangePassword: Boolean
    var person: PersonBrief?

    fun clearSession() {
        accessToken = null
        refreshToken = null
        mustChangePassword = false
        person = null
    }
}

class InMemorySessionStore(override var serverUrl: String? = null) : SessionStore {
    override var accessToken: String? = null
    override var refreshToken: String? = null
    override var mustChangePassword: Boolean = false
    override var person: PersonBrief? = null
}

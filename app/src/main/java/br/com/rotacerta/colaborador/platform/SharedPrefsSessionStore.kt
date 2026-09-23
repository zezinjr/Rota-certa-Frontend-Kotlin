package br.com.rotacerta.colaborador.platform

import android.content.Context
import br.com.rotacerta.colaborador.data.model.PersonBrief
import br.com.rotacerta.colaborador.data.remote.AppJson
import br.com.rotacerta.colaborador.data.session.SessionStore

/**
 * Sessão persistida em SharedPreferences (modo privado do app).
 * Para produção, considere EncryptedSharedPreferences / DataStore + Keystore.
 */
class SharedPrefsSessionStore(context: Context) : SessionStore {
    private val prefs = context.getSharedPreferences("rota_certa_session", Context.MODE_PRIVATE)

    override var serverUrl: String?
        get() = prefs.getString(KEY_SERVER, null)
        set(value) = prefs.edit().putString(KEY_SERVER, value).apply()

    override var accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)
        set(value) = prefs.edit().putString(KEY_ACCESS, value).apply()

    override var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)
        set(value) = prefs.edit().putString(KEY_REFRESH, value).apply()

    override var mustChangePassword: Boolean
        get() = prefs.getBoolean(KEY_MUST_CHANGE, false)
        set(value) = prefs.edit().putBoolean(KEY_MUST_CHANGE, value).apply()

    override var person: PersonBrief?
        get() = prefs.getString(KEY_PERSON, null)?.let {
            runCatching { AppJson.decodeFromString(PersonBrief.serializer(), it) }.getOrNull()
        }
        set(value) = prefs.edit()
            .putString(KEY_PERSON, value?.let { AppJson.encodeToString(PersonBrief.serializer(), it) })
            .apply()

    private companion object {
        const val KEY_SERVER = "server_url"
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_MUST_CHANGE = "must_change_password"
        const val KEY_PERSON = "person"
    }
}

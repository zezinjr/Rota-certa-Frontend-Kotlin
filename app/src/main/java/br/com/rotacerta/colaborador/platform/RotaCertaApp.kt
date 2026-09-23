package br.com.rotacerta.colaborador.platform

import android.app.Application
import br.com.rotacerta.colaborador.BuildConfig
import br.com.rotacerta.colaborador.data.AppContainer

class RotaCertaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(
            store = SharedPrefsSessionStore(this),
            defaultServerUrl = BuildConfig.API_BASE_URL,
            debugLogging = BuildConfig.DEBUG,
        )
    }
}

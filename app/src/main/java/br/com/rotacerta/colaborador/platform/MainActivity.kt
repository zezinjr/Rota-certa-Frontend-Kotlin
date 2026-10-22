package br.com.rotacerta.colaborador.platform

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import br.com.rotacerta.colaborador.BuildConfig
import br.com.rotacerta.colaborador.ui.navigation.RotaCertaRoot
import br.com.rotacerta.colaborador.ui.theme.RotaCertaTheme
import br.com.rotacerta.colaborador.ui.trip.LocalTripMap

class MainActivity : ComponentActivity(), PlatformActions {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as RotaCertaApp).container
        setContent {
            RotaCertaTheme {
                Surface(Modifier.fillMaxSize().safeDrawingPadding(), color = MaterialTheme.colorScheme.background) {
                    if (BuildConfig.MAPS_ENABLED) {
                        // Com chave configurada, as telas usam o Google Maps no lugar do mapa esquemático.
                        CompositionLocalProvider(LocalTripMap provides GoogleTripMapRenderer) {
                            RotaCertaRoot(container, this@MainActivity)
                        }
                    } else {
                        RotaCertaRoot(container, this@MainActivity)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (application as RotaCertaApp).container.appInForeground.value = true
    }

    override fun onStop() {
        (application as RotaCertaApp).container.appInForeground.value = false
        super.onStop()
    }

    override fun dial(phone: String) {
        launch(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    }

    override fun openMap(latitude: Double, longitude: Double, label: String) {
        // Rota a pé até o ponto no app do Google Maps (ou no navegador, se não houver o app).
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude&travelmode=walking")
        launch(Intent(Intent.ACTION_VIEW, uri))
    }

    private fun launch(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Nenhum app disponível para esta ação.", Toast.LENGTH_SHORT).show()
        }
    }
}

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
import androidx.compose.ui.Modifier
import br.com.rotacerta.colaborador.ui.navigation.RotaCertaRoot
import br.com.rotacerta.colaborador.ui.theme.RotaCertaTheme

class MainActivity : ComponentActivity(), PlatformActions {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as RotaCertaApp).container
        setContent {
            RotaCertaTheme {
                Surface(Modifier.fillMaxSize().safeDrawingPadding(), color = MaterialTheme.colorScheme.background) {
                    RotaCertaRoot(container, this@MainActivity)
                }
            }
        }
    }

    override fun dial(phone: String) {
        launch(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    }

    override fun openMap(latitude: Double, longitude: Double, label: String) {
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})")
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

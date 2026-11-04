package br.com.rotacerta.colaborador.platform

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import br.com.rotacerta.colaborador.BuildConfig
import br.com.rotacerta.colaborador.ui.navigation.RotaCertaRoot
import br.com.rotacerta.colaborador.ui.theme.RotaCertaTheme
import br.com.rotacerta.colaborador.ui.trip.LocalTripMap

class MainActivity : ComponentActivity(), PlatformActions {

    /** Rota que espera a resposta do pedido de permissão para começar a enviar o GPS. */
    private var pendingRoute: Pair<Long, String>? = null

    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        val route = pendingRoute
        pendingRoute = null
        if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true && route != null) {
            LocationTrackingService.start(this, route.first, route.second)
        } else if (route != null) {
            Toast.makeText(this, "Sem a permissão de localização, a rota não pode ser acompanhada.", Toast.LENGTH_LONG).show()
        }
    }

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

    override fun startTracking(routeId: Long, routeName: String) {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        // Android 13+: sem a permissão de notificação, a notificação fixa da rota não aparece.
        val needsNotification = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (granted && !needsNotification) {
            LocationTrackingService.start(this, routeId, routeName)
            return
        }
        pendingRoute = routeId to routeName
        val permissions = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
        }
        locationPermission.launch(permissions.toTypedArray())
    }

    override fun stopTracking() = LocationTrackingService.stop(this)

    private fun launch(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Nenhum app disponível para esta ação.", Toast.LENGTH_SHORT).show()
        }
    }
}

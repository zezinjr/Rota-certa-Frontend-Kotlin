package br.com.rotacerta.colaborador.platform

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import br.com.rotacerta.colaborador.data.GpsFix
import br.com.rotacerta.colaborador.data.LOCATION_INTERVAL_MILLIS
import br.com.rotacerta.colaborador.data.shouldSend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Envia o GPS do motorista ao backend a cada 5 s enquanto a rota está em andamento.
 * Roda como serviço em primeiro plano (com notificação fixa), então continua com a tela
 * apagada ou com outro app aberto, ao contrário da página de rastreamento no navegador.
 */
class LocationTrackingService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val container get() = (application as RotaCertaApp).container
    private var locationManager: LocationManager? = null
    private var routeId = -1L
    private var departureSent = false
    private var stopping = false

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) = onFix(location.toFix())
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {
            if (provider == LocationManager.GPS_PROVIDER) setError("O GPS do celular foi desligado. Ative a localização.")
        }

        @Deprecated("Exigido em versões antigas do Android")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start(intent.getLongExtra(EXTRA_ROUTE_ID, -1L), intent.getStringExtra(EXTRA_ROUTE_NAME).orEmpty())
            ACTION_STOP -> stop()
        }
        return START_NOT_STICKY
    }

    private fun start(id: Long, name: String) {
        if (id < 0) return stopSelf()
        ServiceCompat.startForeground(
            this, NOTIFICATION_ID, notification(name),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0,
        )
        if (routeId == id) return // já está enviando esta rota
        routeId = id
        departureSent = false
        container.tracking.value = container.tracking.value.copy(
            active = true, routeId = id, routeName = name, sent = 0, lastSentAtMillis = null, lastFix = null, error = null,
        )
        requestUpdates()
    }

    @SuppressLint("MissingPermission") // conferida logo abaixo
    private fun requestUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            setError("Permita o acesso à localização para iniciar a rota.")
            return finish()
        }
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager = manager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            setError("O GPS do celular está desligado. Ative a localização.")
        }
        for (provider in listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)) {
            if (manager.allProviders.contains(provider)) {
                manager.requestLocationUpdates(provider, LOCATION_INTERVAL_MILLIS, 0f, listener, Looper.getMainLooper())
            }
        }
    }

    private fun onFix(fix: GpsFix) {
        val repository = container.repository
        val state = container.tracking.value
        container.tracking.update { it.copy(lastFix = fix) }
        if (stopping || routeId < 0) return
        if (!departureSent) {
            departureSent = true
            scope.launch { repository.routeEvent(routeId, "departure", "Rota iniciada pelo motorista", fix) }
        }
        val now = System.currentTimeMillis()
        if (!shouldSend(state.lastSentAtMillis, now)) return
        container.tracking.update { it.copy(lastSentAtMillis = now) }
        val id = routeId
        scope.launch {
            repository.sendLocation(id, fix)
                .onSuccess { container.tracking.update { it.copy(sent = it.sent + 1, error = null) } }
                .onFailure { e -> setError(e.message ?: "Falha ao enviar a localização. Tentando de novo.") }
        }
    }

    /** Registra a chegada com a última posição e encerra o serviço. */
    private fun stop() {
        if (stopping) return
        stopping = true
        locationManager?.removeUpdates(listener)
        val fix = container.tracking.value.lastFix
        val id = routeId
        scope.launch {
            if (fix != null && id >= 0) container.repository.routeEvent(id, "arrival", "Rota encerrada pelo motorista", fix)
            finish()
        }
    }

    private fun finish() {
        container.tracking.update { it.copy(active = false) }
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun setError(message: String) = container.tracking.update { it.copy(error = message) }

    override fun onDestroy() {
        locationManager?.removeUpdates(listener)
        container.tracking.update { it.copy(active = false) }
        scope.cancel()
        super.onDestroy()
    }

    private fun notification(routeName: String): android.app.Notification {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Rota em andamento", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Aparece enquanto o app envia a localização da van."
                },
            )
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), flags)
        val stop = PendingIntent.getService(this, 1, Intent(this, LocationTrackingService::class.java).setAction(ACTION_STOP), flags)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Rota em andamento")
            .setContentText("$routeName · enviando a localização da van")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(open)
            .addAction(0, "Encerrar rota", stop)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "rastreamento"
        private const val NOTIFICATION_ID = 42
        private const val ACTION_START = "br.com.rotacerta.colaborador.START_TRACKING"
        private const val ACTION_STOP = "br.com.rotacerta.colaborador.STOP_TRACKING"
        private const val EXTRA_ROUTE_ID = "route_id"
        private const val EXTRA_ROUTE_NAME = "route_name"

        fun start(context: Context, routeId: Long, routeName: String) {
            val intent = Intent(context, LocationTrackingService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_ROUTE_ID, routeId)
                .putExtra(EXTRA_ROUTE_NAME, routeName)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.startService(Intent(context, LocationTrackingService::class.java).setAction(ACTION_STOP))
        }
    }
}

private fun Location.toFix() = GpsFix(
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = if (hasAccuracy()) accuracy else null,
    speedMps = if (hasSpeed()) speed.toDouble() else null,
    heading = if (hasBearing()) bearing.toDouble().coerceIn(0.0, 360.0) else null,
    timeMillis = time,
)

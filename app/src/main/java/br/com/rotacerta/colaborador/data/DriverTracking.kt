package br.com.rotacerta.colaborador.data

/** Uma leitura do GPS do celular do motorista. */
data class GpsFix(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?,
    val speedMps: Double?,
    val heading: Double?,
    val timeMillis: Long,
)

/**
 * Situação do envio da localização, compartilhada entre o serviço em segundo plano
 * (que envia o GPS) e a tela do motorista (que mostra o andamento).
 */
data class TrackingState(
    val active: Boolean = false,
    val routeId: Long? = null,
    val routeName: String? = null,
    val sent: Int = 0,
    val lastSentAtMillis: Long? = null,
    val lastFix: GpsFix? = null,
    val error: String? = null,
)

/** Intervalo entre envios, o mesmo da página de rastreamento. */
const val LOCATION_INTERVAL_MILLIS = 5_000L

/** Decide se uma leitura nova deve ser enviada (evita mandar mais de uma a cada [LOCATION_INTERVAL_MILLIS]). */
fun shouldSend(lastSentAtMillis: Long?, nowMillis: Long, intervalMillis: Long = LOCATION_INTERVAL_MILLIS): Boolean =
    lastSentAtMillis == null || nowMillis - lastSentAtMillis >= intervalMillis - 500

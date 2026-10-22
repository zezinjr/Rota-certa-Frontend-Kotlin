package br.com.rotacerta.colaborador.data

/**
 * Decodifica uma polyline no formato do Google (precisão 5), usada pelo backend para
 * enviar o trajeto da rota pelas ruas. Retorna pares (latitude, longitude).
 */
fun decodePolyline(encoded: String, precision: Int = 5): List<Pair<Double, Double>> {
    val factor = Math.pow(10.0, precision.toDouble())
    val points = ArrayList<Pair<Double, Double>>(encoded.length / 4)
    var index = 0
    var lat = 0
    var lng = 0
    fun next(): Int {
        var shift = 0
        var result = 0
        while (true) {
            val b = encoded[index++].code - 63
            result = result or ((b and 0x1F) shl shift)
            shift += 5
            if (b < 0x20) break
        }
        return if (result and 1 != 0) (result shr 1).inv() else result shr 1
    }
    while (index < encoded.length) {
        lat += next()
        lng += next()
        points.add(lat / factor to lng / factor)
    }
    return points
}

/** O mesmo trajeto é desenhado a cada recomposição; guarda os últimos decodificados. */
private val decodedCache = object : LinkedHashMap<String, List<Pair<Double, Double>>>(8, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<Pair<Double, Double>>>?) = size > 8
}

fun decodePolylineCached(encoded: String): List<Pair<Double, Double>> =
    synchronized(decodedCache) { decodedCache.getOrPut(encoded) { decodePolyline(encoded) } }

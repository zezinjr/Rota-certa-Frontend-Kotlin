package br.com.rotacerta.colaborador.platform

/** Ações que dependem do sistema (discador, app de mapas, GPS em segundo plano). */
interface PlatformActions {
    fun dial(phone: String)
    /** Abre o app do Google Maps com a rota a pé até o ponto. */
    fun openMap(latitude: Double, longitude: Double, label: String)
    /** Motorista: pede a permissão de localização (se preciso) e começa a enviar o GPS da rota. */
    fun startTracking(routeId: Long, routeName: String)
    /** Motorista: registra a chegada e para de enviar o GPS. */
    fun stopTracking()
}

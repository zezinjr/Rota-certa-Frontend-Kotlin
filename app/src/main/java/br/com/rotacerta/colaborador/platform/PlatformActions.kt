package br.com.rotacerta.colaborador.platform

/** Ações que dependem do sistema (discador, app de mapas). */
interface PlatformActions {
    fun dial(phone: String)
    /** Abre o app do Google Maps com a rota a pé até o ponto. */
    fun openMap(latitude: Double, longitude: Double, label: String)
}

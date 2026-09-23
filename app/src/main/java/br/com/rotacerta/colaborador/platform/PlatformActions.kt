package br.com.rotacerta.colaborador.platform

/** Ações que dependem do sistema (discador, app de mapas). */
interface PlatformActions {
    fun dial(phone: String)
    fun openMap(latitude: Double, longitude: Double, label: String)
}

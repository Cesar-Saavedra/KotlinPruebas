package com.example.appnombre_grupo1.model.repository

// Envía los registros pendientes y descarga del servidor la versión actual de todo
class SincronizacionRepository(
    private val usuarioRepository: UsuarioRepository,
    private val clienteRepository: ClienteRepository,
    private val registroRepository: RegistroRepository,
) {
    // true si se pudo sincronizar todo
    suspend fun sincronizar(): Boolean {
        val subidos = registroRepository.subirPendientes()
        val usuarios = usuarioRepository.descargar()
        val clientes = clienteRepository.descargar()
        val registros = registroRepository.descargar()
        return subidos && usuarios && clientes && registros
    }
}

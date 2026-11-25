package com.example.appnombre_grupo1.model.repository

import com.example.appnombre_grupo1.model.Cliente
import com.example.appnombre_grupo1.model.database.dao.ClienteDao
import com.example.appnombre_grupo1.model.database.entity.ClienteEntity
import com.example.appnombre_grupo1.model.database.toModel
import com.example.appnombre_grupo1.model.remote.ApiService
import com.example.appnombre_grupo1.model.remote.MENSAJE_SIN_CONEXION
import com.example.appnombre_grupo1.model.remote.RespuestaApi
import com.example.appnombre_grupo1.model.remote.llamarApi
import com.example.appnombre_grupo1.model.remote.toDto
import com.example.appnombre_grupo1.model.remote.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Clientes: se crean y editan en el servidor; Room guarda la copia que usan las pantallas
class ClienteRepository(private val dao: ClienteDao, private val api: ApiService) {

    val clientes: Flow<List<Cliente>> = dao.observarClientes().map { lista -> lista.map { it.toModel() } }

    // Devuelve null si se guardó, o el mensaje de error
    suspend fun guardar(cliente: Cliente): String? {
        val respuesta = if (cliente.id.isBlank()) {
            llamarApi { api.crearCliente(cliente.toDto()) }
        } else {
            llamarApi { api.editarCliente(cliente.id, cliente.toDto()) }
        }
        return when (respuesta) {
            is RespuestaApi.Exito -> { guardarLocal(respuesta.datos.toModel()); null }
            is RespuestaApi.Error -> respuesta.mensaje
            RespuestaApi.SinConexion -> MENSAJE_SIN_CONEXION
        }
    }

    suspend fun eliminar(id: String): String? =
        when (val respuesta = llamarApi { api.eliminarCliente(id) }) {
            is RespuestaApi.Exito -> { dao.eliminarCliente(id); null }
            is RespuestaApi.Error -> respuesta.mensaje
            RespuestaApi.SinConexion -> MENSAJE_SIN_CONEXION
        }

    // Descarga los clientes del servidor. false si no hubo conexión
    suspend fun descargar(): Boolean {
        val respuesta = llamarApi { api.obtenerClientes() }
        if (respuesta !is RespuestaApi.Exito) return false
        dao.reemplazarTodos(respuesta.datos.map { it.toModel() }.map { it.aEntidad() to it.instalacionesAGuardar() })
        return true
    }

    private suspend fun guardarLocal(cliente: Cliente) {
        dao.guardarConInstalaciones(cliente.aEntidad(), cliente.instalacionesAGuardar())
    }

    private fun Cliente.aEntidad() = ClienteEntity(id = id, nombre = nombre)

    private fun Cliente.instalacionesAGuardar() = instalaciones.map {
        ClienteDao.DatosInstalacionGuardar(it.nombre, it.tiposEquipo, it.latitud, it.longitud)
    }
}

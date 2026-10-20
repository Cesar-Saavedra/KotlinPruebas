package com.example.appnombre_grupo1.model.repository

import com.example.appnombre_grupo1.model.Cliente
import com.example.appnombre_grupo1.model.database.dao.ClienteDao
import com.example.appnombre_grupo1.model.database.entity.ClienteEntity
import com.example.appnombre_grupo1.model.database.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

// Clientes con sus instalaciones y equipos, guardados en las tablas
// "clientes", "instalaciones" y "tipos_equipo" (Room)
class ClienteRepository(private val dao: ClienteDao) {

    val clientes: Flow<List<Cliente>> = dao.observarClientes().map { lista -> lista.map { it.toModel() } }

    // Crea el cliente si no tiene id, o lo actualiza si ya existe
    suspend fun guardar(cliente: Cliente) {
        val id = cliente.id.ifBlank { UUID.randomUUID().toString() }
        dao.guardarConInstalaciones(
            cliente = ClienteEntity(id = id, nombre = cliente.nombre),
            instalaciones = cliente.instalaciones.map { it.nombre to it.tiposEquipo },
        )
    }

    suspend fun eliminar(id: String) {
        dao.eliminarCliente(id)
    }
}

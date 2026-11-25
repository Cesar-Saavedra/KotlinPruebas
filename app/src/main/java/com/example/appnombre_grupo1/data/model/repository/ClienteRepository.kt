package com.example.appnombre_grupo1.data.model.repository

import com.example.appnombre_grupo1.data.model.Cliente
import com.example.appnombre_grupo1.data.model.Instalacion
import com.example.appnombre_grupo1.data.model.TipoEquipo
import com.example.appnombre_grupo1.data.model.database.dao.ClienteDao
import com.example.appnombre_grupo1.data.model.database.entity.ClienteEntity
import com.example.appnombre_grupo1.data.model.database.toModel
import com.example.appnombre_grupo1.data.remote.ApiService
import com.example.appnombre_grupo1.data.remote.dto.ClienteDto
import com.example.appnombre_grupo1.data.remote.dto.InstalacionDto
import com.example.appnombre_grupo1.data.remote.dto.TipoEquipoDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import android.util.Log

// Clientes con sus instalaciones y equipos, guardados en las tablas de Room y sincronizados vía Retrofit
class ClienteRepository(
    private val dao: ClienteDao,
    private val apiService: ApiService,
) {

    val clientes: Flow<List<Cliente>> = dao.observarClientes().map { lista -> lista.map { it.toModel() } }

    suspend fun refrescarClientes(): Result<Unit> {
        return try {
            val response = apiService.getClientes()
            if (!response.isSuccessful) {
                return Result.failure(Exception("Error al obtener clientes (HTTP ${response.code()})"))
            }

            val remotos = response.body().orEmpty()
            remotos.forEach { dto ->
                val clienteModel = dto.aClienteModel()
                dao.guardarConInstalaciones(
                    cliente = ClienteEntity(id = clienteModel.id, nombre = clienteModel.nombre),
                    instalaciones = clienteModel.instalaciones.map { inst ->
                        ClienteDao.DatosInstalacionGuardar(
                            remoteId = inst.id,
                            nombre = inst.nombre,
                            tipos = inst.tiposEquipo.map { tipo ->
                                ClienteDao.DatosTipoEquipoGuardar(
                                    remoteId = tipo.id,
                                    nombre = tipo.nombre,
                                )
                            },
                            latitud = inst.latitud,
                            longitud = inst.longitud,
                        )
                    },
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Crea el cliente si no tiene id, o lo actualiza si ya existe: primero en Room, luego Retrofit
    suspend fun guardar(cliente: Cliente) {
        val id = cliente.id.ifBlank { UUID.randomUUID().toString() }
        val clienteCompleto = cliente.copy(id = id)

        // 1. Guardar siempre primero en Room
        guardarLocal(clienteCompleto)

        // 2. Intentar sincronizar con el backend vía Retrofit sin romper Room si falla
        try {
            val response = apiService.guardarCliente(clienteCompleto.aClienteDto())
            if (response.isSuccessful) {
                val clienteServidor = response.body()?.aClienteModel()
                if (clienteServidor != null) {
                    guardarLocal(clienteServidor.copy(id = clienteServidor.id.ifBlank { id }))
                }
            } else {
                Log.w("ClienteRepository", "Sync falló HTTP ${response.code()}: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("ClienteRepository", "Error sincronizando cliente con backend", e)
        }

    }

    suspend fun eliminar(id: String) {
        if (id.isBlank()) return

        // 1. Eliminar primero en Room
        dao.eliminarCliente(id)

        // 2. Intentar sincronizar eliminación con Retrofit
        try {
            apiService.eliminarCliente(id)
        } catch (_: Exception) {
            // Queda eliminado en Room
        }
    }

    private suspend fun guardarLocal(cliente: Cliente) {
        dao.guardarConInstalaciones(
            cliente = ClienteEntity(id = cliente.id, nombre = cliente.nombre),
            instalaciones = cliente.instalaciones.map { inst ->
                ClienteDao.DatosInstalacionGuardar(
                    remoteId = inst.id,
                    nombre = inst.nombre,
                    tipos = inst.tiposEquipo.map { tipo ->
                        ClienteDao.DatosTipoEquipoGuardar(
                            remoteId = tipo.id,
                            nombre = tipo.nombre,
                        )
                    },
                    latitud = inst.latitud,
                    longitud = inst.longitud,
                )
            },
        )
    }



    private fun Cliente.aClienteDto(): ClienteDto {
    return ClienteDto(
        id = id,
        nombre = nombre,
        instalaciones = instalaciones.map { inst ->
            InstalacionDto(
                clientId = id,
                nombre = inst.nombre,
                latitud = inst.latitud,
                longitud = inst.longitud,
                tiposEquipo = inst.tiposEquipo.map { tipo ->
                    TipoEquipoDto(nombre = tipo.nombre)
                },
            )
        },
    )
}

private fun ClienteDto.aClienteModel(): Cliente {
    return Cliente(
        id = id,
        nombre = nombre,
        instalaciones = instalaciones.map { instDto ->
            Instalacion(
                id = instDto.id.orEmpty(), // si Instalacion tiene propiedad id
                nombre = instDto.nombre,
                tiposEquipo = instDto.tiposEquipo.map { tipoDto ->
                    TipoEquipo(
                        id = tipoDto.id.orEmpty(), // si TipoEquipo tiene propiedad id
                        nombre = tipoDto.nombre,
                    )
                },
                latitud = instDto.latitud,
                longitud = instDto.longitud,
            )
        },
    )
}
}

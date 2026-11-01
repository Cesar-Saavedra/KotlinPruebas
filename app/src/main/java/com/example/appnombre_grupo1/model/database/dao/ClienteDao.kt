package com.example.appnombre_grupo1.model.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.appnombre_grupo1.model.database.entity.ClienteConInstalaciones
import com.example.appnombre_grupo1.model.database.entity.ClienteEntity
import com.example.appnombre_grupo1.model.database.entity.InstalacionEntity
import com.example.appnombre_grupo1.model.database.entity.TipoEquipoEntity
import kotlinx.coroutines.flow.Flow

// Clase abstracta (en vez de interfaz) para poder escribir métodos @Transaction con cuerpo
@Dao
abstract class ClienteDao {

    // @Transaction: Room lee el cliente, sus instalaciones y sus equipos de forma consistente
    @Transaction
    @Query("SELECT * FROM clientes ORDER BY nombre COLLATE NOCASE")
    abstract fun observarClientes(): Flow<List<ClienteConInstalaciones>>

    @Upsert
    abstract suspend fun guardarCliente(cliente: ClienteEntity)

    @Insert
    abstract suspend fun insertarInstalacion(instalacion: InstalacionEntity): Long

    @Insert
    abstract suspend fun insertarTiposEquipo(tipos: List<TipoEquipoEntity>)

    @Query("DELETE FROM tipos_equipo WHERE instalacionId IN (SELECT id FROM instalaciones WHERE clienteId = :clienteId)")
    abstract suspend fun eliminarTiposEquipoDeCliente(clienteId: String)

    @Query("DELETE FROM instalaciones WHERE clienteId = :clienteId")
    abstract suspend fun eliminarInstalacionesDeCliente(clienteId: String)

    @Query("DELETE FROM clientes WHERE id = :clienteId")
    abstract suspend fun eliminarFilaCliente(clienteId: String)

    data class DatosInstalacionGuardar(
        val nombre: String,
        val tipos: List<String>,
        val latitud: Double? = null,
        val longitud: Double? = null,
    )

    // Guarda el cliente completo en una sola transacción: si algo falla, no queda nada a medias.
    // Las instalaciones y equipos se reemplazan por los del formulario.
    @Transaction
    open suspend fun guardarConInstalaciones(
        cliente: ClienteEntity,
        instalaciones: List<DatosInstalacionGuardar>,
    ) {
        guardarCliente(cliente)
        eliminarTiposEquipoDeCliente(cliente.id)
        eliminarInstalacionesDeCliente(cliente.id)
        instalaciones.forEach { (nombreInstalacion, tipos, latitud, longitud) ->
            val instalacionId = insertarInstalacion(
                InstalacionEntity(
                    clienteId = cliente.id,
                    nombre = nombreInstalacion,
                    latitud = latitud,
                    longitud = longitud,
                ),
            )
            insertarTiposEquipo(tipos.map { TipoEquipoEntity(instalacionId = instalacionId, nombre = it) })
        }
    }

    @Transaction
    open suspend fun eliminarCliente(clienteId: String) {
        eliminarTiposEquipoDeCliente(clienteId)
        eliminarInstalacionesDeCliente(clienteId)
        eliminarFilaCliente(clienteId)
    }
}

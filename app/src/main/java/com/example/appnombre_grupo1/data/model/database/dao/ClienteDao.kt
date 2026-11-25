package com.example.appnombre_grupo1.data.model.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.appnombre_grupo1.data.model.TipoEquipo
import com.example.appnombre_grupo1.data.model.database.entity.ClienteConInstalaciones
import com.example.appnombre_grupo1.data.model.database.entity.ClienteEntity
import com.example.appnombre_grupo1.data.model.database.entity.InstalacionEntity
import com.example.appnombre_grupo1.data.model.database.entity.TipoEquipoEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ClienteDao {

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

    data class DatosTipoEquipoGuardar(
        val remoteId: String = "",
        val nombre: String,
    )

    data class DatosInstalacionGuardar(
        val remoteId: String = "",
        val nombre: String,
        val tipos: List<DatosTipoEquipoGuardar>,
        val latitud: Double? = null,
        val longitud: Double? = null,
    )

    @Transaction
    open suspend fun guardarConInstalaciones(
        cliente: ClienteEntity,
        instalaciones: List<DatosInstalacionGuardar>,
    ) {
        guardarCliente(cliente)
        eliminarTiposEquipoDeCliente(cliente.id)
        eliminarInstalacionesDeCliente(cliente.id)
        instalaciones.forEach { dato ->
            val instalacionId = insertarInstalacion(
                InstalacionEntity(
                    clienteId = cliente.id,
                    remoteId = dato.remoteId,
                    nombre = dato.nombre,
                    latitud = dato.latitud,
                    longitud = dato.longitud,
                ),
            )
            insertarTiposEquipo(
                dato.tipos.map { tipo ->
                    TipoEquipoEntity(
                        instalacionId = instalacionId,
                        remoteId = tipo.remoteId,
                        nombre = tipo.nombre,
                        )
                    },
                )
        }
    }

    @Transaction
    open suspend fun eliminarCliente(clienteId: String) {
        eliminarTiposEquipoDeCliente(clienteId)
        eliminarInstalacionesDeCliente(clienteId)
        eliminarFilaCliente(clienteId)
    }
}

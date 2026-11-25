package com.example.appnombre_grupo1.data.model.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.OnConflictStrategy
import com.example.appnombre_grupo1.data.model.database.entity.EdicionRegistroEntity
import com.example.appnombre_grupo1.data.model.database.entity.MantenimientoEntity
import com.example.appnombre_grupo1.data.model.database.entity.PruebaElectricaEntity
import com.example.appnombre_grupo1.data.model.database.entity.RegistroCompleto
import com.example.appnombre_grupo1.data.model.database.entity.RegistroEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RegistroDao {

    @Transaction
    @Query("SELECT * FROM registros WHERE archivado = 0 ORDER BY fechaMillis DESC")
    abstract fun observarActivos(): Flow<List<RegistroCompleto>>

    @Transaction
    @Query("SELECT * FROM registros WHERE id = :id LIMIT 1")
    abstract suspend fun obtenerPorId(id: String): RegistroCompleto?


    @Transaction
    @Query("SELECT * FROM registros WHERE archivado = 1 ORDER BY archivadoFechaMillis DESC")
    abstract fun observarArchivados(): Flow<List<RegistroCompleto>>

    @Query(
        "UPDATE registros SET archivado = 1, archivadoPorUsername = :username, " +
            "archivadoPorNombre = :nombre, archivadoFechaMillis = :fechaMillis WHERE id = :id",
    )
    abstract suspend fun archivar(id: String, username: String, nombre: String, fechaMillis: Long)

    @Query(
        "UPDATE registros SET archivado = 0, archivadoPorUsername = NULL, " +
            "archivadoPorNombre = NULL, archivadoFechaMillis = NULL WHERE id = :id",
    )
    abstract suspend fun restaurar(id: String)

    @Query("SELECT COUNT(*) FROM mantenimientos WHERE fotoPath = :fotoPath")
    abstract suspend fun contarUsosDeFoto(fotoPath: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarRegistro(registro: RegistroEntity)


    @Insert
    abstract suspend fun insertarMantenimiento(mantenimiento: MantenimientoEntity)

    @Insert
    abstract suspend fun insertarPrueba(prueba: PruebaElectricaEntity)

    @Update
    abstract suspend fun actualizarRegistro(registro: RegistroEntity)

    @Update
    abstract suspend fun actualizarMantenimiento(mantenimiento: MantenimientoEntity)

    @Update
    abstract suspend fun actualizarPrueba(prueba: PruebaElectricaEntity)

    @Insert
    abstract suspend fun insertarEdicion(edicion: EdicionRegistroEntity)

    @Transaction
    open suspend fun actualizar(registro: RegistroCompleto, edicion: EdicionRegistroEntity) {
        actualizarRegistro(registro.registro)
        registro.mantenimiento?.let { actualizarMantenimiento(it) }
        registro.prueba?.let { actualizarPrueba(it) }
        insertarEdicion(edicion)
    }

    @Transaction
    open suspend fun guardar(registro: RegistroCompleto) {
        val existente = obtenerPorId(registro.registro.id)
        if (existente == null) {
            insertarRegistro(registro.registro)
            registro.mantenimiento?.let { insertarMantenimiento(it) }
            registro.prueba?.let { insertarPrueba(it) }
        } else {
            actualizarRegistro(registro.registro)
            registro.mantenimiento?.let { actualizarMantenimiento(it) }
            registro.prueba?.let { actualizarPrueba(it) }
        }
    }



}

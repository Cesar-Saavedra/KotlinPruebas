package com.example.appnombre_grupo1.model.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.appnombre_grupo1.model.database.entity.EdicionRegistroEntity
import com.example.appnombre_grupo1.model.database.entity.MantenimientoEntity
import com.example.appnombre_grupo1.model.database.entity.PruebaElectricaEntity
import com.example.appnombre_grupo1.model.database.entity.RegistroCompleto
import com.example.appnombre_grupo1.model.database.entity.RegistroEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RegistroDao {

    // Registros activos, del más reciente al más antiguo, con su detalle e historial
    @Transaction
    @Query("SELECT * FROM registros WHERE archivado = 0 ORDER BY fechaMillis DESC")
    abstract fun observarActivos(): Flow<List<RegistroCompleto>>

    // Registros archivados por un administrador, el último archivado primero
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

    @Transaction
    @Query("SELECT * FROM registros WHERE id = :id")
    abstract suspend fun obtener(id: String): RegistroCompleto?

    @Query("SELECT id FROM registros WHERE pendiente IS NOT NULL")
    abstract suspend fun idsPendientes(): List<String>

    @Query("SELECT id FROM registros WHERE pendiente IS NULL")
    abstract suspend fun idsSincronizados(): List<String>

    @Query("UPDATE registros SET pendiente = :pendiente, pendienteEditor = :editor WHERE id = :id")
    abstract suspend fun marcarPendiente(id: String, pendiente: String?, editor: String?)

    @Query("SELECT COUNT(*) FROM mantenimientos WHERE fotoPath = :fotoPath")
    abstract suspend fun contarUsosDeFoto(fotoPath: String): Int

    @Insert
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

    @Insert
    abstract suspend fun insertarEdiciones(ediciones: List<EdicionRegistroEntity>)

    @Query("DELETE FROM ediciones_registro WHERE registroId = :id")
    abstract suspend fun eliminarEdiciones(id: String)

    @Query("DELETE FROM mantenimientos WHERE registroId = :id")
    abstract suspend fun eliminarMantenimiento(id: String)

    @Query("DELETE FROM pruebas_electricas WHERE registroId = :id")
    abstract suspend fun eliminarPrueba(id: String)

    @Query("DELETE FROM registros WHERE id = :id")
    abstract suspend fun eliminarRegistro(id: String)

    @Transaction
    open suspend fun eliminarCompleto(id: String) {
        eliminarEdiciones(id)
        eliminarMantenimiento(id)
        eliminarPrueba(id)
        eliminarRegistro(id)
    }

    // Reemplaza un registro completo (con detalle e historial) por la versión del servidor
    @Transaction
    open suspend fun reemplazar(registro: RegistroCompleto) {
        eliminarCompleto(registro.registro.id)
        guardar(registro)
        insertarEdiciones(registro.ediciones)
    }

    // Deja la tabla igual a la lista del servidor, sin tocar los registros que aún no se envían
    @Transaction
    open suspend fun sincronizarCon(remotos: List<RegistroCompleto>) {
        val pendientes = idsPendientes().toSet()
        val idsRemotos = remotos.map { it.registro.id }.toSet()
        idsSincronizados().filter { it !in idsRemotos }.forEach { eliminarCompleto(it) }
        remotos.filter { it.registro.id !in pendientes }.forEach { reemplazar(it) }
    }

    // Actualiza el registro editado y deja constancia de quién lo cambió, todo en una transacción
    @Transaction
    open suspend fun actualizar(registro: RegistroCompleto, edicion: EdicionRegistroEntity) {
        actualizarRegistro(registro.registro)
        registro.mantenimiento?.let { actualizarMantenimiento(it) }
        registro.prueba?.let { actualizarPrueba(it) }
        insertarEdicion(edicion)
    }

    // Guarda el registro y su detalle juntos en una transacción
    @Transaction
    open suspend fun guardar(registro: RegistroCompleto) {
        insertarRegistro(registro.registro)
        registro.mantenimiento?.let { insertarMantenimiento(it) }
        registro.prueba?.let { insertarPrueba(it) }
    }
}

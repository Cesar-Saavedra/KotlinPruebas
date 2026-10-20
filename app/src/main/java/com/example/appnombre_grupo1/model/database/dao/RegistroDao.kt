package com.example.appnombre_grupo1.model.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.appnombre_grupo1.model.database.entity.MantenimientoEntity
import com.example.appnombre_grupo1.model.database.entity.PruebaElectricaEntity
import com.example.appnombre_grupo1.model.database.entity.RegistroCompleto
import com.example.appnombre_grupo1.model.database.entity.RegistroEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RegistroDao {

    // Del más reciente al más antiguo, con el detalle de cada protocolo
    @Transaction
    @Query("SELECT * FROM registros ORDER BY fechaMillis DESC")
    abstract fun observarTodos(): Flow<List<RegistroCompleto>>

    @Query("SELECT COUNT(*) FROM mantenimientos WHERE fotoPath = :fotoPath")
    abstract suspend fun contarUsosDeFoto(fotoPath: String): Int

    @Insert
    abstract suspend fun insertarRegistro(registro: RegistroEntity)

    @Insert
    abstract suspend fun insertarMantenimiento(mantenimiento: MantenimientoEntity)

    @Insert
    abstract suspend fun insertarPrueba(prueba: PruebaElectricaEntity)

    // Guarda el registro y su detalle juntos en una transacción
    @Transaction
    open suspend fun guardar(registro: RegistroCompleto) {
        insertarRegistro(registro.registro)
        registro.mantenimiento?.let { insertarMantenimiento(it) }
        registro.prueba?.let { insertarPrueba(it) }
    }
}

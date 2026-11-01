package com.example.appnombre_grupo1.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.appnombre_grupo1.model.database.dao.ClienteDao
import com.example.appnombre_grupo1.model.database.dao.RegistroDao
import com.example.appnombre_grupo1.model.database.dao.UsuarioDao
import com.example.appnombre_grupo1.model.database.entity.ClienteEntity
import com.example.appnombre_grupo1.model.database.entity.InstalacionEntity
import com.example.appnombre_grupo1.model.database.entity.MantenimientoEntity
import com.example.appnombre_grupo1.model.database.entity.PruebaElectricaEntity
import com.example.appnombre_grupo1.model.database.entity.RegistroEntity
import com.example.appnombre_grupo1.model.database.entity.TipoEquipoEntity
import com.example.appnombre_grupo1.model.database.entity.UsuarioEntity

// Base de datos SQLite de la app. Si se cambian las tablas hay que subir "version"
// y agregar una migración para no perder los datos guardados.
@Database(
    entities = [
        UsuarioEntity::class,
        ClienteEntity::class,
        InstalacionEntity::class,
        TipoEquipoEntity::class,
        RegistroEntity::class,
        MantenimientoEntity::class,
        PruebaElectricaEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class InduztekDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun clienteDao(): ClienteDao
    abstract fun registroDao(): RegistroDao

    companion object {
        const val NOMBRE = "induztek.db"
    }
}

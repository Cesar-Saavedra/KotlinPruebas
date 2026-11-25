package com.example.appnombre_grupo1.data.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.appnombre_grupo1.data.model.database.dao.ClienteDao
import com.example.appnombre_grupo1.data.model.database.dao.RegistroDao
import com.example.appnombre_grupo1.data.model.database.dao.UsuarioDao
import com.example.appnombre_grupo1.data.model.database.entity.ClienteEntity
import com.example.appnombre_grupo1.data.model.database.entity.EdicionRegistroEntity
import com.example.appnombre_grupo1.data.model.database.entity.InstalacionEntity
import com.example.appnombre_grupo1.data.model.database.entity.MantenimientoEntity
import com.example.appnombre_grupo1.data.model.database.entity.PruebaElectricaEntity
import com.example.appnombre_grupo1.data.model.database.entity.RegistroEntity
import com.example.appnombre_grupo1.data.model.database.entity.TipoEquipoEntity
import com.example.appnombre_grupo1.data.model.database.entity.UsuarioEntity

@Database(
    entities = [
        UsuarioEntity::class,
        ClienteEntity::class,
        InstalacionEntity::class,
        TipoEquipoEntity::class,
        RegistroEntity::class,
        MantenimientoEntity::class,
        PruebaElectricaEntity::class,
        EdicionRegistroEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class InduztekDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun clienteDao(): ClienteDao
    abstract fun registroDao(): RegistroDao

    companion object {
        const val NOMBRE = "induztek.db"

        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE registros ADD COLUMN archivado INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE registros ADD COLUMN archivadoPorUsername TEXT")
                db.execSQL("ALTER TABLE registros ADD COLUMN archivadoPorNombre TEXT")
                db.execSQL("ALTER TABLE registros ADD COLUMN archivadoFechaMillis INTEGER")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `ediciones_registro` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`registroId` TEXT NOT NULL, " +
                        "`fechaMillis` INTEGER NOT NULL, " +
                        "`editorUsername` TEXT NOT NULL, " +
                        "`editorNombre` TEXT NOT NULL, " +
                        "`cambios` TEXT NOT NULL, " +
                        "FOREIGN KEY(`registroId`) REFERENCES `registros`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_ediciones_registro_registroId` " +
                        "ON `ediciones_registro` (`registroId`)",
                )
            }
        }

        val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE instalaciones ADD COLUMN latitud REAL")
                db.execSQL("ALTER TABLE instalaciones ADD COLUMN longitud REAL")
            }
        }

        val MIGRACION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE instalaciones ADD COLUMN remoteId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE tipos_equipo ADD COLUMN remoteId TEXT NOT NULL DEFAULT ''")
            }
        }
        val MIGRACION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE usuarios ADD COLUMN id TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE usuarios SET id = username WHERE id = ''")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_usuarios_id` ON `usuarios` (`id`)")
            }
        }

    }
}

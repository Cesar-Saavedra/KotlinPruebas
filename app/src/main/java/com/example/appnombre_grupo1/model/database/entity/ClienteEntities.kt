package com.example.appnombre_grupo1.model.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

// Tabla "clientes": el nombre no se puede repetir (índice único)
@Entity(
    tableName = "clientes",
    indices = [Index(value = ["nombre"], unique = true)],
)
data class ClienteEntity(
    @PrimaryKey val id: String,
    val nombre: String,
)

// Tabla "instalaciones": cada fila pertenece a un cliente (clave foránea).
// Si se borra el cliente, se borran sus instalaciones (CASCADE).
@Entity(
    tableName = "instalaciones",
    foreignKeys = [
        ForeignKey(
            entity = ClienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("clienteId")],
)
data class InstalacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clienteId: String,
    val nombre: String,
)

// Tabla "tipos_equipo": cada fila pertenece a una instalación
@Entity(
    tableName = "tipos_equipo",
    foreignKeys = [
        ForeignKey(
            entity = InstalacionEntity::class,
            parentColumns = ["id"],
            childColumns = ["instalacionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("instalacionId")],
)
data class TipoEquipoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val instalacionId: Long,
    val nombre: String,
)

// Relaciones 1 a N que Room arma automáticamente al consultar
data class InstalacionConEquipos(
    @Embedded val instalacion: InstalacionEntity,
    @Relation(parentColumn = "id", entityColumn = "instalacionId")
    val tiposEquipo: List<TipoEquipoEntity>,
)

data class ClienteConInstalaciones(
    @Embedded val cliente: ClienteEntity,
    @Relation(entity = InstalacionEntity::class, parentColumn = "id", entityColumn = "clienteId")
    val instalaciones: List<InstalacionConEquipos>,
)

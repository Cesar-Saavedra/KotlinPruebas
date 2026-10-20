package com.example.appnombre_grupo1.model.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.appnombre_grupo1.model.EstadoComponente
import com.example.appnombre_grupo1.model.TipoRegistro
import com.example.appnombre_grupo1.model.VeredictoPrueba

// Tabla "registros": datos comunes a ambos protocolos. Se guarda una copia de los
// nombres (cliente, instalación, técnico) para que el historial no cambie si se editan.
@Entity(
    tableName = "registros",
    indices = [Index(value = ["cliente", "instalacion", "codigoEquipo"])],
)
data class RegistroEntity(
    @PrimaryKey val id: String,
    val tipo: TipoRegistro,
    val fechaMillis: Long,
    val tecnicoUsername: String,
    val tecnicoNombre: String,
    val cliente: String,
    val instalacion: String,
    val tipoEquipo: String,
    val codigoEquipo: String,
)

// Tabla "mantenimientos": detalle de un registro de tipo Mantenimiento (relación 1 a 1)
@Entity(
    tableName = "mantenimientos",
    foreignKeys = [
        ForeignKey(
            entity = RegistroEntity::class,
            parentColumns = ["id"],
            childColumns = ["registroId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class MantenimientoEntity(
    @PrimaryKey val registroId: String,
    val estadoFisicoExterior: EstadoComponente,
    val descripcionFisicoExterior: String,
    val estadoAisladores: EstadoComponente,
    val descripcionAisladores: String,
    val estadoRegletas: EstadoComponente,
    val descripcionRegletas: String,
    val estadoContactores: EstadoComponente,
    val descripcionContactores: String,
    val estadoCables: EstadoComponente,
    val descripcionCables: String,
    val estadoLucesPiloto: EstadoComponente,
    val descripcionLucesPiloto: String,
    val estadoCanaletas: EstadoComponente,
    val descripcionCanaletas: String,
    val sistemaTierraOk: Boolean,
    val sinFugasAceite: Boolean,
    val limpiezaRealizada: Boolean,
    val reajusteTorqueRealizado: Boolean,
    val lubricacionRealizada: Boolean,
    val observaciones: String,
    val fotoPath: String?,
)

// Tabla "pruebas_electricas": detalle de un registro de tipo Pruebas Eléctricas (1 a 1)
@Entity(
    tableName = "pruebas_electricas",
    foreignKeys = [
        ForeignKey(
            entity = RegistroEntity::class,
            parentColumns = ["id"],
            childColumns = ["registroId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class PruebaElectricaEntity(
    @PrimaryKey val registroId: String,
    val tensionPruebaAislamiento: String,
    val resistenciaAislamientoMOhm: String,
    val resistenciaContactoMicroOhm: String,
    val voltajeLineaV: String,
    val corrienteCargaA: String,
    val veredicto: VeredictoPrueba,
    val notasTecnicas: String,
)

// Registro con su detalle; solo uno de los dos viene informado según el tipo
data class RegistroCompleto(
    @Embedded val registro: RegistroEntity,
    @Relation(parentColumn = "id", entityColumn = "registroId")
    val mantenimiento: MantenimientoEntity?,
    @Relation(parentColumn = "id", entityColumn = "registroId")
    val prueba: PruebaElectricaEntity?,
)

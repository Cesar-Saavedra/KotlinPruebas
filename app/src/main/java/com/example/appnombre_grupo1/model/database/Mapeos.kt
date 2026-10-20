package com.example.appnombre_grupo1.model.database

import com.example.appnombre_grupo1.model.Cliente
import com.example.appnombre_grupo1.model.DatosGeneralesForm
import com.example.appnombre_grupo1.model.Instalacion
import com.example.appnombre_grupo1.model.MantenimientoState
import com.example.appnombre_grupo1.model.PruebasElectricasState
import com.example.appnombre_grupo1.model.RegistroGuardado
import com.example.appnombre_grupo1.model.Usuario
import com.example.appnombre_grupo1.model.database.entity.ClienteConInstalaciones
import com.example.appnombre_grupo1.model.database.entity.MantenimientoEntity
import com.example.appnombre_grupo1.model.database.entity.PruebaElectricaEntity
import com.example.appnombre_grupo1.model.database.entity.RegistroCompleto
import com.example.appnombre_grupo1.model.database.entity.RegistroEntity
import com.example.appnombre_grupo1.model.database.entity.UsuarioEntity

// Conversiones entre las tablas de Room (entity) y los modelos que usan ViewModels y pantallas.
// Así las pantallas no dependen de cómo se guardan los datos.

fun UsuarioEntity.toModel() = Usuario(username = username, nombre = nombre, rol = rol)

fun ClienteConInstalaciones.toModel() = Cliente(
    id = cliente.id,
    nombre = cliente.nombre,
    // Room no garantiza el orden de las relaciones; el id refleja el orden en que se agregaron
    instalaciones = instalaciones
        .sortedBy { it.instalacion.id }
        .map { conEquipos ->
            Instalacion(
                nombre = conEquipos.instalacion.nombre,
                tiposEquipo = conEquipos.tiposEquipo.sortedBy { it.id }.map { it.nombre },
            )
        },
)

fun RegistroCompleto.toModel(): RegistroGuardado {
    val datos = DatosGeneralesForm(
        cliente = registro.cliente,
        instalacion = registro.instalacion,
        tecnico = registro.tecnicoNombre,
        tipoEquipo = registro.tipoEquipo,
        codigoEquipo = registro.codigoEquipo,
    )
    return RegistroGuardado(
        id = registro.id,
        tipo = registro.tipo,
        fechaMillis = registro.fechaMillis,
        tecnicoUsername = registro.tecnicoUsername,
        mantenimiento = mantenimiento?.toModel(datos),
        pruebas = prueba?.toModel(datos),
    )
}

fun RegistroGuardado.toEntity(): RegistroCompleto {
    val datos = datosGenerales
    return RegistroCompleto(
        registro = RegistroEntity(
            id = id,
            tipo = tipo,
            fechaMillis = fechaMillis,
            tecnicoUsername = tecnicoUsername,
            tecnicoNombre = datos.tecnico,
            cliente = datos.cliente,
            instalacion = datos.instalacion,
            tipoEquipo = datos.tipoEquipo,
            codigoEquipo = datos.codigoEquipo,
        ),
        mantenimiento = mantenimiento?.toEntity(id),
        prueba = pruebas?.toEntity(id),
    )
}

private fun MantenimientoEntity.toModel(datos: DatosGeneralesForm) = MantenimientoState(
    datosGenerales = datos,
    estadoFisicoExterior = estadoFisicoExterior,
    descripcionFisicoExterior = descripcionFisicoExterior,
    estadoAisladores = estadoAisladores,
    descripcionAisladores = descripcionAisladores,
    estadoRegletas = estadoRegletas,
    descripcionRegletas = descripcionRegletas,
    estadoContactores = estadoContactores,
    descripcionContactores = descripcionContactores,
    estadoCables = estadoCables,
    descripcionCables = descripcionCables,
    estadoLucesPiloto = estadoLucesPiloto,
    descripcionLucesPiloto = descripcionLucesPiloto,
    estadoCanaletas = estadoCanaletas,
    descripcionCanaletas = descripcionCanaletas,
    sistemaTierraOk = sistemaTierraOk,
    sinFugasAceite = sinFugasAceite,
    limpiezaRealizada = limpiezaRealizada,
    reajusteTorqueRealizado = reajusteTorqueRealizado,
    lubricacionRealizada = lubricacionRealizada,
    observaciones = observaciones,
    fotoPath = fotoPath,
)

private fun MantenimientoState.toEntity(registroId: String) = MantenimientoEntity(
    registroId = registroId,
    estadoFisicoExterior = estadoFisicoExterior,
    descripcionFisicoExterior = descripcionFisicoExterior,
    estadoAisladores = estadoAisladores,
    descripcionAisladores = descripcionAisladores,
    estadoRegletas = estadoRegletas,
    descripcionRegletas = descripcionRegletas,
    estadoContactores = estadoContactores,
    descripcionContactores = descripcionContactores,
    estadoCables = estadoCables,
    descripcionCables = descripcionCables,
    estadoLucesPiloto = estadoLucesPiloto,
    descripcionLucesPiloto = descripcionLucesPiloto,
    estadoCanaletas = estadoCanaletas,
    descripcionCanaletas = descripcionCanaletas,
    sistemaTierraOk = sistemaTierraOk,
    sinFugasAceite = sinFugasAceite,
    limpiezaRealizada = limpiezaRealizada,
    reajusteTorqueRealizado = reajusteTorqueRealizado,
    lubricacionRealizada = lubricacionRealizada,
    observaciones = observaciones,
    fotoPath = fotoPath,
)

private fun PruebaElectricaEntity.toModel(datos: DatosGeneralesForm) = PruebasElectricasState(
    datosGenerales = datos,
    tensionPruebaAislamiento = tensionPruebaAislamiento,
    resistenciaAislamientoMOhm = resistenciaAislamientoMOhm,
    resistenciaContactoMicroOhm = resistenciaContactoMicroOhm,
    voltajeLineaV = voltajeLineaV,
    corrienteCargaA = corrienteCargaA,
    veredicto = veredicto,
    notasTecnicas = notasTecnicas,
)

private fun PruebasElectricasState.toEntity(registroId: String) = PruebaElectricaEntity(
    registroId = registroId,
    tensionPruebaAislamiento = tensionPruebaAislamiento,
    resistenciaAislamientoMOhm = resistenciaAislamientoMOhm,
    resistenciaContactoMicroOhm = resistenciaContactoMicroOhm,
    voltajeLineaV = voltajeLineaV,
    corrienteCargaA = corrienteCargaA,
    veredicto = veredicto,
    notasTecnicas = notasTecnicas,
)

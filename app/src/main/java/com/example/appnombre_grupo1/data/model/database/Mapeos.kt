package com.example.appnombre_grupo1.data.model.database

import com.example.appnombre_grupo1.data.model.ArchivoRegistro
import com.example.appnombre_grupo1.data.model.Cliente
import com.example.appnombre_grupo1.data.model.DatosGeneralesForm
import com.example.appnombre_grupo1.data.model.EdicionRegistro
import com.example.appnombre_grupo1.data.model.Instalacion
import com.example.appnombre_grupo1.data.model.MantenimientoState
import com.example.appnombre_grupo1.data.model.PruebasElectricasState
import com.example.appnombre_grupo1.data.model.RegistroGuardado
import com.example.appnombre_grupo1.data.model.TipoEquipo
import com.example.appnombre_grupo1.data.model.Usuario
import com.example.appnombre_grupo1.data.model.database.entity.ClienteConInstalaciones
import com.example.appnombre_grupo1.data.model.database.entity.EdicionRegistroEntity
import com.example.appnombre_grupo1.data.model.database.entity.MantenimientoEntity
import com.example.appnombre_grupo1.data.model.database.entity.PruebaElectricaEntity
import com.example.appnombre_grupo1.data.model.database.entity.RegistroCompleto
import com.example.appnombre_grupo1.data.model.database.entity.RegistroEntity
import com.example.appnombre_grupo1.data.model.database.entity.UsuarioEntity

fun UsuarioEntity.toModel() = Usuario(id = id, username = username, nombre = nombre, rol = rol)

fun ClienteConInstalaciones.toModel() = Cliente(
    id = cliente.id,
    nombre = cliente.nombre,
    instalaciones = instalaciones
        .sortedBy { it.instalacion.id }
        .map { inst ->
            Instalacion(
                id = inst.instalacion.remoteId.ifBlank { inst.instalacion.id.toString() },
                nombre = inst.instalacion.nombre,
                tiposEquipo = inst.tiposEquipo.map { TipoEquipo(id = it.remoteId.ifBlank { it.id.toString() }, nombre = it.nombre) },
                latitud = inst.instalacion.latitud,
                longitud = inst.instalacion.longitud,
            )
        },
)


fun RegistroCompleto.toModel() = RegistroGuardado(
    id = registro.id,
    tipo = registro.tipo,
    fechaMillis = registro.fechaMillis,
    tecnicoUsername = registro.tecnicoUsername,
    mantenimiento = mantenimiento?.toModel(registro),
    pruebas = prueba?.toModel(registro),
    ediciones = ediciones.map { it.toModel() },
    archivado = if (registro.archivado) {
        ArchivoRegistro(
            fechaMillis = registro.archivadoFechaMillis ?: 0L,
            porUsername = registro.archivadoPorUsername.orEmpty(),
            porNombre = registro.archivadoPorNombre.orEmpty(),
        )
    } else {
        null
    },
)

fun MantenimientoEntity.toModel(registro: RegistroEntity) = MantenimientoState(
    datosGenerales = DatosGeneralesForm(
        cliente = registro.cliente,
        instalacion = registro.instalacion,
        tipoEquipo = registro.tipoEquipo,
        codigoEquipo = registro.codigoEquipo,
    ),
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

fun PruebaElectricaEntity.toModel(registro: RegistroEntity) = PruebasElectricasState(
    datosGenerales = DatosGeneralesForm(
        cliente = registro.cliente,
        instalacion = registro.instalacion,
        tipoEquipo = registro.tipoEquipo,
        codigoEquipo = registro.codigoEquipo,
    ),
    tensionPruebaAislamiento = tensionPruebaAislamiento,
    resistenciaAislamientoMOhm = resistenciaAislamientoMOhm,
    resistenciaContactoMicroOhm = resistenciaContactoMicroOhm,
    voltajeLineaV = voltajeLineaV,
    corrienteCargaA = corrienteCargaA,
    veredicto = veredicto,
    notasTecnicas = notasTecnicas,
)

fun EdicionRegistroEntity.toModel() = EdicionRegistro(
    fechaMillis = fechaMillis,
    editorUsername = editorUsername,
    editorNombre = editorNombre,
    cambios = cambios.split("\n").filter { it.isNotBlank() },
)

fun RegistroGuardado.toEntity(): RegistroCompleto {
    val archivadoInfo = archivado
    val reg = RegistroEntity(
        id = id,
        tipo = tipo,
        fechaMillis = fechaMillis,
        tecnicoUsername = tecnicoUsername,
        tecnicoNombre = datosGenerales.tecnico.ifBlank { tecnicoUsername },
        cliente = datosGenerales.cliente,
        instalacion = datosGenerales.instalacion,
        tipoEquipo = datosGenerales.tipoEquipo,
        codigoEquipo = datosGenerales.codigoEquipo,
        archivado = archivadoInfo != null,
        archivadoPorUsername = archivadoInfo?.porUsername,
        archivadoPorNombre = archivadoInfo?.porNombre,
        archivadoFechaMillis = archivadoInfo?.fechaMillis,
    )
    val mant = mantenimiento?.toEntity(id)
    val prue = pruebas?.toEntity(id)
    return RegistroCompleto(registro = reg, mantenimiento = mant, prueba = prue)
}

fun MantenimientoState.toEntity(registroId: String) = MantenimientoEntity(
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

fun PruebasElectricasState.toEntity(registroId: String) = PruebaElectricaEntity(
    registroId = registroId,
    tensionPruebaAislamiento = tensionPruebaAislamiento,
    resistenciaAislamientoMOhm = resistenciaAislamientoMOhm,
    resistenciaContactoMicroOhm = resistenciaContactoMicroOhm,
    voltajeLineaV = voltajeLineaV,
    corrienteCargaA = corrienteCargaA,
    veredicto = veredicto,
    notasTecnicas = notasTecnicas,
)

fun EdicionRegistro.toEntity(registroId: String) = EdicionRegistroEntity(
    registroId = registroId,
    fechaMillis = fechaMillis,
    editorUsername = editorUsername,
    editorNombre = editorNombre,
    cambios = cambios.joinToString("\n"),
)

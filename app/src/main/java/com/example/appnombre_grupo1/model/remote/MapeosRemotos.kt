package com.example.appnombre_grupo1.model.remote

import com.example.appnombre_grupo1.model.ArchivoRegistro
import com.example.appnombre_grupo1.model.Cliente
import com.example.appnombre_grupo1.model.DatosGeneralesForm
import com.example.appnombre_grupo1.model.EdicionRegistro
import com.example.appnombre_grupo1.model.EstadoComponente
import com.example.appnombre_grupo1.model.Instalacion
import com.example.appnombre_grupo1.model.MantenimientoState
import com.example.appnombre_grupo1.model.PruebasElectricasState
import com.example.appnombre_grupo1.model.RegistroGuardado
import com.example.appnombre_grupo1.model.RolUsuario
import com.example.appnombre_grupo1.model.TipoRegistro
import com.example.appnombre_grupo1.model.Usuario
import com.example.appnombre_grupo1.model.VeredictoPrueba

// Conversiones entre el JSON del backend (DTO) y los modelos de la app

fun UsuarioDto.toModel() = Usuario(username = username, nombre = nombre, rol = rol ?: RolUsuario.TECNICO)

fun ClienteDto.toModel() = Cliente(
    id = id.orEmpty(),
    nombre = nombre,
    instalaciones = instalaciones.orEmpty().map {
        Instalacion(it.nombre, it.tiposEquipo.orEmpty(), it.latitud, it.longitud)
    },
)

fun Cliente.toDto() = ClienteDto(
    id = id.ifBlank { null },
    nombre = nombre,
    instalaciones = instalaciones.map { InstalacionDto(it.nombre, it.tiposEquipo, it.latitud, it.longitud) },
)

fun RegistroDto.toModel(): RegistroGuardado {
    val datos = DatosGeneralesForm(
        cliente = cliente,
        instalacion = instalacion,
        tecnico = tecnicoNombre.orEmpty(),
        tipoEquipo = tipoEquipo,
        codigoEquipo = codigoEquipo,
    )
    return RegistroGuardado(
        id = id.orEmpty(),
        tipo = tipo ?: TipoRegistro.MANTENIMIENTO,
        fechaMillis = fechaMillis ?: 0L,
        tecnicoUsername = tecnicoUsername,
        mantenimiento = mantenimiento?.toModel(datos),
        pruebas = prueba?.toModel(datos),
        ediciones = ediciones.orEmpty().map {
            EdicionRegistro(it.fechaMillis, it.editorUsername, it.editorNombre, it.cambios.orEmpty())
        },
        archivado = if (archivado) {
            ArchivoRegistro(archivadoFechaMillis ?: 0L, archivadoPorUsername.orEmpty(), archivadoPorNombre.orEmpty())
        } else {
            null
        },
    )
}

fun RegistroGuardado.toDto(): RegistroDto {
    val datos = datosGenerales
    return RegistroDto(
        id = id,
        tipo = tipo,
        fechaMillis = fechaMillis,
        tecnicoUsername = tecnicoUsername,
        cliente = datos.cliente,
        instalacion = datos.instalacion,
        tipoEquipo = datos.tipoEquipo,
        codigoEquipo = datos.codigoEquipo,
        mantenimiento = mantenimiento?.toDto(),
        prueba = pruebas?.toDto(),
    )
}

private fun MantenimientoDto.toModel(datos: DatosGeneralesForm) = MantenimientoState(
    datosGenerales = datos,
    estadoFisicoExterior = estadoFisicoExterior ?: EstadoComponente.BUENO,
    descripcionFisicoExterior = descripcionFisicoExterior.orEmpty(),
    estadoAisladores = estadoAisladores ?: EstadoComponente.BUENO,
    descripcionAisladores = descripcionAisladores.orEmpty(),
    estadoRegletas = estadoRegletas ?: EstadoComponente.BUENO,
    descripcionRegletas = descripcionRegletas.orEmpty(),
    estadoContactores = estadoContactores ?: EstadoComponente.BUENO,
    descripcionContactores = descripcionContactores.orEmpty(),
    estadoCables = estadoCables ?: EstadoComponente.BUENO,
    descripcionCables = descripcionCables.orEmpty(),
    estadoLucesPiloto = estadoLucesPiloto ?: EstadoComponente.BUENO,
    descripcionLucesPiloto = descripcionLucesPiloto.orEmpty(),
    estadoCanaletas = estadoCanaletas ?: EstadoComponente.BUENO,
    descripcionCanaletas = descripcionCanaletas.orEmpty(),
    sistemaTierraOk = sistemaTierraOk,
    sinFugasAceite = sinFugasAceite,
    limpiezaRealizada = limpiezaRealizada,
    reajusteTorqueRealizado = reajusteTorqueRealizado,
    lubricacionRealizada = lubricacionRealizada,
    observaciones = observaciones.orEmpty(),
    fotoPath = fotoUri,
)

private fun MantenimientoState.toDto() = MantenimientoDto(
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
    fotoUri = fotoPath,
)

private fun PruebaDto.toModel(datos: DatosGeneralesForm) = PruebasElectricasState(
    datosGenerales = datos,
    tensionPruebaAislamiento = tensionPruebaAislamiento ?: "1000 V",
    resistenciaAislamientoMOhm = resistenciaAislamientoMOhm.orEmpty(),
    resistenciaContactoMicroOhm = resistenciaContactoMicroOhm.orEmpty(),
    voltajeLineaV = voltajeLineaV.orEmpty(),
    corrienteCargaA = corrienteCargaA.orEmpty(),
    veredicto = veredicto ?: VeredictoPrueba.APROBADO,
    notasTecnicas = notasTecnicas.orEmpty(),
)

private fun PruebasElectricasState.toDto() = PruebaDto(
    tensionPruebaAislamiento = tensionPruebaAislamiento,
    resistenciaAislamientoMOhm = resistenciaAislamientoMOhm,
    resistenciaContactoMicroOhm = resistenciaContactoMicroOhm,
    voltajeLineaV = voltajeLineaV,
    corrienteCargaA = corrienteCargaA,
    veredicto = veredicto,
    notasTecnicas = notasTecnicas,
)

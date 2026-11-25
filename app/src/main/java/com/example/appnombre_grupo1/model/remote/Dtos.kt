package com.example.appnombre_grupo1.model.remote

import com.example.appnombre_grupo1.model.EstadoComponente
import com.example.appnombre_grupo1.model.RolUsuario
import com.example.appnombre_grupo1.model.TipoRegistro
import com.example.appnombre_grupo1.model.VeredictoPrueba

// Formato JSON del backend. Los campos tienen valor por defecto porque Gson
// deja en null los que no vienen en la respuesta.

data class LoginRequestDto(val username: String, val password: String)

data class UsuarioDto(
    val username: String = "",
    val nombre: String = "",
    val rol: RolUsuario? = null,
)

data class UsuarioRequestDto(
    val username: String?,
    val nombre: String,
    val rol: RolUsuario,
    val password: String?,
)

data class ClienteDto(
    val id: String? = null,
    val nombre: String = "",
    val instalaciones: List<InstalacionDto>? = null,
)

data class InstalacionDto(
    val nombre: String = "",
    val tiposEquipo: List<String>? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
)

data class RegistroDto(
    val id: String? = null,
    val tipo: TipoRegistro? = null,
    val fechaMillis: Long? = null,
    val tecnicoUsername: String = "",
    val tecnicoNombre: String? = null,
    val cliente: String = "",
    val instalacion: String = "",
    val tipoEquipo: String = "",
    val codigoEquipo: String = "",
    val mantenimiento: MantenimientoDto? = null,
    val prueba: PruebaDto? = null,
    val archivado: Boolean = false,
    val archivadoPorUsername: String? = null,
    val archivadoPorNombre: String? = null,
    val archivadoFechaMillis: Long? = null,
    val ediciones: List<EdicionDto>? = null,
)

data class MantenimientoDto(
    val estadoFisicoExterior: EstadoComponente? = null,
    val descripcionFisicoExterior: String? = null,
    val estadoAisladores: EstadoComponente? = null,
    val descripcionAisladores: String? = null,
    val estadoRegletas: EstadoComponente? = null,
    val descripcionRegletas: String? = null,
    val estadoContactores: EstadoComponente? = null,
    val descripcionContactores: String? = null,
    val estadoCables: EstadoComponente? = null,
    val descripcionCables: String? = null,
    val estadoLucesPiloto: EstadoComponente? = null,
    val descripcionLucesPiloto: String? = null,
    val estadoCanaletas: EstadoComponente? = null,
    val descripcionCanaletas: String? = null,
    val sistemaTierraOk: Boolean = true,
    val sinFugasAceite: Boolean = true,
    val limpiezaRealizada: Boolean = false,
    val reajusteTorqueRealizado: Boolean = false,
    val lubricacionRealizada: Boolean = false,
    val observaciones: String? = null,
    val fotoUri: String? = null,
)

data class PruebaDto(
    val tensionPruebaAislamiento: String? = null,
    val resistenciaAislamientoMOhm: String? = null,
    val resistenciaContactoMicroOhm: String? = null,
    val voltajeLineaV: String? = null,
    val corrienteCargaA: String? = null,
    val veredicto: VeredictoPrueba? = null,
    val notasTecnicas: String? = null,
)

data class EdicionDto(
    val fechaMillis: Long = 0L,
    val editorUsername: String = "",
    val editorNombre: String = "",
    val cambios: List<String>? = null,
)

data class ErrorDto(val message: String? = null)

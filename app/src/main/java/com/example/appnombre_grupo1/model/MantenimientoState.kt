package com.example.appnombre_grupo1.model

data class MantenimientoState(
    val datosGenerales: DatosGeneralesForm = DatosGeneralesForm(),

    val estadoFisicoExterior: EstadoComponente = EstadoComponente.BUENO,
    val descripcionFisicoExterior: String = "",

    val estadoAisladores: EstadoComponente = EstadoComponente.BUENO,
    val descripcionAisladores: String = "",

    val estadoRegletas: EstadoComponente = EstadoComponente.BUENO,
    val descripcionRegletas: String = "",

    val estadoContactores: EstadoComponente = EstadoComponente.BUENO,
    val descripcionContactores: String = "",

    val estadoCables: EstadoComponente = EstadoComponente.BUENO,
    val descripcionCables: String = "",

    val estadoLucesPiloto: EstadoComponente = EstadoComponente.BUENO,
    val descripcionLucesPiloto: String = "",

    val estadoCanaletas: EstadoComponente = EstadoComponente.BUENO,
    val descripcionCanaletas: String = "",

    // Verificaciones y observaciones
    val sistemaTierraOk: Boolean = true,
    val sinFugasAceite: Boolean = true,
    val limpiezaRealizada: Boolean = false,
    val reajusteTorqueRealizado: Boolean = false,
    val lubricacionRealizada: Boolean = false,
    val observaciones: String = "",

    // Ruta de la foto guardada en el almacenamiento interno de la app (opcional)
    val fotoPath: String? = null,
)

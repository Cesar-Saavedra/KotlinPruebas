package com.example.appnombre_grupo1.model

import com.example.appnombre_grupo1.model.EstadoComponente

data class MantenimientoState(
    val datosGenerales: DatosGeneralesForm = DatosGeneralesForm(),
    val estadoFisicoExterior: EstadoComponente = EstadoComponente.BUENO,
    val estadoAisladores: EstadoComponente = EstadoComponente.BUENO,
    val sistemaTierraOk: Boolean = true,
    val sinFugasAceite: Boolean = true,
    val limpiezaRealizada: Boolean = false,
    val reajusteTorqueRealizado: Boolean = false,
    val lubricacionRealizada: Boolean = false,
    val observaciones: String = ""
)
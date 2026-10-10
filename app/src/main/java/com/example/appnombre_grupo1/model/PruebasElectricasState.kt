package com.example.appnombre_grupo1.model

import com.example.appnombre_grupo1.model.VeredictoPrueba

data class PruebasElectricasState(
    val datosGenerales: DatosGeneralesForm = DatosGeneralesForm(),
    val tensionPruebaAislamiento: String = "1000 V",
    val resistenciaAislamientoMOhm: String = "",
    val resistenciaContactoMicroOhm: String = "",
    val voltajeLineaV: String = "",
    val corrienteCargaA: String = "",
    val veredicto: VeredictoPrueba = VeredictoPrueba.APROBADO,
    val notasTecnicas: String = ""
)
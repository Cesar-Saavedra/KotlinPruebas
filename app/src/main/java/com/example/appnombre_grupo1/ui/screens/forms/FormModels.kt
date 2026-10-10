package com.example.appnombre_grupo1.ui.screens.forms

enum class EstadoComponente(val label: String) {
    BUENO("Bueno"),
    REGULAR("Regular"),
    MALO("Malo"),
    NO_APLICA("N/A")
}

enum class VeredictoPrueba(val label: String) {
    APROBADO("Aprobado"),
    OBSERVADO("Observado"),
    RECHAZADO("Rechazado")
}

data class DatosGeneralesForm(
    val cliente: String = "",
    val instalacion: String = "",
    val tecnico: String = "",
    val tipoEquipo: String = "Transformador",
    val codigoEquipo: String = "",
    val temperaturaAmbiente: String = "",
    val humedadRelativa: String = ""
)


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
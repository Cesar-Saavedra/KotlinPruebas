package com.example.appnombre_grupo1.model

enum class TipoRegistro(val label: String) {
    MANTENIMIENTO("Mantenimiento"),
    PRUEBAS_ELECTRICAS("Pruebas Eléctricas")
}

// Registro terminado por un técnico; solo uno de los dos protocolos viene informado
data class RegistroGuardado(
    val id: String = "",
    val tipo: TipoRegistro = TipoRegistro.MANTENIMIENTO,
    val fechaMillis: Long = 0L,
    val tecnicoUsername: String = "",
    val mantenimiento: MantenimientoState? = null,
    val pruebas: PruebasElectricasState? = null,
) {
    val datosGenerales: DatosGeneralesForm
        get() = mantenimiento?.datosGenerales ?: pruebas?.datosGenerales ?: DatosGeneralesForm()
}

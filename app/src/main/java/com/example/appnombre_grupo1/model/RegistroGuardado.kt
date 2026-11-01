package com.example.appnombre_grupo1.model

enum class TipoRegistro(val label: String) {
    MANTENIMIENTO("Mantenimiento"),
    PRUEBAS_ELECTRICAS("Pruebas Eléctricas")
}

// Una modificación hecha a un registro ya guardado: quién, cuándo y qué campos cambió
data class EdicionRegistro(
    val fechaMillis: Long = 0L,
    val editorUsername: String = "",
    val editorNombre: String = "",
    val cambios: List<String> = emptyList(),
)

// Datos de quién y cuándo archivó un registro (solo los administradores pueden hacerlo)
data class ArchivoRegistro(
    val fechaMillis: Long = 0L,
    val porUsername: String = "",
    val porNombre: String = "",
)

// Registro terminado por un técnico; solo uno de los dos protocolos viene informado
data class RegistroGuardado(
    val id: String = "",
    val tipo: TipoRegistro = TipoRegistro.MANTENIMIENTO,
    val fechaMillis: Long = 0L,
    val tecnicoUsername: String = "",
    val mantenimiento: MantenimientoState? = null,
    val pruebas: PruebasElectricasState? = null,
    // Historial de ediciones, de la más reciente a la más antigua
    val ediciones: List<EdicionRegistro> = emptyList(),
    // null si el registro está activo
    val archivado: ArchivoRegistro? = null,
) {
    val datosGenerales: DatosGeneralesForm
        get() = mantenimiento?.datosGenerales ?: pruebas?.datosGenerales ?: DatosGeneralesForm()
}

package com.example.appnombre_grupo1.model

data class DatosGeneralesForm(
    val cliente: String = "",
    val instalacion: String = "",
    val tecnico: String = "",
    val tipoEquipo: String = "Transformador",
    val codigoEquipo: String = "",
    val temperaturaAmbiente: String = "",
    val humedadRelativa: String = "",
    val errores: DatosGeneralesErrores = DatosGeneralesErrores(),
)

// Mensaje de error por campo; null significa que el campo es válido
data class DatosGeneralesErrores(
    val cliente: String? = null,
    val instalacion: String? = null,
    val tecnico: String? = null,
    val tipoEquipo: String? = null,
    val codigoEquipo: String? = null,
) {
    val isValid: Boolean
        get() = listOf(cliente, instalacion, tecnico, tipoEquipo, codigoEquipo).all { it == null }
}

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

data class DatosGeneralesErrores(
    val cliente: String? = null,
    val instalacion: String? = null,
    val tecnico: String? = null,
    val tipoEquipo: String? = null,
    val codigoEquipo: String? = null,
) {
    val isValid: Boolean
        get() = (cliente == null) &&
                (instalacion == null) &&
                (tecnico == null) &&
                (tipoEquipo == null) &&
                (codigoEquipo == null)
}

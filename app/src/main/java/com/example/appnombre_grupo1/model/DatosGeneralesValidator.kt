package com.example.appnombre_grupo1.model

data class DatosGeneralesValidationResult(
    val clienteError: String? = null,
    val instalacionError: String? = null,
    val tecnicoError: String? = null,
    val tipoEquipoError: String? = null,
    val codigoEquipoError: String? = null
) {
    val isValid: Boolean
        get() = clienteError == null &&
                instalacionError == null &&
                tecnicoError == null &&
                tipoEquipoError == null &&
                codigoEquipoError == null
}

class DatosGeneralesValidator {
    fun validate(form: DatosGeneralesForm): DatosGeneralesValidationResult {
        val clienteError = if (form.cliente.isBlank()) "El campo Cliente no puede estar vacío" else null
        val instalacionError = if (form.instalacion.isBlank()) "El campo Instalación no puede estar vacío" else null
        val tecnicoError = if (form.tecnico.isBlank()) "El campo Técnico no puede estar vacío" else null
        val tipoEquipoError = if (form.tipoEquipo.isBlank()) "El campo Tipo de Equipo no puede estar vacío" else null
        val codigoEquipoError = if (form.codigoEquipo.isBlank()) "El campo Código de Equipo no puede estar vacío" else null

        return DatosGeneralesValidationResult(
            clienteError = clienteError,
            instalacionError = instalacionError,
            tecnicoError = tecnicoError,
            tipoEquipoError = tipoEquipoError,
            codigoEquipoError = codigoEquipoError
        )
    }
}

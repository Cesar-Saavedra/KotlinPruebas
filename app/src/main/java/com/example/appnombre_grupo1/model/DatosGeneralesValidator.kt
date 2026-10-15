package com.example.appnombre_grupo1.model

class DatosGeneralesValidator {
    fun validate(form: DatosGeneralesForm): DatosGeneralesForm {
        val clienteError = if (form.cliente.isBlank()) "El campo Cliente no puede estar vacío" else null
        val instalacionError = if (form.instalacion.isBlank()) "El campo Instalación no puede estar vacío" else null
        val tecnicoError = if (form.tecnico.isBlank()) "El campo Técnico no puede estar vacío" else null
        val tipoEquipoError = if (form.tipoEquipo.isBlank()) "El campo Tipo de Equipo no puede estar vacío" else null
        val codigoEquipoError = if (form.codigoEquipo.isBlank()) "El campo Código de Equipo no puede estar vacío" else null

        val errores = DatosGeneralesErrores(
            cliente = clienteError,
            instalacion = instalacionError,
            tecnico = tecnicoError,
            tipoEquipo = tipoEquipoError,
            codigoEquipo = codigoEquipoError,
        )

        return form.copy(errores = errores)
    }
}

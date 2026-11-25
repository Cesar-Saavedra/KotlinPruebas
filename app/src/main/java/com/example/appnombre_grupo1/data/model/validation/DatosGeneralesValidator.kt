package com.example.appnombre_grupo1.data.model.validation

import com.example.appnombre_grupo1.data.model.DatosGeneralesErrores
import com.example.appnombre_grupo1.data.model.DatosGeneralesForm

class DatosGeneralesValidator {

    fun validate(form: DatosGeneralesForm): DatosGeneralesForm = form.copy(
        errores = DatosGeneralesErrores(
            cliente = obligatorio(form.cliente),
            instalacion = obligatorio(form.instalacion),
            tecnico = obligatorio(form.tecnico),
            tipoEquipo = obligatorio(form.tipoEquipo),
            codigoEquipo = obligatorio(form.codigoEquipo),
        )
    )

    private fun obligatorio(valor: String): String? =
        if (valor.isBlank()) "Este campo es obligatorio" else null
}

package com.example.appnombre_grupo1.model

import android.os.Parcelable



data class DatosGeneralesForm(
    val cliente: String = "",
    val instalacion: String = "",
    val tecnico: String = "",
    val tipoEquipo: String = "Transformador",
    val codigoEquipo: String = "",
    val temperaturaAmbiente: String = "",
    val humedadRelativa: String = ""
)

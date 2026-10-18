package com.example.appnombre_grupo1.model.repository

import android.content.Context
import com.example.appnombre_grupo1.model.DatosGeneralesErrores
import com.example.appnombre_grupo1.model.MantenimientoState
import com.example.appnombre_grupo1.model.PruebasElectricasState
import com.example.appnombre_grupo1.model.RegistroGuardado
import com.example.appnombre_grupo1.model.TipoRegistro
import com.example.appnombre_grupo1.model.Usuario
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

// Registros de terreno terminados, guardados en el teléfono (el más reciente primero)
class RegistroRepository(context: Context, private val gson: Gson) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _registros = MutableStateFlow(leer())
    val registros: StateFlow<List<RegistroGuardado>> = _registros.asStateFlow()

    fun guardarMantenimiento(registro: MantenimientoState, tecnico: Usuario) {
        val limpio = registro.copy(datosGenerales = registro.datosGenerales.copy(errores = DatosGeneralesErrores()))
        agregar(nuevoRegistro(TipoRegistro.MANTENIMIENTO, tecnico).copy(mantenimiento = limpio))
    }

    fun guardarPruebas(registro: PruebasElectricasState, tecnico: Usuario) {
        val limpio = registro.copy(datosGenerales = registro.datosGenerales.copy(errores = DatosGeneralesErrores()))
        agregar(nuevoRegistro(TipoRegistro.PRUEBAS_ELECTRICAS, tecnico).copy(pruebas = limpio))
    }

    private fun nuevoRegistro(tipo: TipoRegistro, tecnico: Usuario) = RegistroGuardado(
        id = UUID.randomUUID().toString(),
        tipo = tipo,
        fechaMillis = System.currentTimeMillis(),
        tecnicoUsername = tecnico.username,
    )

    private fun agregar(registro: RegistroGuardado) {
        val actualizados = listOf(registro) + _registros.value
        prefs.edit().putString(KEY_REGISTROS, gson.toJson(actualizados)).apply()
        _registros.value = actualizados
    }

    private fun leer(): List<RegistroGuardado> {
        val json = prefs.getString(KEY_REGISTROS, null) ?: return emptyList()
        return gson.fromJson(json, object : TypeToken<List<RegistroGuardado>>() {}.type)
    }

    private companion object {
        const val PREFS_NAME = "registros_prefs"
        const val KEY_REGISTROS = "registros"
    }
}

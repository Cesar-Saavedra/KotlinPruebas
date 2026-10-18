package com.example.appnombre_grupo1.model.repository

import android.content.Context
import com.example.appnombre_grupo1.model.Cliente
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

// Clientes con sus instalaciones y tipos de equipo, guardados en el teléfono
class ClienteRepository(context: Context, private val gson: Gson) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _clientes = MutableStateFlow(leer())
    val clientes: StateFlow<List<Cliente>> = _clientes.asStateFlow()

    // Crea el cliente si no tiene id, o lo actualiza si ya existe
    fun guardar(cliente: Cliente) {
        val actuales = _clientes.value
        val actualizados = if (cliente.id.isBlank()) {
            actuales + cliente.copy(id = UUID.randomUUID().toString())
        } else {
            actuales.map { if (it.id == cliente.id) cliente else it }
        }
        escribir(actualizados)
    }

    fun eliminar(id: String) {
        escribir(_clientes.value.filterNot { it.id == id })
    }

    private fun leer(): List<Cliente> {
        val json = prefs.getString(KEY_CLIENTES, null)
        if (json == null) {
            // Clientes de prueba creados la primera vez que se abre la app
            val iniciales = listOf(
                Cliente(
                    id = UUID.randomUUID().toString(),
                    nombre = "Jumbo",
                    instalaciones = listOf("Jumbo Costanera Center", "Jumbo La Florida"),
                    tiposEquipo = listOf("Transformador", "Tablero Eléctrico"),
                ),
                Cliente(
                    id = UUID.randomUUID().toString(),
                    nombre = "Falabella",
                    instalaciones = listOf("Falabella Parque Arauco", "Centro de Distribución Quilicura"),
                    tiposEquipo = listOf("Celda de Media Tensión", "Grupo Electrógeno"),
                ),
            )
            prefs.edit().putString(KEY_CLIENTES, gson.toJson(iniciales)).apply()
            return iniciales
        }
        return gson.fromJson(json, object : TypeToken<List<Cliente>>() {}.type)
    }

    private fun escribir(clientes: List<Cliente>) {
        prefs.edit().putString(KEY_CLIENTES, gson.toJson(clientes)).apply()
        _clientes.value = clientes
    }

    private companion object {
        const val PREFS_NAME = "clientes_prefs"
        const val KEY_CLIENTES = "clientes"
    }
}

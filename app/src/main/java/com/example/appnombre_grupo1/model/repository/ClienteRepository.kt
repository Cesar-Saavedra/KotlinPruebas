package com.example.appnombre_grupo1.model.repository

import android.content.Context
import com.example.appnombre_grupo1.model.Cliente
import com.example.appnombre_grupo1.model.Instalacion
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

// Clientes con sus instalaciones y los tipos de equipo de cada una, guardados en el teléfono
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
        prefs.getString(KEY_CLIENTES, null)?.let { json ->
            return gson.fromJson(json, object : TypeToken<List<Cliente>>() {}.type)
        }

        val iniciales = migrarFormatoAnterior() ?: clientesDePrueba()
        prefs.edit()
            .putString(KEY_CLIENTES, gson.toJson(iniciales))
            .remove(KEY_CLIENTES_V1)
            .apply()
        return iniciales
    }

    // Versión anterior: los tipos de equipo eran del cliente y no de cada instalación.
    // Se asignan todos los tipos del cliente a cada una de sus instalaciones.
    private fun migrarFormatoAnterior(): List<Cliente>? {
        val json = prefs.getString(KEY_CLIENTES_V1, null) ?: return null
        val anteriores: List<ClienteV1> = gson.fromJson(json, object : TypeToken<List<ClienteV1>>() {}.type)
        return anteriores.map { anterior ->
            Cliente(
                id = anterior.id,
                nombre = anterior.nombre,
                instalaciones = anterior.instalaciones.map { Instalacion(it, anterior.tiposEquipo) },
            )
        }
    }

    // Clientes de prueba creados la primera vez que se abre la app
    private fun clientesDePrueba() = listOf(
        Cliente(
            id = UUID.randomUUID().toString(),
            nombre = "Jumbo",
            instalaciones = listOf(
                Instalacion("Jumbo Costanera Center", listOf("Transformador", "Tablero Eléctrico")),
                Instalacion("Jumbo La Florida", listOf("Transformador", "Banco de Condensadores")),
            ),
        ),
        Cliente(
            id = UUID.randomUUID().toString(),
            nombre = "Falabella",
            instalaciones = listOf(
                Instalacion("Falabella Parque Arauco", listOf("Celda de Media Tensión", "Grupo Electrógeno")),
                Instalacion("Centro de Distribución Quilicura", listOf("Transformador", "Celda de Media Tensión")),
            ),
        ),
    )

    private fun escribir(clientes: List<Cliente>) {
        prefs.edit().putString(KEY_CLIENTES, gson.toJson(clientes)).apply()
        _clientes.value = clientes
    }

    private data class ClienteV1(
        val id: String = "",
        val nombre: String = "",
        val instalaciones: List<String> = emptyList(),
        val tiposEquipo: List<String> = emptyList(),
    )

    private companion object {
        const val PREFS_NAME = "clientes_prefs"
        const val KEY_CLIENTES_V1 = "clientes"
        const val KEY_CLIENTES = "clientes_v2"
    }
}

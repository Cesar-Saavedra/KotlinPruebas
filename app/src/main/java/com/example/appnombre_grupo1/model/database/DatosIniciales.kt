package com.example.appnombre_grupo1.model.database

import android.content.Context
import com.example.appnombre_grupo1.model.Cliente
import com.example.appnombre_grupo1.model.Instalacion
import com.example.appnombre_grupo1.model.RegistroGuardado
import com.example.appnombre_grupo1.model.RolUsuario
import com.example.appnombre_grupo1.model.database.entity.ClienteEntity
import com.example.appnombre_grupo1.model.database.entity.UsuarioEntity
import com.example.appnombre_grupo1.model.hashPassword
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

// Llena la base de datos la primera vez que se crea. Si el teléfono tiene datos de la
// versión anterior de la app (SharedPreferences), los pasa a SQLite; si no, crea datos de prueba.
class DatosIniciales(private val context: Context, private val gson: Gson) {

    suspend fun cargar(database: InduztekDatabase) {
        val usuarios = leerUsuariosAnteriores() ?: usuariosDePrueba()
        usuarios.forEach { database.usuarioDao().guardar(it) }

        val clientes = leerClientesAnteriores() ?: clientesDePrueba()
        clientes.forEach { cliente ->
            database.clienteDao().guardarConInstalaciones(
                cliente = ClienteEntity(id = cliente.id, nombre = cliente.nombre),
                instalaciones = cliente.instalaciones.map { it.nombre to it.tiposEquipo },
            )
        }

        leerRegistrosAnteriores().forEach { database.registroDao().guardar(it.toEntity()) }

        borrarDatosAnteriores()
    }

    private fun usuariosDePrueba() = listOf(
        UsuarioEntity("admin", "Administrador", RolUsuario.ADMINISTRADOR, hashPassword("admin")),
        UsuarioEntity("user", "Usuario de Prueba", RolUsuario.TECNICO, hashPassword("user")),
    )

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

    // --- Lectura de los datos guardados por la versión anterior (JSON con Gson) ---

    private fun leerUsuariosAnteriores(): List<UsuarioEntity>? {
        val json = prefs(PREFS_USUARIOS).getString("usuarios", null) ?: return null
        return runCatching {
            gson.fromJson<List<UsuarioEntity>>(json, object : TypeToken<List<UsuarioEntity>>() {}.type)
        }.getOrNull()?.takeIf { it.isNotEmpty() }
    }

    private fun leerClientesAnteriores(): List<Cliente>? {
        val prefs = prefs(PREFS_CLIENTES)
        prefs.getString("clientes_v2", null)?.let { json ->
            return runCatching {
                gson.fromJson<List<Cliente>>(json, object : TypeToken<List<Cliente>>() {}.type)
            }.getOrNull()
        }
        // Formato más antiguo: los equipos eran del cliente; se copian a cada instalación
        val json = prefs.getString("clientes", null) ?: return null
        return runCatching {
            gson.fromJson<List<ClienteV1>>(json, object : TypeToken<List<ClienteV1>>() {}.type)
                .map { v1 -> Cliente(v1.id, v1.nombre, v1.instalaciones.map { Instalacion(it, v1.tiposEquipo) }) }
        }.getOrNull()
    }

    private fun leerRegistrosAnteriores(): List<RegistroGuardado> {
        val json = prefs(PREFS_REGISTROS).getString("registros", null) ?: return emptyList()
        return runCatching {
            gson.fromJson<List<RegistroGuardado>>(json, object : TypeToken<List<RegistroGuardado>>() {}.type)
        }.getOrDefault(emptyList())
    }

    // La sesión iniciada sigue en "usuarios_prefs", solo se borran las listas ya migradas
    private fun borrarDatosAnteriores() {
        prefs(PREFS_USUARIOS).edit().remove("usuarios").apply()
        prefs(PREFS_CLIENTES).edit().clear().apply()
        prefs(PREFS_REGISTROS).edit().clear().apply()
    }

    private fun prefs(nombre: String) = context.getSharedPreferences(nombre, Context.MODE_PRIVATE)

    private data class ClienteV1(
        val id: String = "",
        val nombre: String = "",
        val instalaciones: List<String> = emptyList(),
        val tiposEquipo: List<String> = emptyList(),
    )

    private companion object {
        const val PREFS_USUARIOS = "usuarios_prefs"
        const val PREFS_CLIENTES = "clientes_prefs"
        const val PREFS_REGISTROS = "registros_prefs"
    }
}

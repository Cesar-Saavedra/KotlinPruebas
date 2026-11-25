package com.example.appnombre_grupo1.data.model.database

import android.content.Context
import com.example.appnombre_grupo1.data.model.Cliente
import com.example.appnombre_grupo1.data.model.Instalacion
import com.example.appnombre_grupo1.data.model.RegistroGuardado
import com.example.appnombre_grupo1.data.model.RolUsuario
import com.example.appnombre_grupo1.data.model.TipoEquipo
import com.example.appnombre_grupo1.data.model.database.dao.ClienteDao
import com.example.appnombre_grupo1.data.model.database.entity.ClienteEntity
import com.example.appnombre_grupo1.data.model.database.entity.UsuarioEntity
import com.example.appnombre_grupo1.data.model.hashPassword
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

class DatosIniciales(private val context: Context, private val gson: Gson) {

    suspend fun cargar(database: InduztekDatabase) {
        val usuariosAnteriores = leerUsuariosAnteriores()?.takeIf { it.isNotEmpty() }
        val usuarios = if (!usuariosAnteriores.isNullOrEmpty()) usuariosAnteriores else usuariosDePrueba()
        usuarios.forEach { database.usuarioDao().guardar(it) }

        val clientesAnteriores = leerClientesAnteriores()?.takeIf { it.isNotEmpty() }
        val clientes = if (!clientesAnteriores.isNullOrEmpty()) clientesAnteriores else clientesDePrueba()
        clientes.forEach { cliente ->
            database.clienteDao().guardarConInstalaciones(
                cliente = ClienteEntity(id = cliente.id, nombre = cliente.nombre),
                instalaciones = cliente.instalaciones.map { inst ->
                    ClienteDao.DatosInstalacionGuardar(
                        remoteId = inst.id,
                        nombre = inst.nombre,
                        tipos = inst.tiposEquipo.map { tipo ->
                            ClienteDao.DatosTipoEquipoGuardar(
                                remoteId = tipo.id,
                                nombre = tipo.nombre,
                            )
                        },
                        latitud = inst.latitud,
                        longitud = inst.longitud,
                    )
                },
            )
        }


        leerRegistrosAnteriores().forEach { database.registroDao().guardar(it.toEntity()) }

        borrarDatosAnteriores()
    }

    private fun usuariosDePrueba() = listOf(
        UsuarioEntity("admin1","admin", "Administrador", RolUsuario.ADMINISTRADOR, hashPassword("admin")),
        UsuarioEntity("user1","user", "Usuario de Prueba", RolUsuario.TECNICO, hashPassword("user")),
    )

    private fun clientesDePrueba() = listOf(
        Cliente(
            id = "cli-jumbo",
            nombre = "Jumbo",
            instalaciones = listOf(
                Instalacion("Jumbo-Costanera-1","Jumbo Costanera Center", listOf(TipoEquipo("123","Transformador")), latitud = -33.4173, longitud = -70.6062),
                Instalacion("Jumbo-La-Florida-2","Jumbo La Florida", listOf(
                    TipoEquipo("4324","Transformador"), TipoEquipo("38293","Banco de Condensadores")), latitud = -33.5225, longitud = -70.5982),
            ),
        ),
        Cliente(
            id = "cli-falabella",
            nombre = "Falabella",
            instalaciones = listOf(
                Instalacion("Falabella-Par-Ara-3","Falabella Parque Arauco", listOf(
                    TipoEquipo("23423","Celda de Media Tensión"), TipoEquipo("84398","Grupo Electrógeno")), latitud = -33.4018, longitud = -70.5786),
                Instalacion("Centro-Quili-5","Centro de Distribución Quilicura", listOf(TipoEquipo("935489","Transformador")), latitud = -33.3610, longitud = -70.7320),
            ),
        ),
    )

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
            }.getOrNull()?.takeIf { it.isNotEmpty() }
        }
        val json = prefs.getString("clientes", null) ?: return null
        return runCatching {
            gson.fromJson<List<ClienteV1>>(json, object : TypeToken<List<ClienteV1>>() {}.type)
                .map { v1 -> Cliente(v1.id, v1.nombre, v1.instalaciones.map { Instalacion(v1.id,v1.nombre, v1.tiposEquipo) }) }
        }.getOrNull()?.takeIf { it.isNotEmpty() }
    }

    private fun leerRegistrosAnteriores(): List<RegistroGuardado> {
        val json = prefs(PREFS_REGISTROS).getString("registros", null) ?: return emptyList()
        return runCatching {
            gson.fromJson<List<RegistroGuardado>>(json, object : TypeToken<List<RegistroGuardado>>() {}.type)
        }.getOrDefault(emptyList())
    }

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
        val tiposEquipo: List<TipoEquipo> = emptyList(),
    )

    private companion object {
        const val PREFS_USUARIOS = "usuarios_prefs"
        const val PREFS_CLIENTES = "clientes_prefs"
        const val PREFS_REGISTROS = "registros_prefs"
    }
}

package com.example.appnombre_grupo1.model.repository

import android.content.Context
import com.example.appnombre_grupo1.model.RolUsuario
import com.example.appnombre_grupo1.model.Usuario
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

// Guarda los usuarios y la sesión activa en el almacenamiento interno del teléfono
// (SharedPreferences). Las contraseñas se guardan como hash SHA-256, nunca en texto plano.
class UsuarioRepository(context: Context, private val gson: Gson) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _usuarios = MutableStateFlow(leerUsuarios())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    // Username del usuario con sesión iniciada
    private val _sesion = MutableStateFlow(prefs.getString(KEY_SESION, null))
    val sesion: StateFlow<String?> = _sesion.asStateFlow()

    // Devuelve el usuario si las credenciales son correctas, o null si no lo son
    fun autenticar(username: String, password: String): Usuario? =
        leerGuardados()
            .firstOrNull { it.username == username.trim() && it.passwordHash == hash(password) }
            ?.toUsuario()

    fun guardarSesion(usuario: Usuario) {
        prefs.edit().putString(KEY_SESION, usuario.username).apply()
        _sesion.value = usuario.username
    }

    fun cerrarSesion() {
        prefs.edit().remove(KEY_SESION).apply()
        _sesion.value = null
    }

    fun existe(username: String): Boolean = leerGuardados().any { it.username == username }

    // Crea o actualiza un usuario; si password es null se mantiene la contraseña actual
    fun guardarUsuario(usuario: Usuario, password: String?) {
        val guardados = leerGuardados()
        val anterior = guardados.firstOrNull { it.username == usuario.username }
        val passwordHash = password?.let { hash(it) } ?: anterior?.passwordHash ?: return
        val nuevo = UsuarioGuardado(usuario.username, usuario.nombre, usuario.rol, passwordHash)

        val actualizados = if (anterior == null) {
            guardados + nuevo
        } else {
            guardados.map { if (it.username == usuario.username) nuevo else it }
        }
        escribir(actualizados)
    }

    fun eliminarUsuario(username: String) {
        escribir(leerGuardados().filterNot { it.username == username })
    }

    private fun leerUsuarios(): List<Usuario> = leerGuardados().map { it.toUsuario() }

    private fun leerGuardados(): List<UsuarioGuardado> {
        val json = prefs.getString(KEY_USUARIOS, null)
        if (json == null) {
            // La primera vez que se abre la app se crean los usuarios de prueba
            val iniciales = listOf(
                UsuarioGuardado("admin", "Administrador", RolUsuario.ADMINISTRADOR, hash("admin")),
                UsuarioGuardado("user", "Usuario de Prueba", RolUsuario.TECNICO, hash("user")),
            )
            prefs.edit().putString(KEY_USUARIOS, gson.toJson(iniciales)).apply()
            return iniciales
        }
        return gson.fromJson(json, object : TypeToken<List<UsuarioGuardado>>() {}.type)
    }

    private fun escribir(usuarios: List<UsuarioGuardado>) {
        prefs.edit().putString(KEY_USUARIOS, gson.toJson(usuarios)).apply()
        _usuarios.value = usuarios.map { it.toUsuario() }
    }

    private fun hash(password: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private data class UsuarioGuardado(
        val username: String = "",
        val nombre: String = "",
        val rol: RolUsuario = RolUsuario.TECNICO,
        val passwordHash: String = "",
    ) {
        fun toUsuario() = Usuario(username, nombre, rol)
    }

    private companion object {
        const val PREFS_NAME = "usuarios_prefs"
        const val KEY_USUARIOS = "usuarios"
        const val KEY_SESION = "usuario_sesion"
    }
}

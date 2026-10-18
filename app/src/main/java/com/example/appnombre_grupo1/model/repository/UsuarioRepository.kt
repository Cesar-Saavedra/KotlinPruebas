package com.example.appnombre_grupo1.model.repository

import android.content.Context
import com.example.appnombre_grupo1.model.RolUsuario
import com.example.appnombre_grupo1.model.Usuario
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

// Guarda los usuarios y la sesión activa en el almacenamiento interno del teléfono
// (SharedPreferences). Las contraseñas se guardan como hash SHA-256, nunca en texto plano.
class UsuarioRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        // La primera vez que se abre la app se crean los usuarios de prueba
        if (!prefs.contains(KEY_USUARIOS)) {
            guardarUsuarios(
                listOf(
                    UsuarioGuardado(Usuario("admin", "Administrador", RolUsuario.ADMINISTRADOR), hash("admin")),
                    UsuarioGuardado(Usuario("user", "Usuario de Prueba", RolUsuario.TECNICO), hash("user")),
                )
            )
        }
    }

    // Devuelve el usuario si las credenciales son correctas, o null si no lo son
    fun autenticar(username: String, password: String): Usuario? =
        leerUsuarios()
            .firstOrNull { it.usuario.username == username.trim() && it.passwordHash == hash(password) }
            ?.usuario

    fun guardarSesion(usuario: Usuario) {
        prefs.edit().putString(KEY_SESION, usuario.username).apply()
    }

    fun usuarioConSesion(): Usuario? {
        val username = prefs.getString(KEY_SESION, null) ?: return null
        return leerUsuarios().firstOrNull { it.usuario.username == username }?.usuario
    }

    fun cerrarSesion() {
        prefs.edit().remove(KEY_SESION).apply()
    }

    private fun leerUsuarios(): List<UsuarioGuardado> {
        val json = JSONArray(prefs.getString(KEY_USUARIOS, "[]"))
        return (0 until json.length()).map { i ->
            val obj = json.getJSONObject(i)
            UsuarioGuardado(
                usuario = Usuario(
                    username = obj.getString("username"),
                    nombre = obj.getString("nombre"),
                    rol = RolUsuario.valueOf(obj.getString("rol")),
                ),
                passwordHash = obj.getString("passwordHash"),
            )
        }
    }

    private fun guardarUsuarios(usuarios: List<UsuarioGuardado>) {
        val json = JSONArray()
        usuarios.forEach {
            json.put(
                JSONObject()
                    .put("username", it.usuario.username)
                    .put("nombre", it.usuario.nombre)
                    .put("rol", it.usuario.rol.name)
                    .put("passwordHash", it.passwordHash)
            )
        }
        prefs.edit().putString(KEY_USUARIOS, json.toString()).apply()
    }

    private fun hash(password: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private data class UsuarioGuardado(val usuario: Usuario, val passwordHash: String)

    private companion object {
        const val PREFS_NAME = "usuarios_prefs"
        const val KEY_USUARIOS = "usuarios"
        const val KEY_SESION = "usuario_sesion"
    }
}

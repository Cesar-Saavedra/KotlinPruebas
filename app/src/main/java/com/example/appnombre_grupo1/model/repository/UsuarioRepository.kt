package com.example.appnombre_grupo1.model.repository

import android.content.Context
import com.example.appnombre_grupo1.model.Usuario
import com.example.appnombre_grupo1.model.database.dao.UsuarioDao
import com.example.appnombre_grupo1.model.database.entity.UsuarioEntity
import com.example.appnombre_grupo1.model.database.toModel
import com.example.appnombre_grupo1.model.hashPassword
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

// Los usuarios se guardan en la tabla "usuarios" (Room). La sesión activa es solo un dato
// de preferencia, así que sigue en SharedPreferences.
class UsuarioRepository(context: Context, private val dao: UsuarioDao) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val usuarios: Flow<List<Usuario>> = dao.observarTodos().map { lista -> lista.map { it.toModel() } }

    // Username del usuario con sesión iniciada
    private val _sesion = MutableStateFlow(prefs.getString(KEY_SESION, null))
    val sesion: StateFlow<String?> = _sesion.asStateFlow()

    // Devuelve el usuario si las credenciales son correctas, o null si no lo son
    suspend fun autenticar(username: String, password: String): Usuario? =
        dao.buscar(username.trim())
            ?.takeIf { it.passwordHash == hashPassword(password) }
            ?.toModel()

    fun guardarSesion(usuario: Usuario) {
        prefs.edit().putString(KEY_SESION, usuario.username).apply()
        _sesion.value = usuario.username
    }

    fun cerrarSesion() {
        prefs.edit().remove(KEY_SESION).apply()
        _sesion.value = null
    }

    // Crea o actualiza un usuario; si password es null se mantiene la contraseña actual
    suspend fun guardarUsuario(usuario: Usuario, password: String?) {
        val passwordHash = password?.let { hashPassword(it) }
            ?: dao.buscar(usuario.username)?.passwordHash
            ?: return
        dao.guardar(UsuarioEntity(usuario.username, usuario.nombre, usuario.rol, passwordHash))
    }

    suspend fun eliminarUsuario(username: String) {
        dao.eliminar(username)
    }

    private companion object {
        const val PREFS_NAME = "usuarios_prefs"
        const val KEY_SESION = "usuario_sesion"
    }
}

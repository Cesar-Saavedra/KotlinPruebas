package com.example.appnombre_grupo1.model.repository

import android.content.Context
import com.example.appnombre_grupo1.model.Usuario
import com.example.appnombre_grupo1.model.database.dao.UsuarioDao
import com.example.appnombre_grupo1.model.database.entity.UsuarioEntity
import com.example.appnombre_grupo1.model.database.toModel
import com.example.appnombre_grupo1.model.hashPassword
import com.example.appnombre_grupo1.model.remote.ApiService
import com.example.appnombre_grupo1.model.remote.LoginRequestDto
import com.example.appnombre_grupo1.model.remote.MENSAJE_SIN_CONEXION
import com.example.appnombre_grupo1.model.remote.RespuestaApi
import com.example.appnombre_grupo1.model.remote.UsuarioDto
import com.example.appnombre_grupo1.model.remote.UsuarioRequestDto
import com.example.appnombre_grupo1.model.remote.llamarApi
import com.example.appnombre_grupo1.model.remote.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import retrofit2.Response

// Usuarios: el servidor es la fuente de verdad y Room guarda una copia para trabajar sin conexión.
// La sesión activa sigue en SharedPreferences.
class UsuarioRepository(context: Context, private val dao: UsuarioDao, private val api: ApiService) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val usuarios: Flow<List<Usuario>> = dao.observarTodos().map { lista -> lista.map { it.toModel() } }

    private val _sesion = MutableStateFlow(prefs.getString(KEY_SESION, null))
    val sesion: StateFlow<String?> = _sesion.asStateFlow()

    // Valida con el servidor; si no hay conexión, con la copia local. null = credenciales incorrectas
    suspend fun autenticar(username: String, password: String): Usuario? {
        val usuario = username.trim()
        return when (val respuesta = llamarApi { api.login(LoginRequestDto(usuario, password)) }) {
            is RespuestaApi.Exito -> {
                guardarLocal(respuesta.datos, hashPassword(password))
                respuesta.datos.toModel()
            }
            is RespuestaApi.Error -> if (respuesta.codigo == 401) null else autenticarLocal(usuario, password)
            RespuestaApi.SinConexion -> autenticarLocal(usuario, password)
        }
    }

    fun guardarSesion(usuario: Usuario) {
        prefs.edit().putString(KEY_SESION, usuario.username).apply()
        _sesion.value = usuario.username
    }

    fun cerrarSesion() {
        prefs.edit().remove(KEY_SESION).apply()
        _sesion.value = null
    }

    // Devuelven null si se guardó, o el mensaje de error
    suspend fun crearUsuario(usuario: Usuario, password: String): String? =
        guardarEnServidor(password) {
            api.crearUsuario(UsuarioRequestDto(usuario.username, usuario.nombre, usuario.rol, password))
        }

    suspend fun editarUsuario(usuario: Usuario, password: String?): String? =
        guardarEnServidor(password) {
            api.editarUsuario(usuario.username, UsuarioRequestDto(null, usuario.nombre, usuario.rol, password))
        }

    suspend fun eliminarUsuario(username: String): String? =
        when (val respuesta = llamarApi { api.eliminarUsuario(username) }) {
            is RespuestaApi.Exito -> { dao.eliminar(username); null }
            is RespuestaApi.Error -> respuesta.mensaje
            RespuestaApi.SinConexion -> MENSAJE_SIN_CONEXION
        }

    // Descarga los usuarios del servidor. false si no hubo conexión
    suspend fun descargar(): Boolean {
        val respuesta = llamarApi { api.obtenerUsuarios() }
        if (respuesta !is RespuestaApi.Exito) return false
        dao.reemplazarTodos(respuesta.datos.map { UsuarioEntity(it.username, it.nombre, it.toModel().rol, "") })
        return true
    }

    private suspend fun guardarEnServidor(
        password: String?,
        llamada: suspend () -> Response<UsuarioDto>,
    ): String? = when (val respuesta = llamarApi(llamada)) {
        is RespuestaApi.Exito -> { guardarLocal(respuesta.datos, password?.let { hashPassword(it) }); null }
        is RespuestaApi.Error -> respuesta.mensaje
        RespuestaApi.SinConexion -> MENSAJE_SIN_CONEXION
    }

    // Si no llega una contraseña nueva, se conserva el hash local
    private suspend fun guardarLocal(dto: UsuarioDto, passwordHash: String?) {
        val hash = passwordHash ?: dao.buscar(dto.username)?.passwordHash.orEmpty()
        dao.guardar(UsuarioEntity(dto.username, dto.nombre, dto.toModel().rol, hash))
    }

    private suspend fun autenticarLocal(username: String, password: String): Usuario? =
        dao.buscar(username)
            ?.takeIf { it.passwordHash.isNotEmpty() && it.passwordHash == hashPassword(password) }
            ?.toModel()

    private companion object {
        const val PREFS_NAME = "usuarios_prefs"
        const val KEY_SESION = "usuario_sesion"
    }
}

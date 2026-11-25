package com.example.appnombre_grupo1.data.model.repository

import android.content.Context
import com.example.appnombre_grupo1.data.model.RolUsuario
import com.example.appnombre_grupo1.data.model.Usuario
import com.example.appnombre_grupo1.data.model.database.dao.UsuarioDao
import com.example.appnombre_grupo1.data.model.database.entity.UsuarioEntity
import com.example.appnombre_grupo1.data.model.database.toModel
import com.example.appnombre_grupo1.data.model.hashPassword
import com.example.appnombre_grupo1.data.remote.ApiService
import com.example.appnombre_grupo1.data.remote.dto.LoginRequestDto
import com.example.appnombre_grupo1.data.remote.dto.UsuarioDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import android.util.Log

// Los usuarios se guardan en la tabla "usuarios" (Room) y se sincronizan con la API REST (Retrofit).
class UsuarioRepository(
    context: Context,
    private val dao: UsuarioDao,
    private val apiService: ApiService,
) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val usuarios: Flow<List<Usuario>> = dao.observarTodos().map { lista ->
        lista.map { it.toModel() }
            .groupBy { it.username.trim().lowercase() }
            .map { (_, grupo) ->
                grupo.firstOrNull { it.id.isNotBlank() } ?: grupo.first()
            }
    }
    // Username del usuario con sesión iniciada
    private val _sesion = MutableStateFlow(prefs.getString(KEY_SESION, null))
    val sesion: StateFlow<String?> = _sesion.asStateFlow()

    private val _sesionId = MutableStateFlow(prefs.getString(KEY_SESION_ID, null))
    val sesionId: StateFlow<String?> = _sesionId.asStateFlow()

    suspend fun refrescarUsuarios(): Result<Unit> {
        return try {
            val response = apiService.getUsuarios()
            if (!response.isSuccessful) {
                return Result.failure(Exception("Error al obtener usuarios (HTTP ${response.code()})"))
            }

            val remotos = response.body().orEmpty()
            remotos.forEach { dto ->
                val existente = dao.buscar(dto.username)
                val rol = dto.rol ?: existente?.rol ?: RolUsuario.TECNICO
                val nombre = dto.nombre.ifBlank { existente?.nombre ?: dto.username }
                val passwordHash = existente?.passwordHash ?: ""
                val idUsuario = dto.id?.takeIf { it.isNotBlank() } ?: existente?.id.orEmpty()
                if (existente != null && existente.id != idUsuario) {
                    dao.eliminar(existente.username)
                }
                dao.guardar(
                    UsuarioEntity(
                        id = idUsuario,
                        username = dto.username,
                        nombre = nombre,
                        rol = rol,
                        passwordHash = passwordHash,
                    )
                )

            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Intenta autenticar remotamente con Retrofit; si la API no responde o falla, realiza autenticación local (Room)
    suspend fun autenticar(username: String, password: String): Usuario? {
        val trimmedUser = username.trim()
        try {
            val response = apiService.login(LoginRequestDto(trimmedUser, password))
            if (response.isSuccessful) {
                val body = response.body()
                val resolvedUsername = body?.username
                val resolvedRol = body?.rol
                if (body != null && resolvedUsername != null && resolvedRol != null) {
                    val existente = dao.buscar(resolvedUsername)
                    val idUsuario = body.id?.takeIf { it.isNotBlank() } ?: existente?.id.orEmpty()
                    val nombreRemoto = body.name?.takeIf { it.isNotBlank() }
                    val nombreLocal = existente?.nombre?.takeIf { it.isNotBlank() && it != resolvedUsername }
                    val nombre = nombreRemoto ?: nombreLocal ?: resolvedUsername
                    val usuario = Usuario(
                        id = idUsuario,
                        username = resolvedUsername,
                        nombre = nombre,
                        rol = resolvedRol,
                    )
                    val pwdHash = hashPassword(password)
                    if (existente != null && existente.id != idUsuario) {
                        dao.eliminar(existente.username)
                    }
                    dao.guardar(
                        UsuarioEntity(
                            id = idUsuario,
                            username = resolvedUsername,
                            nombre = nombre,
                            rol = resolvedRol,
                            passwordHash = pwdHash,
                        )
                    )
                    return usuario
                }
            }
        } catch (_: Exception) {
            // Fallback a autenticación local si el servidor remoto no está disponible
        }

        return dao.buscar(trimmedUser)
            ?.takeIf { it.passwordHash == hashPassword(password) }
            ?.toModel()
    }
    fun guardarSesion(usuario: Usuario) {
        prefs.edit()
            .putString(KEY_SESION, usuario.username)
            .putString(KEY_SESION_ID, usuario.id)
            .putString(KEY_SESION_ROL, usuario.rol.name)
            .apply()
        _sesion.value = usuario.username
        _sesionId.value = usuario.id
    }

    fun cerrarSesion() {
        prefs.edit()
            .remove(KEY_SESION)
            .remove(KEY_SESION_ID)
            .remove(KEY_SESION_ROL)
            .apply()
        _sesion.value = null
        _sesionId.value = null
    }

    fun obtenerRolSesion(): RolUsuario? {
        val rolGuardado = prefs.getString(KEY_SESION_ROL, null) ?: return null
        return runCatching { RolUsuario.valueOf(rolGuardado) }.getOrNull()
    }

    // Crea o actualiza un usuario en la API remota y luego actualiza Room
    suspend fun guardarUsuario(usuario: Usuario, password: String?) {
        val existente = dao.buscar(usuario.username)
        val passwordHash = password?.takeIf { it.isNotBlank() }?.let { hashPassword(it) }
            ?: existente?.passwordHash
            ?: return

        // 1. Guardar siempre primero en Room
        if (existente != null && existente.id != usuario.id) {
            dao.eliminar(existente.username)
        }

        dao.guardar(
            UsuarioEntity(
                id = usuario.id,
                username = usuario.username,
                nombre = usuario.nombre,
                rol = usuario.rol,
                passwordHash = passwordHash,
            )
        )

        // 2. Intentar sincronizar con el backend vía Retrofit sin romper Room si falla
        try {
            val passwordParaApi = password?.takeIf { it.isNotBlank() }
            val idParaRuta = existente?.id?.takeIf { it.isNotBlank() }
                ?: usuario.id.takeIf { it.isNotBlank() }
                ?: usuario.username

            val usuarioDto = UsuarioDto(
                id = idParaRuta,
                username = usuario.username,
                nombre = usuario.nombre,
                rol = usuario.rol,
                password = passwordParaApi,
            )

            val resp = if (existente == null) {
                apiService.crearUsuario(usuarioDto).also {
                    Log.d("SYNC_USUARIO", "crearUsuario -> code=${it.code()}, ok=${it.isSuccessful}")
                }
            } else {
                apiService.actualizarUsuario(
                    id = idParaRuta,
                    currentUser = _sesion.value ?: "admin",
                    usuarioDto = usuarioDto,
                ).also {
                    Log.d("SYNC_USUARIO", "actualizarUsuario -> code=${it.code()}, ok=${it.isSuccessful}, body=${it.body()}")
                }
            }

            if (resp.isSuccessful) {
                val actualizadoRemoto = resp.body()
                val usuarioGuardado = if (actualizadoRemoto != null) {
                    actualizadoRemoto.copy(
                        id = actualizadoRemoto.id.ifBlank { idParaRuta },
                        nombre = actualizadoRemoto.nombre.ifBlank { usuario.nombre },
                        rol = actualizadoRemoto.rol,
                    )
                } else {
                    usuario.copy(id = idParaRuta)
                }

                if (usuarioGuardado.id != usuario.id && usuario.id.isNotBlank()) {
                    dao.eliminar(usuario.username)
                }

                dao.guardar(
                    UsuarioEntity(
                        id = usuarioGuardado.id,
                        username = usuarioGuardado.username,
                        nombre = usuarioGuardado.nombre,
                        rol = usuarioGuardado.rol,
                        passwordHash = passwordHash,
                    )
                )
            }
        } catch (_: Exception) {
            // Queda guardado en Room para sincronizar después
        }
    }


    suspend fun eliminarUsuario(username: String) {
        // 1. Eliminar primero en Room
        dao.eliminar(username)

        // 2. Intentar sincronizar eliminación con Retrofit
        try {
            apiService.eliminarUsuario(username)
        } catch (_: Exception) {
            // Queda eliminado en Room
        }
    }


    private companion object {
        const val PREFS_NAME = "usuarios_prefs"
        const val KEY_SESION = "usuario_sesion"

        const val KEY_SESION_ID = "usuario_sesion_id"
        const val KEY_SESION_ROL = "usuario_sesion_rol"
    }
}

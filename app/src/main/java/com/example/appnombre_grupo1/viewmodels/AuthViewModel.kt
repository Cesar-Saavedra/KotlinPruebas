package com.example.appnombre_grupo1.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appnombre_grupo1.data.model.LoginForm
import com.example.appnombre_grupo1.data.model.Usuario
import com.example.appnombre_grupo1.di.container
import com.example.appnombre_grupo1.data.model.RolUsuario
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log

// Resultado de leer la sesión guardada; usuario es null si no hay sesión iniciada
data class SesionLeida(val usuario: Usuario?)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = container.usuarioRepository

    private val _loginForm = MutableStateFlow(LoginForm())
    val loginForm: StateFlow<LoginForm> = _loginForm.asStateFlow()

    // Busca en la base de datos al usuario guardado como sesión; se mantiene al día
    // si el administrador edita sus datos
    private val usuarioConSesion = combine(
        repository.usuarios,
        repository.sesion,
        repository.sesionId,
    ) { usuarios, sesion, sesionId ->
        val encontrado = usuarios.firstOrNull { it.username == sesion }
            ?: usuarios.firstOrNull { sesionId != null && it.id == sesionId }

        Log.d(
            "SESION_DEBUG",
            "sesion=$sesion, sesionId=$sesionId, encontrado=${encontrado?.username}:${encontrado?.id}:${encontrado?.rol}, prefsRol=${repository.obtenerRolSesion()}"
        )

        if (encontrado != null) {
            if (!sesionId.isNullOrBlank() && encontrado.id != sesionId) {
                encontrado.copy(id = sesionId)
            } else {
                encontrado
            }
        } else if (!sesion.isNullOrBlank() && !sesionId.isNullOrBlank()) {
            Usuario(
                id = sesionId,
                username = sesion,
                nombre = sesion,
                rol = repository.obtenerRolSesion() ?: RolUsuario.TECNICO,
            )
        } else {
            null
        }
    }



    // null hasta la primera lectura de la base de datos al abrir la app
    val sesion: StateFlow<SesionLeida?> =
        usuarioConSesion.map { SesionLeida(it) }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val usuarioActual: StateFlow<Usuario?> =
        sesion.map { it?.usuario }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Evento que se emite una vez cuando el inicio de sesión es correcto
    private val _inicioSesion = MutableSharedFlow<Usuario>()
    val inicioSesion: SharedFlow<Usuario> = _inicioSesion.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.refrescarUsuarios()
        }
    }

    fun actualizarUsername(username: String) {
        _loginForm.update { it.copy(username = username, error = null) }
    }

    fun actualizarPassword(password: String) {
        _loginForm.update { it.copy(password = password, error = null) }
    }

    fun iniciarSesion() {
        val form = _loginForm.value
        if (form.verificando) return
        if (form.username.isBlank() || form.password.isBlank()) {
            _loginForm.update { it.copy(error = "Ingrese usuario y contraseña") }
            return
        }

        // La autenticación intenta primero contra la API remota y luego contra Room
        viewModelScope.launch {
            _loginForm.update { it.copy(verificando = true) }
            val usuario = repository.autenticar(form.username, form.password)
            if (usuario == null) {
                _loginForm.update {
                    it.copy(password = "", error = "Usuario o contraseña incorrectos", verificando = false)
                }
                return@launch
            }

            repository.guardarSesion(usuario)
            _loginForm.value = LoginForm()
            _inicioSesion.emit(usuario)

            // Descargar datos desde el backend hacia Room al iniciar sesión
            viewModelScope.launch {
                try {
                    container.clienteRepository.refrescarClientes()
                } catch (_: Exception) {
                }
                try {
                    container.registroRepository.refrescarRegistros()
                } catch (_: Exception) {
                }
                try {
                    repository.refrescarUsuarios()
                } catch (_: Exception) {
                }
            }

        }
    }

    fun cerrarSesion() {
        repository.cerrarSesion()
    }
}

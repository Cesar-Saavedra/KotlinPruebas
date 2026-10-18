package com.example.appnombre_grupo1.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appnombre_grupo1.di.container
import com.example.appnombre_grupo1.model.LoginForm
import com.example.appnombre_grupo1.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = container.usuarioRepository

    private val _loginForm = MutableStateFlow(LoginForm())
    val loginForm: StateFlow<LoginForm> = _loginForm.asStateFlow()

    // Usuario con sesión iniciada; se recupera al abrir la app si no cerró sesión
    // y se mantiene al día si el administrador edita sus datos
    val usuarioActual: StateFlow<Usuario?> =
        combine(repository.usuarios, repository.sesion) { usuarios, sesion ->
            usuarios.firstOrNull { it.username == sesion }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = repository.usuarios.value.firstOrNull { it.username == repository.sesion.value },
        )

    fun actualizarUsername(username: String) {
        _loginForm.update { it.copy(username = username, error = null) }
    }

    fun actualizarPassword(password: String) {
        _loginForm.update { it.copy(password = password, error = null) }
    }

    // Devuelve el usuario si las credenciales son válidas y la sesión quedó iniciada
    fun iniciarSesion(): Usuario? {
        val form = _loginForm.value
        if (form.username.isBlank() || form.password.isBlank()) {
            _loginForm.update { it.copy(error = "Ingrese usuario y contraseña") }
            return null
        }

        val usuario = repository.autenticar(form.username, form.password)
        if (usuario == null) {
            _loginForm.update { it.copy(password = "", error = "Usuario o contraseña incorrectos") }
            return null
        }

        repository.guardarSesion(usuario)
        _loginForm.value = LoginForm()
        return usuario
    }

    fun cerrarSesion() {
        repository.cerrarSesion()
    }
}

package com.example.appnombre_grupo1.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.appnombre_grupo1.model.LoginForm
import com.example.appnombre_grupo1.model.Usuario
import com.example.appnombre_grupo1.model.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UsuarioRepository(application)

    private val _loginForm = MutableStateFlow(LoginForm())
    val loginForm: StateFlow<LoginForm> = _loginForm.asStateFlow()

    // Usuario con sesión iniciada; se recupera al abrir la app si no cerró sesión
    private val _usuarioActual = MutableStateFlow(repository.usuarioConSesion())
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    fun actualizarUsername(username: String) {
        _loginForm.update { it.copy(username = username, error = null) }
    }

    fun actualizarPassword(password: String) {
        _loginForm.update { it.copy(password = password, error = null) }
    }

    // Devuelve true si las credenciales son válidas y la sesión quedó iniciada
    fun iniciarSesion(): Boolean {
        val form = _loginForm.value
        if (form.username.isBlank() || form.password.isBlank()) {
            _loginForm.update { it.copy(error = "Ingrese usuario y contraseña") }
            return false
        }

        val usuario = repository.autenticar(form.username, form.password)
        if (usuario == null) {
            _loginForm.update { it.copy(password = "", error = "Usuario o contraseña incorrectos") }
            return false
        }

        repository.guardarSesion(usuario)
        _usuarioActual.value = usuario
        _loginForm.value = LoginForm()
        return true
    }

    fun cerrarSesion() {
        repository.cerrarSesion()
        _usuarioActual.value = null
    }
}

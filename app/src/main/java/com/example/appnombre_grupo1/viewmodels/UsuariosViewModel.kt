package com.example.appnombre_grupo1.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.appnombre_grupo1.di.container
import com.example.appnombre_grupo1.model.RolUsuario
import com.example.appnombre_grupo1.model.Usuario
import com.example.appnombre_grupo1.model.UsuarioForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UsuariosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = container.usuarioRepository

    val usuarios: StateFlow<List<Usuario>> = repository.usuarios

    private val _form = MutableStateFlow(UsuarioForm())
    val form: StateFlow<UsuarioForm> = _form.asStateFlow()

    fun nuevoUsuario() {
        _form.value = UsuarioForm()
    }

    fun editarUsuario(usuario: Usuario, usernameActual: String) {
        _form.value = UsuarioForm(
            esNuevo = false,
            username = usuario.username,
            nombre = usuario.nombre,
            rol = usuario.rol,
            esUsuarioActual = usuario.username == usernameActual,
        )
    }

    fun actualizarUsername(username: String) {
        // Sin espacios para que sea fácil de escribir al iniciar sesión
        _form.update { it.copy(username = username.replace(" ", ""), error = null) }
    }

    fun actualizarNombre(nombre: String) {
        _form.update { it.copy(nombre = nombre, error = null) }
    }

    fun actualizarRol(rol: RolUsuario) {
        _form.update { if (it.esUsuarioActual) it else it.copy(rol = rol) }
    }

    fun actualizarPassword(password: String) {
        _form.update { it.copy(password = password, error = null) }
    }

    // Devuelve true si el usuario se guardó correctamente
    fun guardar(): Boolean {
        val form = _form.value
        val error = when {
            form.username.isBlank() -> "Ingrese el nombre de usuario"
            form.esNuevo && repository.existe(form.username) -> "Ese nombre de usuario ya existe"
            form.nombre.isBlank() -> "Ingrese el nombre completo"
            form.esNuevo && form.password.length < 4 -> "La contraseña debe tener al menos 4 caracteres"
            !form.esNuevo && form.password.isNotEmpty() && form.password.length < 4 ->
                "La contraseña debe tener al menos 4 caracteres"
            else -> null
        }
        if (error != null) {
            _form.update { it.copy(error = error) }
            return false
        }

        repository.guardarUsuario(
            Usuario(username = form.username, nombre = form.nombre.trim(), rol = form.rol),
            password = form.password.ifEmpty { null },
        )
        return true
    }

    // El administrador no puede eliminarse a sí mismo
    fun eliminar() {
        val form = _form.value
        if (!form.esNuevo && !form.esUsuarioActual) {
            repository.eliminarUsuario(form.username)
        }
    }
}

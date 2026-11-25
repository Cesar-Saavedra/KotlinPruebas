package com.example.appnombre_grupo1.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appnombre_grupo1.di.container
import com.example.appnombre_grupo1.model.RolUsuario
import com.example.appnombre_grupo1.model.Usuario
import com.example.appnombre_grupo1.model.UsuarioForm
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UsuariosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = container.usuarioRepository

    // Lista que se actualiza sola cada vez que cambia la base de datos
    val usuarios: StateFlow<List<Usuario>> =
        repository.usuarios.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _form = MutableStateFlow(UsuarioForm())
    val form: StateFlow<UsuarioForm> = _form.asStateFlow()

    // Se emite cuando el servidor confirmó el guardado o la eliminación
    private val _operacionExitosa = MutableSharedFlow<Unit>()
    val operacionExitosa: SharedFlow<Unit> = _operacionExitosa.asSharedFlow()

    private var enProceso = false

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
    fun guardar() {
        if (enProceso) return
        val form = _form.value
        val error = when {
            form.username.isBlank() -> "Ingrese el nombre de usuario"
            form.esNuevo && usuarios.value.any { it.username == form.username } -> "Ese nombre de usuario ya existe"
            form.nombre.isBlank() -> "Ingrese el nombre completo"
            form.esNuevo && form.password.length < 4 -> "La contraseña debe tener al menos 4 caracteres"
            !form.esNuevo && form.password.isNotEmpty() && form.password.length < 4 ->
                "La contraseña debe tener al menos 4 caracteres"
            else -> null
        }
        if (error != null) {
            _form.update { it.copy(error = error) }
            return
        }
        val usuario = Usuario(username = form.username, nombre = form.nombre.trim(), rol = form.rol)
        ejecutar {
            if (form.esNuevo) repository.crearUsuario(usuario, form.password)
            else repository.editarUsuario(usuario, form.password.ifEmpty { null })
        }
    }

    // El administrador no puede eliminarse a sí mismo
    fun eliminar() {
        val form = _form.value
        if (form.esNuevo || form.esUsuarioActual || enProceso) return
        ejecutar { repository.eliminarUsuario(form.username) }
    }

    // operacion devuelve null si el servidor la aceptó, o el mensaje de error
    private fun ejecutar(operacion: suspend () -> String?) {
        enProceso = true
        viewModelScope.launch {
            val error = operacion()
            enProceso = false
            if (error == null) _operacionExitosa.emit(Unit) else _form.update { it.copy(error = error) }
        }
    }
}

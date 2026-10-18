package com.example.appnombre_grupo1.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.appnombre_grupo1.di.container
import com.example.appnombre_grupo1.model.Cliente
import com.example.appnombre_grupo1.model.ClienteForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ClientesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = container.clienteRepository

    val clientes: StateFlow<List<Cliente>> = repository.clientes

    private val _form = MutableStateFlow(ClienteForm())
    val form: StateFlow<ClienteForm> = _form.asStateFlow()

    // Prepara el formulario: vacío para un cliente nuevo o con los datos del cliente a editar
    fun nuevoCliente() {
        _form.value = ClienteForm()
    }

    fun editarCliente(cliente: Cliente) {
        _form.value = ClienteForm(
            id = cliente.id,
            nombre = cliente.nombre,
            instalaciones = cliente.instalaciones,
            tiposEquipo = cliente.tiposEquipo,
        )
    }

    fun actualizarNombre(nombre: String) {
        _form.update { it.copy(nombre = nombre, error = null) }
    }

    fun actualizarNuevaInstalacion(texto: String) {
        _form.update { it.copy(nuevaInstalacion = texto) }
    }

    fun actualizarNuevoTipoEquipo(texto: String) {
        _form.update { it.copy(nuevoTipoEquipo = texto) }
    }

    fun agregarInstalacion() {
        _form.update {
            val nueva = it.nuevaInstalacion.trim()
            if (nueva.isEmpty() || it.instalaciones.contiene(nueva)) it.copy(nuevaInstalacion = "")
            else it.copy(instalaciones = it.instalaciones + nueva, nuevaInstalacion = "", error = null)
        }
    }

    fun quitarInstalacion(instalacion: String) {
        _form.update { it.copy(instalaciones = it.instalaciones - instalacion) }
    }

    fun agregarTipoEquipo() {
        _form.update {
            val nuevo = it.nuevoTipoEquipo.trim()
            if (nuevo.isEmpty() || it.tiposEquipo.contiene(nuevo)) it.copy(nuevoTipoEquipo = "")
            else it.copy(tiposEquipo = it.tiposEquipo + nuevo, nuevoTipoEquipo = "", error = null)
        }
    }

    fun quitarTipoEquipo(tipo: String) {
        _form.update { it.copy(tiposEquipo = it.tiposEquipo - tipo) }
    }

    // Devuelve true si el cliente se guardó correctamente
    fun guardar(): Boolean {
        val form = _form.value
        val nombre = form.nombre.trim()
        val error = when {
            nombre.isEmpty() -> "Ingrese el nombre del cliente"
            clientes.value.any { it.id != form.id && it.nombre.equals(nombre, ignoreCase = true) } ->
                "Ya existe un cliente con ese nombre"
            form.instalaciones.isEmpty() -> "Agregue al menos una instalación"
            form.tiposEquipo.isEmpty() -> "Agregue al menos un tipo de equipo"
            else -> null
        }
        if (error != null) {
            _form.update { it.copy(error = error) }
            return false
        }

        repository.guardar(
            Cliente(
                id = form.id.orEmpty(),
                nombre = nombre,
                instalaciones = form.instalaciones,
                tiposEquipo = form.tiposEquipo,
            )
        )
        return true
    }

    fun eliminar() {
        _form.value.id?.let { repository.eliminar(it) }
    }

    private fun List<String>.contiene(valor: String) = any { it.equals(valor, ignoreCase = true) }
}

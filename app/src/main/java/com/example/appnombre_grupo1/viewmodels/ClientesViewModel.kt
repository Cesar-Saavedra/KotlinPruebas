package com.example.appnombre_grupo1.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appnombre_grupo1.di.container
import com.example.appnombre_grupo1.model.Cliente
import com.example.appnombre_grupo1.model.ClienteForm
import com.example.appnombre_grupo1.model.Instalacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClientesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = container.clienteRepository

    // Lista que se actualiza sola cada vez que cambia la base de datos
    val clientes: StateFlow<List<Cliente>> =
        repository.clientes.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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
        )
    }

    fun actualizarNombre(nombre: String) {
        _form.update { it.copy(nombre = nombre, error = null) }
    }

    fun actualizarNuevaInstalacion(texto: String) {
        _form.update { it.copy(nuevaInstalacion = texto) }
    }

    fun agregarInstalacion() {
        _form.update { form ->
            val nueva = form.nuevaInstalacion.trim()
            if (nueva.isEmpty() || form.instalaciones.any { it.nombre.equals(nueva, ignoreCase = true) }) {
                form.copy(nuevaInstalacion = "")
            } else {
                form.copy(
                    instalaciones = form.instalaciones + Instalacion(nombre = nueva),
                    nuevaInstalacion = "",
                    error = null,
                )
            }
        }
    }

    fun quitarInstalacion(instalacion: String) {
        _form.update { form ->
            form.copy(
                instalaciones = form.instalaciones.filterNot { it.nombre == instalacion },
                nuevoTipoEquipo = form.nuevoTipoEquipo - instalacion,
            )
        }
    }

    fun actualizarNuevoTipoEquipo(instalacion: String, texto: String) {
        _form.update { it.copy(nuevoTipoEquipo = it.nuevoTipoEquipo + (instalacion to texto)) }
    }

    fun agregarTipoEquipo(instalacion: String) {
        _form.update { form ->
            val nuevo = form.nuevoTipoEquipo[instalacion].orEmpty().trim()
            val limpio = form.copy(nuevoTipoEquipo = form.nuevoTipoEquipo - instalacion)
            if (nuevo.isEmpty()) return@update limpio

            limpio.copy(
                instalaciones = form.instalaciones.map { actual ->
                    if (actual.nombre != instalacion || actual.tiposEquipo.any { it.equals(nuevo, ignoreCase = true) }) {
                        actual
                    } else {
                        actual.copy(tiposEquipo = actual.tiposEquipo + nuevo)
                    }
                },
                error = null,
            )
        }
    }

    fun quitarTipoEquipo(instalacion: String, tipo: String) {
        _form.update { form ->
            form.copy(
                instalaciones = form.instalaciones.map {
                    if (it.nombre == instalacion) it.copy(tiposEquipo = it.tiposEquipo - tipo) else it
                },
            )
        }
    }

    fun actualizarCoordenadasInstalacion(instalacionNombre: String, latitud: Double?, longitud: Double?) {
        _form.update { form ->
            form.copy(
                instalaciones = form.instalaciones.map { inst ->
                    if (inst.nombre == instalacionNombre) {
                        inst.copy(latitud = latitud, longitud = longitud)
                    } else {
                        inst
                    }
                },
            )
        }
    }

    fun capturarUbicacionActualInstalacion(context: android.content.Context, instalacionNombre: String) {
        com.example.appnombre_grupo1.ui.utils.UbicacionUtils.obtenerUbicacionActual(context) { ubicacion, error ->
            if (ubicacion != null) {
                actualizarCoordenadasInstalacion(instalacionNombre, ubicacion.latitud, ubicacion.longitud)
            } else if (error != null) {
                _form.update { it.copy(error = error) }
            }
        }
    }

    // Devuelve true si el cliente se guardó correctamente
    fun guardar(): Boolean {
        val form = _form.value
        val nombre = form.nombre.trim()
        val instalacionSinEquipos = form.instalaciones.firstOrNull { it.tiposEquipo.isEmpty() }
        val error = when {
            nombre.isEmpty() -> "Ingrese el nombre del cliente"
            clientes.value.any { it.id != form.id && it.nombre.equals(nombre, ignoreCase = true) } ->
                "Ya existe un cliente con ese nombre"
            form.instalaciones.isEmpty() -> "Agregue al menos una instalación"
            instalacionSinEquipos != null ->
                "Agregue al menos un tipo de equipo a ${instalacionSinEquipos.nombre}"
            else -> null
        }
        if (error != null) {
            _form.update { it.copy(error = error) }
            return false
        }

        // Room no permite escribir en el hilo principal: se guarda en una corrutina
        viewModelScope.launch {
            repository.guardar(
                Cliente(
                    id = form.id.orEmpty(),
                    nombre = nombre,
                    instalaciones = form.instalaciones,
                ),
            )
        }
        return true
    }

    fun eliminar() {
        val id = _form.value.id ?: return
        viewModelScope.launch { repository.eliminar(id) }
    }
}

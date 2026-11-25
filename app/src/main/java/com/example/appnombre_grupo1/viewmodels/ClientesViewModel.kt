package com.example.appnombre_grupo1.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appnombre_grupo1.data.model.Cliente
import com.example.appnombre_grupo1.data.model.ClienteForm
import com.example.appnombre_grupo1.data.model.Instalacion
import com.example.appnombre_grupo1.data.model.TipoEquipo
import com.example.appnombre_grupo1.di.container
import com.example.appnombre_grupo1.ui.utils.UbicacionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log

class ClientesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = container.clienteRepository

    // Lista que se actualiza sola cada vez que cambia la base de datos
    val clientes: StateFlow<List<Cliente>> =
        repository.clientes.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _form = MutableStateFlow(ClienteForm())
    val form: StateFlow<ClienteForm> = _form.asStateFlow()

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    init {
        refrescar()
    }

    fun refrescar() {
        if (_sincronizando.value) return
        viewModelScope.launch {
            _sincronizando.value = true
            repository.refrescarClientes()
                .onFailure { throwable ->
                    _form.update {
                        it.copy(error = throwable.message ?: "No se pudieron sincronizar los clientes")
                    }
                }
            _sincronizando.value = false
        }
    }

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
                    if (actual.nombre != instalacion || actual.tiposEquipo.any { it.nombre.equals(nuevo, ignoreCase = true) }) {
                        actual
                    } else {
                        actual.copy(tiposEquipo = actual.tiposEquipo + TipoEquipo(nombre = nuevo))
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
                    if (it.nombre == instalacion) it.copy(tiposEquipo = it.tiposEquipo.filterNot { t -> t.nombre == tipo }) else it
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

    // Usa el contexto de la aplicación para no retener la pantalla en memoria
    fun capturarUbicacionActualInstalacion(instalacionNombre: String) {
        UbicacionUtils.obtenerUbicacionActual(getApplication<Application>()) { ubicacion, error ->
            if (ubicacion != null) {
                actualizarCoordenadasInstalacion(instalacionNombre, ubicacion.latitud, ubicacion.longitud)
            } else if (error != null) {
                _form.update { it.copy(error = error) }
            }
        }
    }

    // Devuelve true si el formulario pasó las validaciones locales
    fun guardar(onGuardadoExitoso: () -> Unit = {}) {
        val form = _form.value
        val nombre = form.nombre.trim()
        val instalacionSinEquipos = form.instalaciones.firstOrNull { it.tiposEquipo.isEmpty() }
        val instalacionCoordenadaInvalida = form.instalaciones.firstOrNull { !coordenadasValidas(it) }
        val error = when {
            nombre.isEmpty() -> "Ingrese el nombre del cliente"
            clientes.value.any { it.id != form.id && it.nombre.equals(nombre, ignoreCase = true) } ->
                "Ya existe un cliente con ese nombre"
            form.instalaciones.isEmpty() -> "Agregue al menos una instalación"
            instalacionSinEquipos != null ->
                "Agregue al menos un tipo de equipo a ${instalacionSinEquipos.nombre}"
            instalacionCoordenadaInvalida != null ->
                "Revise la latitud y longitud de ${instalacionCoordenadaInvalida.nombre}"
            else -> null
        }
        if (error != null) {
            _form.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            try {
                repository.guardar(
                    Cliente(
                        id = form.id.orEmpty(),
                        nombre = nombre,
                        instalaciones = form.instalaciones,
                    ),
                )
                repository.refrescarClientes()
                onGuardadoExitoso()
            } catch (e: Exception) {
                _form.update {
                    it.copy(error = e.message ?: "Error al guardar el cliente")
                }
            }
        }
    }


    fun eliminar() {
        val id = _form.value.id ?: return
        viewModelScope.launch {
            try {
                repository.eliminar(id)
            } catch (e: Exception) {
                _form.update {
                    it.copy(error = e.message ?: "Error al eliminar el cliente")
                }
            }
        }
    }

    private fun coordenadasValidas(instalacion: Instalacion): Boolean {
        val lat = instalacion.latitud
        val lon = instalacion.longitud
        if (lat == null && lon == null) return true
        return lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0
    }
}

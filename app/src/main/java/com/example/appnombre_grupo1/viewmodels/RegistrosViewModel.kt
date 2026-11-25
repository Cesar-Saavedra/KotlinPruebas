package com.example.appnombre_grupo1.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appnombre_grupo1.di.container
import com.example.appnombre_grupo1.model.RegistroGuardado
import com.example.appnombre_grupo1.model.Usuario
import com.example.appnombre_grupo1.model.remote.MENSAJE_SIN_CONEXION
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RegistrosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = container.registroRepository
    private val sincronizacion = container.sincronizacionRepository

    // Mensajes para la vista: "Registro archivado", errores de conexión, etc.
    private val _mensajes = MutableSharedFlow<String>()
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    // Se emite cuando el servidor confirmó archivar o restaurar (para volver a la lista)
    private val _cambioConfirmado = MutableSharedFlow<Unit>()
    val cambioConfirmado: SharedFlow<Unit> = _cambioConfirmado.asSharedFlow()

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    // Registros activos (técnicos y administradores)
    val registros: StateFlow<List<RegistroGuardado>> =
        repository.registros.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Registros archivados (solo administradores)
    val archivados: StateFlow<List<RegistroGuardado>> =
        repository.archivados.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Se guarda solo el id del registro abierto; el detalle se busca en las listas, así
    // se actualiza solo cuando el registro se edita, se archiva o se restaura
    private val idSeleccionado = MutableStateFlow<String?>(null)

    val registroSeleccionado: StateFlow<RegistroGuardado?> =
        combine(registros, archivados, idSeleccionado) { activos, archivados, id ->
            activos.firstOrNull { it.id == id } ?: archivados.firstOrNull { it.id == id }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun seleccionar(registro: RegistroGuardado) {
        idSeleccionado.value = registro.id
    }

    // "Eliminar": el registro pasa a Archivados
    fun archivar(administrador: Usuario) {
        val id = idSeleccionado.value ?: return
        confirmar("Registro archivado") { repository.archivar(id, administrador) }
    }

    fun restaurar(administrador: Usuario) {
        val id = idSeleccionado.value ?: return
        confirmar("Registro restaurado") { repository.restaurar(id, administrador) }
    }

    // Envía los pendientes y trae del servidor los registros de los demás técnicos
    fun sincronizar() {
        if (_sincronizando.value) return
        viewModelScope.launch {
            _sincronizando.value = true
            val ok = sincronizacion.sincronizar()
            _sincronizando.value = false
            _mensajes.emit(if (ok) "Datos actualizados" else MENSAJE_SIN_CONEXION)
        }
    }

    private fun confirmar(mensajeExito: String, operacion: suspend () -> String?) {
        viewModelScope.launch {
            val error = operacion()
            if (error == null) _cambioConfirmado.emit(Unit)
            _mensajes.emit(error ?: mensajeExito)
        }
    }
}

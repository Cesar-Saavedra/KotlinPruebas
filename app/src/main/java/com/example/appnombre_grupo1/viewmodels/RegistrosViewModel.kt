package com.example.appnombre_grupo1.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appnombre_grupo1.di.container
import com.example.appnombre_grupo1.model.RegistroGuardado
import com.example.appnombre_grupo1.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RegistrosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = container.registroRepository

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
        viewModelScope.launch { repository.archivar(id, administrador) }
    }

    fun restaurar() {
        val id = idSeleccionado.value ?: return
        viewModelScope.launch { repository.restaurar(id) }
    }
}

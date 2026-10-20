package com.example.appnombre_grupo1.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appnombre_grupo1.di.container
import com.example.appnombre_grupo1.model.RegistroGuardado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class RegistrosViewModel(application: Application) : AndroidViewModel(application) {

    val registros: StateFlow<List<RegistroGuardado>> =
        container.registroRepository.registros.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _registroSeleccionado = MutableStateFlow<RegistroGuardado?>(null)
    val registroSeleccionado: StateFlow<RegistroGuardado?> = _registroSeleccionado.asStateFlow()

    fun seleccionar(registro: RegistroGuardado) {
        _registroSeleccionado.value = registro
    }
}

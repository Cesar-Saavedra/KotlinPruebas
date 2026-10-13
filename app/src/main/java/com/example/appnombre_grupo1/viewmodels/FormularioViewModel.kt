package com.example.appnombre_grupo1.viewmodels

import androidx.lifecycle.ViewModel
import com.example.appnombre_grupo1.model.DatosGeneralesForm
import com.example.appnombre_grupo1.model.MantenimientoState
import com.example.appnombre_grupo1.model.PruebasElectricasState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FormularioViewModel : ViewModel() {

    private val _datosGenerales = MutableStateFlow(DatosGeneralesForm())
    val datosGenerales: StateFlow<DatosGeneralesForm> = _datosGenerales.asStateFlow()

    private val _mantenimientoState = MutableStateFlow(MantenimientoState())
    val mantenimientoState: StateFlow<MantenimientoState> = _mantenimientoState.asStateFlow()

    private val _pruebasElectricasState = MutableStateFlow(PruebasElectricasState())
    val pruebasElectricasState: StateFlow<PruebasElectricasState> = _pruebasElectricasState.asStateFlow()

    fun actualizarDatosGenerales(nuevosDatos: DatosGeneralesForm) {
        _datosGenerales.value = nuevosDatos
        // Sincronizar datos generales en los estados individuales
        _mantenimientoState.update { it.copy(datosGenerales = nuevosDatos) }
        _pruebasElectricasState.update { it.copy(datosGenerales = nuevosDatos) }
    }

    fun actualizarMantenimiento(nuevoEstado: MantenimientoState) {
        _mantenimientoState.value = nuevoEstado
    }

    fun actualizarPruebas(nuevoEstado: PruebasElectricasState) {
        _pruebasElectricasState.value = nuevoEstado
    }

    fun reiniciarRegistro() {
        _datosGenerales.value = DatosGeneralesForm()
        _mantenimientoState.value = MantenimientoState()
        _pruebasElectricasState.value = PruebasElectricasState()
    }
}
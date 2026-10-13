package com.example.appnombre_grupo1.viewmodels

import androidx.lifecycle.ViewModel
import com.example.appnombre_grupo1.model.DatosGeneralesForm
import com.example.appnombre_grupo1.model.MantenimientoState
import com.example.appnombre_grupo1.model.PruebasElectricasState
import com.example.appnombre_grupo1.ui.screens.PasoFormulario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FormularioViewModel : ViewModel() {

    // Control del paso actual de navegación (sobrevive a la rotación de pantalla)
    private val _pasoActual = MutableStateFlow(PasoFormulario.INICIO)
    val pasoActual: StateFlow<PasoFormulario> = _pasoActual.asStateFlow()

    private val _datosGenerales = MutableStateFlow(DatosGeneralesForm())
    val datosGenerales: StateFlow<DatosGeneralesForm> = _datosGenerales.asStateFlow()

    private val _mantenimientoState = MutableStateFlow(MantenimientoState())
    val mantenimientoState: StateFlow<MantenimientoState> = _mantenimientoState.asStateFlow()

    private val _pruebasElectricasState = MutableStateFlow(PruebasElectricasState())
    val pruebasElectricasState: StateFlow<PruebasElectricasState> = _pruebasElectricasState.asStateFlow()

    fun navegarA(paso: PasoFormulario) {
        _pasoActual.value = paso
    }

    fun actualizarDatosGenerales(nuevosDatos: DatosGeneralesForm) {
        _datosGenerales.value = nuevosDatos
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
        _pasoActual.value = PasoFormulario.DATOS_GENERALES
    }
}
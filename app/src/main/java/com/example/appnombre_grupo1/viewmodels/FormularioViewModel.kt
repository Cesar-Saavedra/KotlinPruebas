package com.example.appnombre_grupo1.viewmodels

import androidx.lifecycle.ViewModel
import com.example.appnombre_grupo1.model.DatosGeneralesForm
import com.example.appnombre_grupo1.model.MantenimientoState
import com.example.appnombre_grupo1.model.PruebasElectricasState
import com.example.appnombre_grupo1.model.repository.RegistroRepository
import com.example.appnombre_grupo1.model.validation.DatosGeneralesValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FormularioViewModel(
    private val repository: RegistroRepository = RegistroRepository(),
    private val validator: DatosGeneralesValidator = DatosGeneralesValidator(),
) : ViewModel() {

    // Estado de cada formulario (sobrevive a la rotación de pantalla)
    private val _datosGenerales = MutableStateFlow(DatosGeneralesForm())
    val datosGenerales: StateFlow<DatosGeneralesForm> = _datosGenerales.asStateFlow()

    // Los errores solo se muestran después del primer intento de continuar
    private val _mostrarErrores = MutableStateFlow(false)
    val mostrarErrores: StateFlow<Boolean> = _mostrarErrores.asStateFlow()

    private val _mantenimientoState = MutableStateFlow(MantenimientoState())
    val mantenimientoState: StateFlow<MantenimientoState> = _mantenimientoState.asStateFlow()

    private val _pruebasElectricasState = MutableStateFlow(PruebasElectricasState())
    val pruebasElectricasState: StateFlow<PruebasElectricasState> = _pruebasElectricasState.asStateFlow()

    fun actualizarDatosGenerales(nuevosDatos: DatosGeneralesForm) {
        val validados = validator.validate(nuevosDatos)
        _datosGenerales.value = validados
        _mantenimientoState.update { it.copy(datosGenerales = validados) }
        _pruebasElectricasState.update { it.copy(datosGenerales = validados) }
    }

    // Activa la visualización de errores y devuelve true si se puede continuar
    fun validarDatosGenerales(): Boolean {
        _mostrarErrores.value = true
        actualizarDatosGenerales(_datosGenerales.value)
        return _datosGenerales.value.errores.isValid
    }

    fun actualizarMantenimiento(nuevoEstado: MantenimientoState) {
        _mantenimientoState.value = nuevoEstado
    }

    fun actualizarPruebas(nuevoEstado: PruebasElectricasState) {
        _pruebasElectricasState.value = nuevoEstado
    }

    fun guardarMantenimiento() {
        repository.guardarMantenimiento(_mantenimientoState.value)
    }

    fun guardarPruebas() {
        repository.guardarPruebas(_pruebasElectricasState.value)
    }

    // Comienza un registro nuevo con el técnico que tiene la sesión iniciada
    fun reiniciarRegistro(nombreTecnico: String) {
        _datosGenerales.value = DatosGeneralesForm(tecnico = nombreTecnico)
        _mostrarErrores.value = false
        _mantenimientoState.value = MantenimientoState(datosGenerales = _datosGenerales.value)
        _pruebasElectricasState.value = PruebasElectricasState(datosGenerales = _datosGenerales.value)
    }
}

package com.example.appnombre_grupo1.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.appnombre_grupo1.di.container
import com.example.appnombre_grupo1.model.Cliente
import com.example.appnombre_grupo1.model.DatosGeneralesForm
import com.example.appnombre_grupo1.model.MantenimientoState
import com.example.appnombre_grupo1.model.PruebasElectricasState
import com.example.appnombre_grupo1.model.Usuario
import com.example.appnombre_grupo1.model.validation.DatosGeneralesValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FormularioViewModel(application: Application) : AndroidViewModel(application) {

    private val registroRepository = container.registroRepository
    private val validator = DatosGeneralesValidator()

    // Opciones definidas por el administrador
    val clientes: StateFlow<List<Cliente>> = container.clienteRepository.clientes

    // Técnico que está llenando el registro actual
    private var tecnico: Usuario? = null

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

    // Al cambiar de cliente se limpian la instalación y el equipo, que dependen de él
    fun seleccionarCliente(nombreCliente: String) {
        val actual = _datosGenerales.value
        if (actual.cliente == nombreCliente) return
        actualizarDatosGenerales(actual.copy(cliente = nombreCliente, instalacion = "", tipoEquipo = ""))
    }

    // Al cambiar de instalación se limpia el equipo, que depende de ella
    fun seleccionarInstalacion(nombreInstalacion: String) {
        val actual = _datosGenerales.value
        if (actual.instalacion == nombreInstalacion) return
        actualizarDatosGenerales(actual.copy(instalacion = nombreInstalacion, tipoEquipo = ""))
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
        tecnico?.let { registroRepository.guardarMantenimiento(_mantenimientoState.value, it) }
    }

    fun guardarPruebas() {
        tecnico?.let { registroRepository.guardarPruebas(_pruebasElectricasState.value, it) }
    }

    // Comienza un registro nuevo con el técnico que tiene la sesión iniciada
    fun reiniciarRegistro(usuario: Usuario) {
        tecnico = usuario
        _datosGenerales.value = DatosGeneralesForm(tecnico = usuario.nombre)
        _mostrarErrores.value = false
        _mantenimientoState.value = MantenimientoState(datosGenerales = _datosGenerales.value)
        _pruebasElectricasState.value = PruebasElectricasState(datosGenerales = _datosGenerales.value)
    }
}

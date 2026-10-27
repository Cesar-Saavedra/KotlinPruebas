package com.example.appnombre_grupo1.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appnombre_grupo1.di.container
import com.example.appnombre_grupo1.model.Cliente
import com.example.appnombre_grupo1.model.DatosGeneralesForm
import com.example.appnombre_grupo1.model.MantenimientoState
import com.example.appnombre_grupo1.model.PruebasElectricasState
import com.example.appnombre_grupo1.model.RegistroGuardado
import com.example.appnombre_grupo1.model.TipoRegistro
import com.example.appnombre_grupo1.model.Usuario
import com.example.appnombre_grupo1.model.validation.DatosGeneralesValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class FormularioViewModel(application: Application) : AndroidViewModel(application) {

    private val registroRepository = container.registroRepository
    private val fotoRepository = container.fotoRepository
    private val validator = DatosGeneralesValidator()

    // Opciones definidas por el administrador
    val clientes: StateFlow<List<Cliente>> =
        container.clienteRepository.clientes.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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

    // true mientras se está guardando un registro (muestra el círculo de carga)
    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    // Evento que se emite una vez cuando el registro terminó de guardarse
    private val _registroGuardado = MutableSharedFlow<Unit>()
    val registroGuardado: SharedFlow<Unit> = _registroGuardado.asSharedFlow()

    // Último registro de cada protocolo hecho al mismo equipo, para compararlo con el actual
    val mantenimientoAnterior: StateFlow<RegistroGuardado?> = registroAnterior(TipoRegistro.MANTENIMIENTO)
    val pruebaAnterior: StateFlow<RegistroGuardado?> = registroAnterior(TipoRegistro.PRUEBAS_ELECTRICAS)

    // true mientras se copia una foto elegida de la galería
    private val _procesandoFoto = MutableStateFlow(false)
    val procesandoFoto: StateFlow<Boolean> = _procesandoFoto.asStateFlow()

    // Archivo que se entregó a la cámara y que aún no tiene foto confirmada
    private var fotoPendiente: File? = null

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
        calcularDistanciaAInstalacion()
    }

    // Al cambiar de instalación se limpia el equipo, que depende de ella
    fun seleccionarInstalacion(nombreInstalacion: String) {
        val actual = _datosGenerales.value
        if (actual.instalacion == nombreInstalacion) return
        actualizarDatosGenerales(actual.copy(instalacion = nombreInstalacion, tipoEquipo = ""))
        calcularDistanciaAInstalacion()
    }

    // --- Verificación GPS y Selección Automática ---
    private val _ubicacionTecnico = MutableStateFlow<com.example.appnombre_grupo1.ui.utils.Ubicacion?>(null)
    val ubicacionTecnico: StateFlow<com.example.appnombre_grupo1.ui.utils.Ubicacion?> = _ubicacionTecnico.asStateFlow()

    private val _distanciaMetros = MutableStateFlow<Float?>(null)
    val distanciaMetros: StateFlow<Float?> = _distanciaMetros.asStateFlow()

    private val _buscandoUbicacion = MutableStateFlow(false)
    val buscandoUbicacion: StateFlow<Boolean> = _buscandoUbicacion.asStateFlow()

    private val _errorGps = MutableStateFlow<String?>(null)
    val errorGps: StateFlow<String?> = _errorGps.asStateFlow()

    fun actualizarUbicacionGps(context: android.content.Context) {
        _buscandoUbicacion.value = true
        _errorGps.value = null
        com.example.appnombre_grupo1.ui.utils.UbicacionUtils.obtenerUbicacionActual(context) { ubicacion, error ->
            _buscandoUbicacion.value = false
            if (ubicacion != null) {
                _ubicacionTecnico.value = ubicacion
                calcularDistanciaAInstalacion()
            } else {
                _errorGps.value = error
            }
        }
    }

    fun detectarInstalacionAutomatica(context: android.content.Context) {
        _buscandoUbicacion.value = true
        _errorGps.value = null
        com.example.appnombre_grupo1.ui.utils.UbicacionUtils.obtenerUbicacionActual(context) { ubicacion, error ->
            _buscandoUbicacion.value = false
            if (ubicacion != null) {
                _ubicacionTecnico.value = ubicacion
                var menorDistancia = Float.MAX_VALUE
                var clienteElegido: Cliente? = null
                var instalacionElegida: String? = null

                clientes.value.forEach { cliente ->
                    cliente.instalaciones.forEach { inst ->
                        if (inst.latitud != null && inst.longitud != null) {
                            val dist = com.example.appnombre_grupo1.ui.utils.UbicacionUtils.calcularDistanciaMetros(
                                ubicacion.latitud, ubicacion.longitud,
                                inst.latitud, inst.longitud
                            )
                            if (dist < menorDistancia) {
                                menorDistancia = dist
                                clienteElegido = cliente
                                instalacionElegida = inst.nombre
                            }
                        }
                    }
                }

                if (clienteElegido != null && instalacionElegida != null && menorDistancia <= com.example.appnombre_grupo1.ui.utils.UbicacionUtils.RANGO_MAXIMO_METROS) {
                    val actual = _datosGenerales.value
                    actualizarDatosGenerales(
                        actual.copy(
                            cliente = clienteElegido.nombre,
                            instalacion = instalacionElegida,
                            tipoEquipo = ""
                        )
                    )
                    _distanciaMetros.value = menorDistancia
                } else if (clienteElegido != null) {
                    _errorGps.value = "La instalación más cercana es '$instalacionElegida' a %.0f metros (máx 100m).".format(menorDistancia)
                } else {
                    _errorGps.value = "No hay instalaciones georreferenciadas registradas."
                }
            } else {
                _errorGps.value = error
            }
        }
    }

    fun calcularDistanciaAInstalacion() {
        val ubicacion = _ubicacionTecnico.value ?: run {
            _distanciaMetros.value = null
            return
        }
        val datos = _datosGenerales.value
        val clienteObj = clientes.value.firstOrNull { it.nombre == datos.cliente }
        val instObj = clienteObj?.instalaciones?.firstOrNull { it.nombre == datos.instalacion }

        if (instObj?.latitud != null && instObj.longitud != null) {
            _distanciaMetros.value = com.example.appnombre_grupo1.ui.utils.UbicacionUtils.calcularDistanciaMetros(
                ubicacion.latitud, ubicacion.longitud,
                instObj.latitud, instObj.longitud
            )
        } else {
            _distanciaMetros.value = null
        }
    }

    fun validarRangoGps(): Boolean {
        val datos = _datosGenerales.value
        val clienteObj = clientes.value.firstOrNull { it.nombre == datos.cliente }
        val instObj = clienteObj?.instalaciones?.firstOrNull { it.nombre == datos.instalacion }

        if (instObj?.latitud != null && instObj.longitud != null) {
            val dist = _distanciaMetros.value
            if (dist == null || dist > com.example.appnombre_grupo1.ui.utils.UbicacionUtils.RANGO_MAXIMO_METROS) {
                _errorGps.value = if (dist != null) {
                    "Está a %.0f metros de la instalación. Debe estar a menos de 100m para realizar pruebas.".format(dist)
                } else {
                    "Obtenga su ubicación GPS para verificar la cercanía a la instalación."
                }
                return false
            }
        }
        return true
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

    fun guardarMantenimiento() = guardar { tecnico ->
        registroRepository.guardarMantenimiento(_mantenimientoState.value, tecnico)
    }

    fun guardarPruebas() = guardar { tecnico ->
        registroRepository.guardarPruebas(_pruebasElectricasState.value, tecnico)
    }

    // Guarda en una corrutina mostrando la carga y avisa a la vista cuando termina
    private fun guardar(accion: suspend (Usuario) -> Unit) {
        val tecnicoActual = tecnico ?: return
        if (_guardando.value) return

        viewModelScope.launch {
            _guardando.value = true
            // Simula el tiempo que tomaría enviar el registro a un servidor
            delay(TIEMPO_GUARDADO_MS)
            accion(tecnicoActual)
            _guardando.value = false
            _registroGuardado.emit(Unit)
        }
    }

    // Crea el archivo donde la cámara guardará la foto y devuelve su Uri
    fun prepararFotoCamara(): Uri {
        val archivo = fotoRepository.nuevoArchivo()
        fotoPendiente = archivo
        return fotoRepository.uriParaCamara(archivo)
    }

    fun resultadoCamara(fotoTomada: Boolean) {
        val archivo = fotoPendiente ?: return
        fotoPendiente = null
        if (fotoTomada && archivo.length() > 0) {
            asignarFoto(archivo.absolutePath)
        } else {
            archivo.delete()
        }
    }

    fun fotoDeGaleria(uri: Uri) {
        viewModelScope.launch {
            _procesandoFoto.value = true
            fotoRepository.copiarDesdeGaleria(uri)?.let { asignarFoto(it) }
            _procesandoFoto.value = false
        }
    }

    fun quitarFoto() {
        asignarFoto(null)
    }

    // Reemplaza la foto del mantenimiento y borra el archivo de la anterior
    private fun asignarFoto(path: String?) {
        val anterior = _mantenimientoState.value.fotoPath
        if (anterior != null && anterior != path) fotoRepository.eliminar(anterior)
        _mantenimientoState.update { it.copy(fotoPath = path) }
    }

    private fun registroAnterior(tipo: TipoRegistro): StateFlow<RegistroGuardado?> =
        combine(registroRepository.registros, _datosGenerales) { registros, datos ->
            // Los registros vienen del más reciente al más antiguo
            registros.firstOrNull { it.tipo == tipo && esMismoEquipo(it.datosGenerales, datos) }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private fun esMismoEquipo(a: DatosGeneralesForm, b: DatosGeneralesForm): Boolean =
        b.codigoEquipo.isNotBlank() &&
            a.cliente == b.cliente &&
            a.instalacion == b.instalacion &&
            a.codigoEquipo.trim().equals(b.codigoEquipo.trim(), ignoreCase = true)

    // Comienza un registro nuevo con el técnico que tiene la sesión iniciada
    fun reiniciarRegistro(usuario: Usuario) {
        // Borra la foto del registro anterior si nunca llegó a guardarse
        _mantenimientoState.value.fotoPath?.let { foto ->
            viewModelScope.launch {
                if (!registroRepository.fotoEnUso(foto)) fotoRepository.eliminar(foto)
            }
        }
        tecnico = usuario
        _datosGenerales.value = DatosGeneralesForm(tecnico = usuario.nombre)
        _mostrarErrores.value = false
        _mantenimientoState.value = MantenimientoState(datosGenerales = _datosGenerales.value)
        _pruebasElectricasState.value = PruebasElectricasState(datosGenerales = _datosGenerales.value)
    }

    private companion object {
        const val TIEMPO_GUARDADO_MS = 3_000L
    }
}

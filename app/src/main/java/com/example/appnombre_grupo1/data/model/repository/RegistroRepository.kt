package com.example.appnombre_grupo1.data.model.repository

import com.example.appnombre_grupo1.data.model.DatosGeneralesErrores
import com.example.appnombre_grupo1.data.model.DetectorCambios
import com.example.appnombre_grupo1.data.model.EdicionRegistro
import com.example.appnombre_grupo1.data.model.MantenimientoState
import com.example.appnombre_grupo1.data.model.PruebasElectricasState
import com.example.appnombre_grupo1.data.model.RegistroGuardado
import com.example.appnombre_grupo1.data.model.TipoRegistro
import com.example.appnombre_grupo1.data.model.Usuario
import com.example.appnombre_grupo1.data.model.database.dao.RegistroDao
import com.example.appnombre_grupo1.data.model.database.toEntity
import com.example.appnombre_grupo1.data.model.database.toModel
import com.example.appnombre_grupo1.data.remote.ApiService
import com.example.appnombre_grupo1.data.remote.aRegistroGuardado
import com.example.appnombre_grupo1.data.remote.dto.CrearRegistroTerrenoRequestDto
import com.example.appnombre_grupo1.data.remote.dto.PruebaElectricaDto
import com.example.appnombre_grupo1.data.remote.dto.EdicionRegistroDto
import com.example.appnombre_grupo1.data.model.EstadoComponente
import com.example.appnombre_grupo1.data.remote.dto.DetalleMantenimientoDto
import com.example.appnombre_grupo1.ui.utils.Ubicacion
import com.example.appnombre_grupo1.data.model.DatosGeneralesForm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Response
import java.util.UUID
import android.util.Log

// Registros de terreno, guardados en las tablas "registros", "mantenimientos",
// "pruebas_electricas" y "ediciones_registro" (Room). El más reciente primero.
class RegistroRepository(
    private val dao: RegistroDao,
    private val apiService: ApiService,
) {

    // Registros activos: los que ven técnicos y administradores
    val registros: Flow<List<RegistroGuardado>> = dao.observarActivos().map { lista -> lista.map { it.toModel() } }

    // Registros archivados: solo los ve el administrador
    val archivados: Flow<List<RegistroGuardado>> = dao.observarArchivados().map { lista -> lista.map { it.toModel() } }

    suspend fun refrescarRegistros(): Result<Unit> {
        return try {
            val activosResp = apiService.getRegistros(archived = false)
            val archivadosResp = apiService.getRegistros(archived = true)

            Log.d(
                "SYNC_REGISTROS",
                "activosCode=${activosResp.code()} (${activosResp.body()?.size ?: 0}), archivadosCode=${archivadosResp.code()} (${archivadosResp.body()?.size ?: 0})"
            )

            if (!activosResp.isSuccessful || !archivadosResp.isSuccessful) {
                val codigo = if (!activosResp.isSuccessful) activosResp.code() else archivadosResp.code()
                val errorBody = if (!activosResp.isSuccessful) activosResp.errorBody()?.string() else archivadosResp.errorBody()?.string()
                Log.e("SYNC_REGISTROS", "Error HTTP $codigo: $errorBody")
                return Result.failure(Exception("Error al sincronizar registros (HTTP $codigo)"))
            }

            val activosRemotos = activosResp.body().orEmpty()
            val archivadosRemotos = archivadosResp.body().orEmpty()

            (activosRemotos + archivadosRemotos).forEach { dto ->
                val remoto = dto.aRegistroGuardado()
                val existente = dao.obtenerPorId(remoto.id)?.toModel()

                val obsLocal = existente?.mantenimiento?.observaciones?.trim()?.takeIf { it.isNotBlank() }
                val obsRemoto = remoto.mantenimiento?.observaciones?.trim()?.takeIf { it.isNotBlank() }

                val tecnicoPrevio = existente?.datosGenerales?.tecnico?.takeIf {
                    it.isNotBlank() && !it.equals(remoto.tecnicoUsername, ignoreCase = true)
                }

                val baseConTecnico = if (tecnicoPrevio != null) {
                    remoto.copy(
                        mantenimiento = remoto.mantenimiento?.copy(
                            datosGenerales = remoto.mantenimiento.datosGenerales.copy(tecnico = tecnicoPrevio),
                        ),
                        pruebas = remoto.pruebas?.copy(
                            datosGenerales = remoto.pruebas.datosGenerales.copy(tecnico = tecnicoPrevio),
                        ),
                    )
                } else {
                    remoto
                }

                val finalRegistro = if (baseConTecnico.mantenimiento != null && obsRemoto == null && obsLocal != null) {
                    baseConTecnico.copy(
                        mantenimiento = baseConTecnico.mantenimiento.copy(observaciones = obsLocal),
                    )
                } else {
                    baseConTecnico
                }

                dao.guardar(finalRegistro.toEntity())
            }



            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SYNC_REGISTROS", "Excepción en refrescarRegistros()", e)
            Result.failure(e)
        }
    }


    suspend fun guardarMantenimiento(registro: MantenimientoState, tecnico: Usuario, ubicacion: Ubicacion? = null) {
        val limpio = sinErrores(registro)
        val nuevo = nuevoRegistro(TipoRegistro.MANTENIMIENTO, tecnico).copy(mantenimiento = limpio)
        enviarCreacionYGuardarLocal(nuevo, tecnico, ubicacion)
    }
    suspend fun guardarPruebas(registro: PruebasElectricasState, tecnico: Usuario, ubicacion: Ubicacion? = null) {
        val limpio = sinErrores(registro)
        val nuevo = nuevoRegistro(TipoRegistro.PRUEBAS_ELECTRICAS, tecnico).copy(pruebas = limpio)
        enviarCreacionYGuardarLocal(nuevo, tecnico, ubicacion)
    }

    // Guarda los cambios de un mantenimiento y anota quién lo editó y qué cambió.
    // Devuelve false si no había ningún cambio (en ese caso no se guarda nada).
    suspend fun actualizarMantenimiento(original: RegistroGuardado, nuevo: MantenimientoState, editor: Usuario): Boolean {
        val anterior = original.mantenimiento ?: return false
        val limpio = sinErrores(nuevo)
        val cambios = DetectorCambios.mantenimiento(anterior, limpio)
        if (cambios.isEmpty()) return false

        val registroActualizado = original.copy(mantenimiento = limpio)
        enviarEdicionYActualizarLocal(
            original = original,
            actualizado = registroActualizado,
            editor = editor,
            cambios = cambios
        )
        return true
    }

    suspend fun actualizarPruebas(original: RegistroGuardado, nuevo: PruebasElectricasState, editor: Usuario): Boolean {
        val anterior = original.pruebas ?: return false
        val limpio = sinErrores(nuevo)
        val cambios = DetectorCambios.pruebas(anterior, limpio)
        if (cambios.isEmpty()) return false

        val registroActualizado = original.copy(pruebas = limpio)
        enviarEdicionYActualizarLocal(
            original = original,
            actualizado = registroActualizado,
            editor = editor,
            cambios = cambios
        )
        return true
    }

    // "Eliminar" un registro: queda archivado y solo los administradores lo pueden ver
    suspend fun archivar(registroId: String, administrador: Usuario) {
        dao.archivar(registroId, administrador.username, administrador.nombre, System.currentTimeMillis())
        try {
            apiService.archivarRegistro(registroId)
        } catch (_: Exception) {
            // Queda archivado en Room; se podrá sincronizar después
        }
    }

    suspend fun restaurar(registroId: String) {
        dao.restaurar(registroId)
        try {
            apiService.restaurarRegistro(registroId)
        } catch (_: Exception) {
            // Queda restaurado en Room; se podrá sincronizar después
        }
    }


    // true si algún registro guardado (activo o archivado) usa esa foto
    suspend fun fotoEnUso(fotoPath: String): Boolean = dao.contarUsosDeFoto(fotoPath) > 0

    private suspend fun enviarCreacionYGuardarLocal(nuevo: RegistroGuardado, tecnico: Usuario, ubicacion: Ubicacion? = null) {
        // 1. Guardar siempre primero en Room
        dao.guardar(nuevo.toEntity())

        // 2. Intentar sincronizar con el backend vía Retrofit
        try {
            val requestDto = nuevo.aCrearRegistroRequestDto(tecnico, ubicacion)
            Log.d(
                "SYNC_REGISTRO",
                "Payload ubicacion -> facilityId=${requestDto.facilityId}, lat=${requestDto.latitudUsuario}, lon=${requestDto.longitudUsuario}"
            )
            val response = apiService.guardarRegistro(requestDto)
            if (response.isSuccessful) {
                val registroServidor = response.body()?.aRegistroGuardado()
                if (registroServidor != null) {
                    dao.guardar(registroServidor.toEntity())
                }
            } else {
                Log.e("SYNC_REGISTRO", "HTTP ${response.code()}: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("SYNC_REGISTRO", "Error enviando registro por Retrofit", e)
        }
    }

    private fun pruebaElectricaPorDefectoDto(): PruebaElectricaDto =
        PruebasElectricasState(
            tensionPruebaAislamiento = "0.0",
            resistenciaAislamientoMOhm = "0.0",
            resistenciaContactoMicroOhm = "0.0",
            voltajeLineaV = "0.0",
            corrienteCargaA = "0.0",
        ).aPruebaElectricaDto().copy()



    private suspend fun enviarEdicionYActualizarLocal(
        original: RegistroGuardado,
        actualizado: RegistroGuardado,
        editor: Usuario,
        cambios: List<String>,
    ) {
        val itemsDto = if (actualizado.tipo == TipoRegistro.PRUEBAS_ELECTRICAS) {
            emptyList()
        } else {
            actualizado.mantenimiento?.aItemsMantenimientoDto() ?: emptyList()
        }

        val pruebaDto = if (actualizado.pruebas != null) {
            actualizado.pruebas.aPruebaElectricaDto()
        } else {
            pruebaElectricaPorDefectoDto()
        }

        val dtoEdicion = EdicionRegistroDto(
            editorUsername = editor.username,
            fotoUrlOrBase64 = actualizado.mantenimiento?.fotoPath,
            itemsMantenimiento = itemsDto,
            pruebaElectrica = pruebaDto,
        )

        var registroParaGuardar = actualizado

        try {
            val response = apiService.actualizarRegistro(original.id, dtoEdicion)
            if (response.isSuccessful) {
                val remoto = response.body()?.aRegistroGuardado()
                if (remoto != null) {
                    val obsLocal = actualizado.mantenimiento?.observaciones?.trim()?.takeIf { it.isNotBlank() }
                    val obsRemoto = remoto.mantenimiento?.observaciones?.trim()?.takeIf { it.isNotBlank() }

                    registroParaGuardar = if (remoto.mantenimiento != null && obsRemoto == null && obsLocal != null) {
                        remoto.copy(
                            id = original.id,
                            mantenimiento = remoto.mantenimiento.copy(observaciones = obsLocal),
                        )
                    } else {
                        remoto.copy(id = original.id)
                    }
                }
            } else {
                Log.e(
                    "EDIT_REGISTRO",
                    "HTTP ${response.code()} al editar ${original.id}: ${response.errorBody()?.string()}",
                )
            }
        } catch (e: Exception) {
            Log.e("EDIT_REGISTRO", "Error de red/serialización al editar ${original.id}", e)
        }

        val nuevaEdicion = edicion(original, editor, cambios)
        dao.actualizar(registroParaGuardar.toEntity(), nuevaEdicion)
    }





    private fun <T> verificarRespuestaExitosa(response: Response<T>, mensajePorDefecto: String) {
        if (!response.isSuccessful) {
            val detalle = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
            throw Exception(detalle ?: "$mensajePorDefecto (HTTP ${response.code()})")
        }
    }

    private fun edicion(original: RegistroGuardado, editor: Usuario, cambios: List<String>) =
        EdicionRegistro(
            fechaMillis = System.currentTimeMillis(),
            editorUsername = editor.username,
            editorNombre = editor.nombre.ifBlank { editor.username },
            cambios = cambios,
        ).toEntity(original.id)


    private fun sinErrores(estado: MantenimientoState) =
        estado.copy(datosGenerales = estado.datosGenerales.copy(errores = DatosGeneralesErrores()))

    private fun sinErrores(estado: PruebasElectricasState) =
        estado.copy(datosGenerales = estado.datosGenerales.copy(errores = DatosGeneralesErrores()))

    private fun nuevoRegistro(tipo: TipoRegistro, tecnico: Usuario): RegistroGuardado {
        val nombreTecnico = tecnico.nombre.ifBlank { tecnico.username }
        return RegistroGuardado(
            id = UUID.randomUUID().toString(),
            tipo = tipo,
            fechaMillis = System.currentTimeMillis(),
            tecnicoUsername = tecnico.username,
            mantenimiento = if (tipo == TipoRegistro.MANTENIMIENTO) {
                MantenimientoState(datosGenerales = DatosGeneralesForm(tecnico = nombreTecnico))
            } else {
                null
            },
            pruebas = if (tipo == TipoRegistro.PRUEBAS_ELECTRICAS) {
                PruebasElectricasState(datosGenerales = DatosGeneralesForm(tecnico = nombreTecnico))
            } else {
                null
            },
        )
    }

    private fun RegistroGuardado.fotoPath(): String? =
    mantenimiento?.fotoPath

private fun RegistroGuardado.aCrearRegistroRequestDto(
    tecnico: Usuario,
    ubicacion: Ubicacion? = null,
): CrearRegistroTerrenoRequestDto {
    val datos = datosGenerales
    val items = if (tipo == TipoRegistro.PRUEBAS_ELECTRICAS) {
        emptyList()
    } else {
        mantenimiento?.aItemsMantenimientoDto() ?: emptyList()
    }

    val pruebaDto = if (tipo == TipoRegistro.PRUEBAS_ELECTRICAS) {
        pruebas?.aPruebaElectricaDto()
    } else {
        null
    }

    return CrearRegistroTerrenoRequestDto(
        id = id,
        clientId = datos.cliente,
        facilityId = datos.instalacion,
        equipmentTypeId = datos.tipoEquipo,
        codEquipo = datos.codigoEquipo,
        technicianId = tecnico.id.ifBlank { tecnico.username },
        technicianUsername = tecnico.username,
        latitudUsuario = ubicacion?.latitud ?: 0.0,
        longitudUsuario = ubicacion?.longitud ?: 0.0,
        fotoUrlOrBase64 = fotoPath(),
        itemsMantenimiento = items,
        pruebaElectrica = pruebaDto,
    )
}


private fun PruebasElectricasState.aPruebaElectricaDto(): PruebaElectricaDto = PruebaElectricaDto(
    aislacion = resistenciaAislamientoMOhm.replace(",",".").toDoubleOrNull(),
    contacto = resistenciaContactoMicroOhm.replace(",",".").toDoubleOrNull(),
    voltaje = voltajeLineaV.replace(",", ".").toDoubleOrNull(),
    corriente = corrienteCargaA.replace(",", ".").toDoubleOrNull(),
    veredicto = veredicto.name,
    notasTecnicas = notasTecnicas.trim().takeIf { it.isNotBlank() }
)

companion object {
    private const val PREFIJO_OBS_GENERAL = "[OBS_GRAL]: "
}

private fun MantenimientoState.aItemsMantenimientoDto(): List<DetalleMantenimientoDto> {
    val obsGeneral = observaciones.trim().takeIf { it.isNotBlank() }

    fun item(
        nombreComponente: String,
        estado: EstadoComponente,
        descripcion: String,
        incluirObsGeneral: Boolean = false,
    ): DetalleMantenimientoDto {
        val obsComponente = descripcion.trim().takeIf { it.isNotBlank() }
        val obsCombinada = if (incluirObsGeneral && obsGeneral != null) {
            val bloqueGeneral = "$PREFIJO_OBS_GENERAL$obsGeneral"
            if (obsComponente != null) "$obsComponente\n$bloqueGeneral" else bloqueGeneral
        } else {
            obsComponente
        }

        return DetalleMantenimientoDto(
            nombreComponente = nombreComponente,
            estado = estado.name,
            observaciones = obsCombinada,
        )
    }

    return listOf(
        item("Físico exterior", estadoFisicoExterior, descripcionFisicoExterior, incluirObsGeneral = true),
        item("Aisladores", estadoAisladores, descripcionAisladores),
        item("Regletas", estadoRegletas, descripcionRegletas),
        item("Contactores", estadoContactores, descripcionContactores),
        item("Cables", estadoCables, descripcionCables),
        item("Luces piloto", estadoLucesPiloto, descripcionLucesPiloto),
        item("Canaletas", estadoCanaletas, descripcionCanaletas),
    )
}
}



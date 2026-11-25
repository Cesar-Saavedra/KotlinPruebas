package com.example.appnombre_grupo1.model.repository

import com.example.appnombre_grupo1.model.DatosGeneralesErrores
import com.example.appnombre_grupo1.model.DetectorCambios
import com.example.appnombre_grupo1.model.EdicionRegistro
import com.example.appnombre_grupo1.model.MantenimientoState
import com.example.appnombre_grupo1.model.PruebasElectricasState
import com.example.appnombre_grupo1.model.RegistroGuardado
import com.example.appnombre_grupo1.model.ResultadoEnvio
import com.example.appnombre_grupo1.model.TipoRegistro
import com.example.appnombre_grupo1.model.Usuario
import com.example.appnombre_grupo1.model.database.dao.RegistroDao
import com.example.appnombre_grupo1.model.database.toEntity
import com.example.appnombre_grupo1.model.database.toModel
import com.example.appnombre_grupo1.model.remote.ApiService
import com.example.appnombre_grupo1.model.remote.MENSAJE_SIN_CONEXION
import com.example.appnombre_grupo1.model.remote.RegistroDto
import com.example.appnombre_grupo1.model.remote.RespuestaApi
import com.example.appnombre_grupo1.model.remote.llamarApi
import com.example.appnombre_grupo1.model.remote.toDto
import com.example.appnombre_grupo1.model.remote.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Response
import java.util.UUID

// Registros: se guardan primero en Room (así se puede trabajar sin señal en terreno) y luego se
// envían al servidor. Si el envío falla quedan pendientes y se reintentan al sincronizar.
class RegistroRepository(private val dao: RegistroDao, private val api: ApiService) {

    val registros: Flow<List<RegistroGuardado>> = dao.observarActivos().map { lista -> lista.map { it.toModel() } }

    val archivados: Flow<List<RegistroGuardado>> = dao.observarArchivados().map { lista -> lista.map { it.toModel() } }

    suspend fun guardarMantenimiento(registro: MantenimientoState, tecnico: Usuario): ResultadoEnvio =
        guardarNuevo(nuevoRegistro(TipoRegistro.MANTENIMIENTO, tecnico).copy(mantenimiento = sinErrores(registro)))

    suspend fun guardarPruebas(registro: PruebasElectricasState, tecnico: Usuario): ResultadoEnvio =
        guardarNuevo(nuevoRegistro(TipoRegistro.PRUEBAS_ELECTRICAS, tecnico).copy(pruebas = sinErrores(registro)))

    suspend fun actualizarMantenimiento(original: RegistroGuardado, nuevo: MantenimientoState, editor: Usuario): ResultadoEnvio {
        val anterior = original.mantenimiento ?: return ResultadoEnvio.SIN_CAMBIOS
        val limpio = sinErrores(nuevo)
        val cambios = DetectorCambios.mantenimiento(anterior, limpio)
        if (cambios.isEmpty()) return ResultadoEnvio.SIN_CAMBIOS
        return guardarEdicion(original.copy(mantenimiento = limpio), editor, cambios)
    }

    suspend fun actualizarPruebas(original: RegistroGuardado, nuevo: PruebasElectricasState, editor: Usuario): ResultadoEnvio {
        val anterior = original.pruebas ?: return ResultadoEnvio.SIN_CAMBIOS
        val limpio = sinErrores(nuevo)
        val cambios = DetectorCambios.pruebas(anterior, limpio)
        if (cambios.isEmpty()) return ResultadoEnvio.SIN_CAMBIOS
        return guardarEdicion(original.copy(pruebas = limpio), editor, cambios)
    }

    // Archivar y restaurar son tareas de oficina: requieren conexión. Devuelven null o el mensaje de error
    suspend fun archivar(registroId: String, administrador: Usuario): String? =
        aplicarDelServidor { api.archivarRegistro(registroId, administrador.username) }

    suspend fun restaurar(registroId: String, administrador: Usuario): String? =
        aplicarDelServidor { api.restaurarRegistro(registroId, administrador.username) }

    suspend fun fotoEnUso(fotoPath: String): Boolean = dao.contarUsosDeFoto(fotoPath) > 0

    // Reintenta enviar los registros pendientes. false si no hubo conexión
    suspend fun subirPendientes(): Boolean = dao.idsPendientes().all { subir(it) != ResultadoEnvio.PENDIENTE }

    // Descarga los registros activos y archivados del servidor. false si no hubo conexión
    suspend fun descargar(): Boolean {
        val activos = llamarApi { api.obtenerRegistros(archivados = false) }
        val archivados = llamarApi { api.obtenerRegistros(archivados = true) }
        if (activos !is RespuestaApi.Exito || archivados !is RespuestaApi.Exito) return false
        dao.sincronizarCon((activos.datos + archivados.datos).map { it.toModel().toEntity() })
        return true
    }

    private suspend fun guardarNuevo(registro: RegistroGuardado): ResultadoEnvio {
        dao.guardar(registro.toEntity(pendiente = CREAR))
        return subir(registro.id)
    }

    // Guarda la edición en Room con su historial y la envía. Si el registro aún no estaba en el
    // servidor, sigue pendiente como CREAR y se envía completo
    private suspend fun guardarEdicion(registro: RegistroGuardado, editor: Usuario, cambios: List<String>): ResultadoEnvio {
        val pendienteActual = dao.obtener(registro.id)?.registro?.pendiente
        val pendiente = if (pendienteActual == CREAR) CREAR else EDITAR
        val edicion = EdicionRegistro(System.currentTimeMillis(), editor.username, editor.nombre, cambios).toEntity(registro.id)
        dao.actualizar(registro.toEntity(pendiente = pendiente, pendienteEditor = editor.username), edicion)
        return subir(registro.id)
    }

    private suspend fun subir(id: String): ResultadoEnvio {
        val local = dao.obtener(id) ?: return ResultadoEnvio.ENVIADO
        val dto = local.toModel().toDto()
        val respuesta = when (local.registro.pendiente) {
            CREAR -> llamarApi { api.crearRegistro(dto) }
            EDITAR -> llamarApi { api.editarRegistro(id, local.registro.pendienteEditor.orEmpty(), dto) }
            else -> return ResultadoEnvio.ENVIADO
        }
        return when (respuesta) {
            is RespuestaApi.Exito -> { guardarDelServidor(respuesta.datos); ResultadoEnvio.ENVIADO }
            // 409: el registro ya existe en el servidor (o allí está archivado): manda la versión del servidor
            is RespuestaApi.Error -> if (respuesta.codigo == 409 && traerDelServidor(id)) ResultadoEnvio.ENVIADO else ResultadoEnvio.PENDIENTE
            RespuestaApi.SinConexion -> ResultadoEnvio.PENDIENTE
        }
    }

    private suspend fun traerDelServidor(id: String): Boolean {
        val respuesta = llamarApi { api.obtenerRegistro(id) }
        if (respuesta !is RespuestaApi.Exito) return false
        guardarDelServidor(respuesta.datos)
        return true
    }

    private suspend fun aplicarDelServidor(llamada: suspend () -> Response<RegistroDto>): String? =
        when (val respuesta = llamarApi(llamada)) {
            is RespuestaApi.Exito -> { guardarDelServidor(respuesta.datos); null }
            is RespuestaApi.Error -> respuesta.mensaje
            RespuestaApi.SinConexion -> MENSAJE_SIN_CONEXION
        }

    private suspend fun guardarDelServidor(dto: RegistroDto) {
        dao.reemplazar(dto.toModel().toEntity())
    }

    private fun sinErrores(estado: MantenimientoState) =
        estado.copy(datosGenerales = estado.datosGenerales.copy(errores = DatosGeneralesErrores()))

    private fun sinErrores(estado: PruebasElectricasState) =
        estado.copy(datosGenerales = estado.datosGenerales.copy(errores = DatosGeneralesErrores()))

    private fun nuevoRegistro(tipo: TipoRegistro, tecnico: Usuario) = RegistroGuardado(
        id = UUID.randomUUID().toString(),
        tipo = tipo,
        fechaMillis = System.currentTimeMillis(),
        tecnicoUsername = tecnico.username,
    )

    private companion object {
        const val CREAR = "CREAR"
        const val EDITAR = "EDITAR"
    }
}

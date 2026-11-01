package com.example.appnombre_grupo1.model.repository

import com.example.appnombre_grupo1.model.DatosGeneralesErrores
import com.example.appnombre_grupo1.model.DetectorCambios
import com.example.appnombre_grupo1.model.EdicionRegistro
import com.example.appnombre_grupo1.model.MantenimientoState
import com.example.appnombre_grupo1.model.PruebasElectricasState
import com.example.appnombre_grupo1.model.RegistroGuardado
import com.example.appnombre_grupo1.model.TipoRegistro
import com.example.appnombre_grupo1.model.Usuario
import com.example.appnombre_grupo1.model.database.dao.RegistroDao
import com.example.appnombre_grupo1.model.database.toEntity
import com.example.appnombre_grupo1.model.database.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

// Registros de terreno, guardados en las tablas "registros", "mantenimientos",
// "pruebas_electricas" y "ediciones_registro" (Room). El más reciente primero.
class RegistroRepository(private val dao: RegistroDao) {

    // Registros activos: los que ven técnicos y administradores
    val registros: Flow<List<RegistroGuardado>> = dao.observarActivos().map { lista -> lista.map { it.toModel() } }

    // Registros archivados: solo los ve el administrador
    val archivados: Flow<List<RegistroGuardado>> = dao.observarArchivados().map { lista -> lista.map { it.toModel() } }

    suspend fun guardarMantenimiento(registro: MantenimientoState, tecnico: Usuario) {
        dao.guardar(nuevoRegistro(TipoRegistro.MANTENIMIENTO, tecnico).copy(mantenimiento = sinErrores(registro)).toEntity())
    }

    suspend fun guardarPruebas(registro: PruebasElectricasState, tecnico: Usuario) {
        dao.guardar(nuevoRegistro(TipoRegistro.PRUEBAS_ELECTRICAS, tecnico).copy(pruebas = sinErrores(registro)).toEntity())
    }

    // Guarda los cambios de un mantenimiento y anota quién lo editó y qué cambió.
    // Devuelve false si no había ningún cambio (en ese caso no se guarda nada).
    suspend fun actualizarMantenimiento(original: RegistroGuardado, nuevo: MantenimientoState, editor: Usuario): Boolean {
        val anterior = original.mantenimiento ?: return false
        val limpio = sinErrores(nuevo)
        val cambios = DetectorCambios.mantenimiento(anterior, limpio)
        if (cambios.isEmpty()) return false
        dao.actualizar(original.copy(mantenimiento = limpio).toEntity(), edicion(original, editor, cambios))
        return true
    }

    suspend fun actualizarPruebas(original: RegistroGuardado, nuevo: PruebasElectricasState, editor: Usuario): Boolean {
        val anterior = original.pruebas ?: return false
        val limpio = sinErrores(nuevo)
        val cambios = DetectorCambios.pruebas(anterior, limpio)
        if (cambios.isEmpty()) return false
        dao.actualizar(original.copy(pruebas = limpio).toEntity(), edicion(original, editor, cambios))
        return true
    }

    // "Eliminar" un registro: queda archivado y solo los administradores lo pueden ver
    suspend fun archivar(registroId: String, administrador: Usuario) {
        dao.archivar(registroId, administrador.username, administrador.nombre, System.currentTimeMillis())
    }

    suspend fun restaurar(registroId: String) {
        dao.restaurar(registroId)
    }

    // true si algún registro guardado (activo o archivado) usa esa foto
    suspend fun fotoEnUso(fotoPath: String): Boolean = dao.contarUsosDeFoto(fotoPath) > 0

    private fun edicion(original: RegistroGuardado, editor: Usuario, cambios: List<String>) =
        EdicionRegistro(
            fechaMillis = System.currentTimeMillis(),
            editorUsername = editor.username,
            editorNombre = editor.nombre,
            cambios = cambios,
        ).toEntity(original.id)

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
}

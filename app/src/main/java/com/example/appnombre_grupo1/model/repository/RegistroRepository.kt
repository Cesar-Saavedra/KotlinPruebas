package com.example.appnombre_grupo1.model.repository

import com.example.appnombre_grupo1.model.DatosGeneralesErrores
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

// Registros de terreno terminados, guardados en las tablas "registros",
// "mantenimientos" y "pruebas_electricas" (Room). El más reciente primero.
class RegistroRepository(private val dao: RegistroDao) {

    val registros: Flow<List<RegistroGuardado>> = dao.observarTodos().map { lista -> lista.map { it.toModel() } }

    suspend fun guardarMantenimiento(registro: MantenimientoState, tecnico: Usuario) {
        val limpio = registro.copy(datosGenerales = registro.datosGenerales.copy(errores = DatosGeneralesErrores()))
        dao.guardar(nuevoRegistro(TipoRegistro.MANTENIMIENTO, tecnico).copy(mantenimiento = limpio).toEntity())
    }

    suspend fun guardarPruebas(registro: PruebasElectricasState, tecnico: Usuario) {
        val limpio = registro.copy(datosGenerales = registro.datosGenerales.copy(errores = DatosGeneralesErrores()))
        dao.guardar(nuevoRegistro(TipoRegistro.PRUEBAS_ELECTRICAS, tecnico).copy(pruebas = limpio).toEntity())
    }

    // true si algún registro guardado usa esa foto
    suspend fun fotoEnUso(fotoPath: String): Boolean = dao.contarUsosDeFoto(fotoPath) > 0

    private fun nuevoRegistro(tipo: TipoRegistro, tecnico: Usuario) = RegistroGuardado(
        id = UUID.randomUUID().toString(),
        tipo = tipo,
        fechaMillis = System.currentTimeMillis(),
        tecnicoUsername = tecnico.username,
    )
}

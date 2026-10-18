package com.example.appnombre_grupo1.model.repository

import com.example.appnombre_grupo1.model.MantenimientoState
import com.example.appnombre_grupo1.model.PruebasElectricasState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Fuente de datos de los registros. Por ahora guarda en memoria;
// aquí se conectaría una base de datos local (Room) o una API remota.
class RegistroRepository {

    private val _mantenimientos = MutableStateFlow<List<MantenimientoState>>(emptyList())
    val mantenimientos: StateFlow<List<MantenimientoState>> = _mantenimientos.asStateFlow()

    private val _pruebasElectricas = MutableStateFlow<List<PruebasElectricasState>>(emptyList())
    val pruebasElectricas: StateFlow<List<PruebasElectricasState>> = _pruebasElectricas.asStateFlow()

    fun guardarMantenimiento(registro: MantenimientoState) {
        _mantenimientos.update { it + registro }
    }

    fun guardarPruebas(registro: PruebasElectricasState) {
        _pruebasElectricas.update { it + registro }
    }
}

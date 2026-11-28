package com.example.appnombre_grupo1

import com.example.appnombre_grupo1.model.DetectorCambios
import com.example.appnombre_grupo1.model.EstadoComponente
import com.example.appnombre_grupo1.model.MantenimientoState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectorCambiosTest {

    @Test
    fun detectorCambiosIdentificaModificacionEnObservacionesYCables() {
        val antes = MantenimientoState(observaciones = "Todo bien")
        val despues = MantenimientoState(observaciones = "Se encontró cable suelto", estadoCables = EstadoComponente.MALO)

        val cambios = DetectorCambios.mantenimiento(antes, despues)

        assertTrue(cambios.contains("Observaciones"))
        assertTrue(cambios.contains("Cables"))
        assertEquals(2, cambios.size)
    }

    @Test
    fun detectorCambiosDetectaCambioEnLaFotografia() {
        val antes = MantenimientoState(fotoPath = null)
        val despues = MantenimientoState(fotoPath = "/ruta/a/nueva_foto.jpg")

        val cambios = DetectorCambios.mantenimiento(antes, despues)

        assertTrue(cambios.contains("Fotografía"))
        assertEquals(1, cambios.size)
    }
    @Test
    fun cambioEnMantenimiento(){
        val antes = MantenimientoState(estadoCables = EstadoComponente.NO_APLICA)
        val despues = MantenimientoState(estadoCables = EstadoComponente.MALO)

        val cambios = DetectorCambios.mantenimiento(antes, despues)

        assertTrue(cambios.contains("Cables"))
        assertEquals(1, cambios.size)
    }
}

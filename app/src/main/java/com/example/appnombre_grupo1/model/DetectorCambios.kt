package com.example.appnombre_grupo1.model

// Compara un registro antes y después de editarlo y devuelve los nombres de los campos
// que cambiaron, para guardarlos en el historial (ej. "Cables, Observaciones")
object DetectorCambios {

    fun mantenimiento(antes: MantenimientoState, despues: MantenimientoState): List<String> = buildList {
        datosGenerales(antes.datosGenerales, despues.datosGenerales)
        comparar("Estado físico exterior", antes.estadoFisicoExterior to antes.descripcionFisicoExterior,
            despues.estadoFisicoExterior to despues.descripcionFisicoExterior)
        comparar("Aisladores / Bushing", antes.estadoAisladores to antes.descripcionAisladores,
            despues.estadoAisladores to despues.descripcionAisladores)
        comparar("Regletas", antes.estadoRegletas to antes.descripcionRegletas,
            despues.estadoRegletas to despues.descripcionRegletas)
        comparar("Contactores", antes.estadoContactores to antes.descripcionContactores,
            despues.estadoContactores to despues.descripcionContactores)
        comparar("Cables", antes.estadoCables to antes.descripcionCables,
            despues.estadoCables to despues.descripcionCables)
        comparar("Luces piloto", antes.estadoLucesPiloto to antes.descripcionLucesPiloto,
            despues.estadoLucesPiloto to despues.descripcionLucesPiloto)
        comparar("Canaletas", antes.estadoCanaletas to antes.descripcionCanaletas,
            despues.estadoCanaletas to despues.descripcionCanaletas)
        comparar("Observaciones", antes.observaciones, despues.observaciones)
        comparar("Fotografía", antes.fotoPath, despues.fotoPath)
    }

    fun pruebas(antes: PruebasElectricasState, despues: PruebasElectricasState): List<String> = buildList {
        datosGenerales(antes.datosGenerales, despues.datosGenerales)
        comparar("Tensión de prueba", antes.tensionPruebaAislamiento, despues.tensionPruebaAislamiento)
        comparar("Resistencia de aislamiento", antes.resistenciaAislamientoMOhm, despues.resistenciaAislamientoMOhm)
        comparar("Resistencia de contacto", antes.resistenciaContactoMicroOhm, despues.resistenciaContactoMicroOhm)
        comparar("Voltaje", antes.voltajeLineaV, despues.voltajeLineaV)
        comparar("Corriente", antes.corrienteCargaA, despues.corrienteCargaA)
        comparar("Veredicto", antes.veredicto, despues.veredicto)
        comparar("Notas técnicas", antes.notasTecnicas, despues.notasTecnicas)
    }

    private fun MutableList<String>.datosGenerales(antes: DatosGeneralesForm, despues: DatosGeneralesForm) {
        comparar("Cliente", antes.cliente, despues.cliente)
        comparar("Instalación", antes.instalacion, despues.instalacion)
        comparar("Tipo de equipo", antes.tipoEquipo, despues.tipoEquipo)
        comparar("Código del equipo", antes.codigoEquipo, despues.codigoEquipo)
    }

    private fun MutableList<String>.comparar(campo: String, antes: Any?, despues: Any?) {
        if (antes != despues) add(campo)
    }
}

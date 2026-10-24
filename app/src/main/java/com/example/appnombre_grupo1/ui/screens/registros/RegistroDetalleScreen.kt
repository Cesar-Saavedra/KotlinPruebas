package com.example.appnombre_grupo1.ui.screens.registros

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.appnombre_grupo1.model.EstadoComponente
import com.example.appnombre_grupo1.model.MantenimientoState
import com.example.appnombre_grupo1.model.PruebasElectricasState
import com.example.appnombre_grupo1.model.RegistroGuardado
import com.example.appnombre_grupo1.ui.components.FotoArchivo
import com.example.appnombre_grupo1.ui.utils.formatearFecha

private fun conUnidad(valor: String, unidad: String) = if (valor.isBlank()) "" else "$valor $unidad"

@Composable
private fun Seccion(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            contenido()
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Column {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
        Text(valor.ifBlank { "—" }, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun Componente(nombre: String, estado: EstadoComponente, descripcion: String) {
    Dato(nombre, if (descripcion.isBlank()) estado.label else "${estado.label}: $descripcion")
}

@Composable
private fun DetalleMantenimiento(estado: MantenimientoState) {
    Seccion("Inspección de componentes") {
        Componente("Estado físico exterior", estado.estadoFisicoExterior, estado.descripcionFisicoExterior)
        Componente("Aisladores / Bushing", estado.estadoAisladores, estado.descripcionAisladores)
        Componente("Regletas", estado.estadoRegletas, estado.descripcionRegletas)
        Componente("Contactores", estado.estadoContactores, estado.descripcionContactores)
        Componente("Cables", estado.estadoCables, estado.descripcionCables)
        Componente("Luces piloto", estado.estadoLucesPiloto, estado.descripcionLucesPiloto)
        Componente("Canaletas", estado.estadoCanaletas, estado.descripcionCanaletas)
    }

    Seccion("Observaciones") {
        Text(estado.observaciones.ifBlank { "Sin observaciones" })
    }

    estado.fotoPath?.let { foto ->
        Seccion("Fotografía") {
            FotoArchivo(
                path = foto,
                contentDescription = "Foto del mantenimiento",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(MaterialTheme.shapes.medium),
            )
        }
    }
}

@Composable
private fun DetallePruebas(estado: PruebasElectricasState) {
    Seccion("Mediciones") {
        Dato("Tensión de prueba de aislamiento", estado.tensionPruebaAislamiento)
        Dato("Resistencia de aislamiento", conUnidad(estado.resistenciaAislamientoMOhm, "MΩ"))
        Dato("Resistencia de contacto", conUnidad(estado.resistenciaContactoMicroOhm, "µΩ"))
        Dato("Voltaje", conUnidad(estado.voltajeLineaV, "V"))
        Dato("Corriente", conUnidad(estado.corrienteCargaA, "A"))
    }

    Seccion("Resultado") {
        Dato("Veredicto", estado.veredicto.label)
        if (estado.notasTecnicas.isNotBlank()) {
            Dato("Notas técnicas", estado.notasTecnicas)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroDetalleScreen(
    registro: RegistroGuardado,
    onBackClick: () -> Unit,
) {
    val datos = registro.datosGenerales

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(registro.tipo.label) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Seccion("Datos generales") {
                Dato("Fecha", formatearFecha(registro.fechaMillis))
                Dato("Técnico", "${datos.tecnico} (@${registro.tecnicoUsername})")
                Dato("Cliente", datos.cliente)
                Dato("Instalación", datos.instalacion)
                Dato("Tipo de equipo", datos.tipoEquipo)
                Dato("Código del equipo", datos.codigoEquipo)
            }

            registro.mantenimiento?.let { DetalleMantenimiento(it) }
            registro.pruebas?.let { DetallePruebas(it) }
        }
    }
}

package com.example.appnombre_grupo1.ui.screens.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appnombre_grupo1.model.EstadoComponente
import com.example.appnombre_grupo1.model.MantenimientoState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MantenimientoScreen(
    initialState: MantenimientoState,
    onSaveClick: (MantenimientoState) -> Unit,
    onBackClick: () -> Unit
) {
    var state by remember { mutableStateOf(initialState) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mantenimiento: ${state.datosGenerales.codigoEquipo}") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Inspección de Componentes", style = MaterialTheme.typography.titleMedium)

            // 1. Estado Físico Exterior
            SeccionEstadoComponente(
                titulo = "Estado Físico Exterior",
                estado = state.estadoFisicoExterior,
                descripcion = state.descripcionFisicoExterior,
                onEstadoChange = { state = state.copy(estadoFisicoExterior = it) },
                onDescripcionChange = { state = state.copy(descripcionFisicoExterior = it) }
            )

            // 2. Estado de Aisladores
            SeccionEstadoComponente(
                titulo = "Estado de Aisladores / Bushing",
                estado = state.estadoAisladores,
                descripcion = state.descripcionAisladores,
                onEstadoChange = { state = state.copy(estadoAisladores = it) },
                onDescripcionChange = { state = state.copy(descripcionAisladores = it) }
            )

            // 3. Estado de Regletas
            SeccionEstadoComponente(
                titulo = "Estado de las Regletas",
                estado = state.estadoRegletas,
                descripcion = state.descripcionRegletas,
                onEstadoChange = { state = state.copy(estadoRegletas = it) },
                onDescripcionChange = { state = state.copy(descripcionRegletas = it) }
            )

            // 4. Estado de Contactores
            SeccionEstadoComponente(
                titulo = "Estado de los Contactores",
                estado = state.estadoContactores,
                descripcion = state.descripcionContactores,
                onEstadoChange = { state = state.copy(estadoContactores = it) },
                onDescripcionChange = { state = state.copy(descripcionContactores = it) }
            )

            // 5. Estado de Cables
            SeccionEstadoComponente(
                titulo = "Estado de los Cables",
                estado = state.estadoCables,
                descripcion = state.descripcionCables,
                onEstadoChange = { state = state.copy(estadoCables = it) },
                onDescripcionChange = { state = state.copy(descripcionCables = it) }
            )

            // 6. Estado de Luces Piloto
            SeccionEstadoComponente(
                titulo = "Estado de Luces Piloto",
                estado = state.estadoLucesPiloto,
                descripcion = state.descripcionLucesPiloto,
                onEstadoChange = { state = state.copy(estadoLucesPiloto = it) },
                onDescripcionChange = { state = state.copy(descripcionLucesPiloto = it) }
            )

            // 7. Estado de Canaletas
            SeccionEstadoComponente(
                titulo = "Estado de Canaletas",
                estado = state.estadoCanaletas,
                descripcion = state.descripcionCanaletas,
                onEstadoChange = { state = state.copy(estadoCanaletas = it) },
                onDescripcionChange = { state = state.copy(descripcionCanaletas = it) }
            )

            Text("Observaciones Generales", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = state.observaciones,
                onValueChange = { state = state.copy(observaciones = it) },
                label = { Text("Detalles adicionales de la visita") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = { onSaveClick(state) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Mantenimiento")
            }

            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }
    }
}

@Composable
fun SeccionEstadoComponente(
    titulo: String,
    estado: EstadoComponente,
    descripcion: String,
    onEstadoChange: (EstadoComponente) -> Unit,
    onDescripcionChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(titulo, style = MaterialTheme.typography.bodyMedium)

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            EstadoComponente.values().forEach { op ->
                FilterChip(
                    selected = estado == op,
                    onClick = { onEstadoChange(op) },
                    label = { Text(op.label) }
                )
            }
        }

        // Muestra la caja de descripción si el estado es Regular o Malo (ni Bueno ni N/A)
        if (estado != EstadoComponente.BUENO && estado != EstadoComponente.NO_APLICA) {
            OutlinedTextField(
                value = descripcion,
                onValueChange = onDescripcionChange,
                label = { Text("Detalle / Observación de $titulo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
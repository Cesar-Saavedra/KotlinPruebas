package com.example.appnombre_grupo1.ui.screens.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MantenimientoScreen(
    onSaveClick: (MantenimientoState) -> Unit,
    onBackClick: () -> Unit
) {
    var state by remember { mutableStateOf(MantenimientoState()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Protocolo de Mantenimiento") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar a la pantalla principal"
                        )
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
            Text("1. Identificación y Entorno", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = state.datosGenerales.cliente,
                onValueChange = { state = state.copy(datosGenerales = state.datosGenerales.copy(cliente = it)) },
                label = { Text("Cliente / Subestación") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.datosGenerales.codigoEquipo,
                onValueChange = { state = state.copy(datosGenerales = state.datosGenerales.copy(codigoEquipo = it)) },
                label = { Text("Código / ID Equipo (ej. TR-01)") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("2. Inspección Visual y Componentes", style = MaterialTheme.typography.titleMedium)

            Text("Estado Físico Exterior:")
            SelectorEstadoComponente(
                seleccionado = state.estadoFisicoExterior,
                onSeleccionar = { state = state.copy(estadoFisicoExterior = it) }
            )

            Text("Estado de Aisladores / Bushing:")
            SelectorEstadoComponente(
                seleccionado = state.estadoAisladores,
                onSeleccionar = { state = state.copy(estadoAisladores = it) }
            )

            Text("3. Tareas Realizadas", style = MaterialTheme.typography.titleMedium)

            ItemSwitchCheck(
                label = "Limpieza general dieléctrica",
                checked = state.limpiezaRealizada,
                onCheckedChange = { state = state.copy(limpiezaRealizada = it) }
            )

            ItemSwitchCheck(
                label = "Reapriete y torqueo de bornes",
                checked = state.reajusteTorqueRealizado,
                onCheckedChange = { state = state.copy(reajusteTorqueRealizado = it) }
            )

            ItemSwitchCheck(
                label = "Lubricación de mecanismos móviles",
                checked = state.lubricacionRealizada,
                onCheckedChange = { state = state.copy(lubricacionRealizada = it) }
            )

            OutlinedTextField(
                value = state.observaciones,
                onValueChange = { state = state.copy(observaciones = it) },
                label = { Text("Observaciones / Hallazgos") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón de guardado
            Button(
                onClick = { onSaveClick(state) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Registro de Mantenimiento")
            }

            // Botón secundario para regresar
            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al Menú Principal")
            }
        }
    }
}

@Composable
private fun SelectorEstadoComponente(
    seleccionado: EstadoComponente,
    onSeleccionar: (EstadoComponente) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        EstadoComponente.entries.forEach { estado ->
            FilterChip(
                selected = seleccionado == estado,
                onClick = { onSeleccionar(estado) },
                label = { Text(estado.label) }
            )
        }
    }
}

@Composable
private fun ItemSwitchCheck(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
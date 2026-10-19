package com.example.appnombre_grupo1.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.appnombre_grupo1.model.ClienteForm
import com.example.appnombre_grupo1.model.Instalacion

// Campo de texto con un botón para agregar lo escrito
@Composable
private fun CampoAgregar(
    texto: String,
    etiqueta: String,
    onTextoChange: (String) -> Unit,
    onAgregar: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = texto,
            onValueChange = onTextoChange,
            label = { Text(etiqueta) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAgregar() }),
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onAgregar, enabled = texto.isNotBlank()) {
            Icon(Icons.Default.Add, contentDescription = "Agregar $etiqueta")
        }
    }
}

// Tarjeta de una instalación con la lista de tipos de equipo vinculados a ella
@Composable
private fun TarjetaInstalacion(
    instalacion: Instalacion,
    nuevoTipoEquipo: String,
    onNuevoTipoEquipoChange: (String) -> Unit,
    onAgregarTipoEquipo: () -> Unit,
    onQuitarTipoEquipo: (String) -> Unit,
    onQuitarInstalacion: () -> Unit,
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Text(
                    instalacion.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                )
                IconButton(onClick = onQuitarInstalacion) {
                    Icon(Icons.Default.Delete, contentDescription = "Quitar ${instalacion.nombre}")
                }
            }

            Text(
                "Tipos de equipo",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
            )

            if (instalacion.tiposEquipo.isEmpty()) {
                Text(
                    "Sin equipos. Agregue al menos uno",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            instalacion.tiposEquipo.forEach { tipo ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("• $tipo", modifier = Modifier.weight(1f))
                    IconButton(onClick = { onQuitarTipoEquipo(tipo) }) {
                        Icon(Icons.Default.Close, contentDescription = "Quitar $tipo")
                    }
                }
            }

            CampoAgregar(
                texto = nuevoTipoEquipo,
                etiqueta = "Nuevo tipo de equipo",
                onTextoChange = onNuevoTipoEquipoChange,
                onAgregar = onAgregarTipoEquipo,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteFormScreen(
    state: ClienteForm,
    onNombreChange: (String) -> Unit,
    onNuevaInstalacionChange: (String) -> Unit,
    onAgregarInstalacion: () -> Unit,
    onQuitarInstalacion: (String) -> Unit,
    onNuevoTipoEquipoChange: (instalacion: String, texto: String) -> Unit,
    onAgregarTipoEquipo: (instalacion: String) -> Unit,
    onQuitarTipoEquipo: (instalacion: String, tipo: String) -> Unit,
    onGuardarClick: () -> Unit,
    onEliminarClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    var confirmarEliminar by remember { mutableStateOf(value = false) }
    val esNuevo = state.id == null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esNuevo) "Nuevo Cliente" else "Editar Cliente") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    if (!esNuevo) {
                        IconButton(onClick = { confirmarEliminar = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar cliente")
                        }
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
            OutlinedTextField(
                value = state.nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre del cliente *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Instalaciones y sus equipos", style = MaterialTheme.typography.titleMedium)

            if (state.instalaciones.isEmpty()) {
                Text(
                    "Sin instalaciones",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            state.instalaciones.forEach { instalacion ->
                TarjetaInstalacion(
                    instalacion = instalacion,
                    nuevoTipoEquipo = state.nuevoTipoEquipo[instalacion.nombre].orEmpty(),
                    onNuevoTipoEquipoChange = { onNuevoTipoEquipoChange(instalacion.nombre, it) },
                    onAgregarTipoEquipo = { onAgregarTipoEquipo(instalacion.nombre) },
                    onQuitarTipoEquipo = { onQuitarTipoEquipo(instalacion.nombre, it) },
                    onQuitarInstalacion = { onQuitarInstalacion(instalacion.nombre) },
                )
            }

            CampoAgregar(
                texto = state.nuevaInstalacion,
                etiqueta = "Nueva instalación",
                onTextoChange = onNuevaInstalacionChange,
                onAgregar = onAgregarInstalacion,
            )

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = onGuardarClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text("Guardar Cliente")
            }
        }
    }

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("Eliminar cliente") },
            text = { Text("¿Seguro que desea eliminar a ${state.nombre}? Los registros anteriores no se borran.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarEliminar = false
                        onEliminarClick()
                    },
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") }
            },
        )
    }
}

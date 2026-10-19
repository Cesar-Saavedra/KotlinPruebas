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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.appnombre_grupo1.model.ClienteForm

// Lista de textos con un campo para agregar nuevos y un botón para quitar cada uno
@Composable
private fun ListaEditable(
    titulo: String,
    elementos: List<String>,
    nuevoTexto: String,
    etiquetaNuevo: String,
    onNuevoTextoChange: (String) -> Unit,
    onAgregar: () -> Unit,
    onQuitar: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleMedium)

        if (elementos.isEmpty()) {
            Text(
                "Sin elementos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }

        elementos.forEach { elemento ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(elemento, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onQuitar(elemento) }) {
                        Icon(Icons.Default.Close, contentDescription = "Quitar $elemento")
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = nuevoTexto,
                onValueChange = onNuevoTextoChange,
                label = { Text(etiquetaNuevo) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAgregar() }),
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onAgregar, enabled = nuevoTexto.isNotBlank()) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
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
    onNuevoTipoEquipoChange: (String) -> Unit,
    onAgregarTipoEquipo: () -> Unit,
    onQuitarTipoEquipo: (String) -> Unit,
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

            ListaEditable(
                titulo = "Instalaciones",
                elementos = state.instalaciones,
                nuevoTexto = state.nuevaInstalacion,
                etiquetaNuevo = "Nueva instalación",
                onNuevoTextoChange = onNuevaInstalacionChange,
                onAgregar = onAgregarInstalacion,
                onQuitar = onQuitarInstalacion,
            )

            ListaEditable(
                titulo = "Tipos de equipo",
                elementos = state.tiposEquipo,
                nuevoTexto = state.nuevoTipoEquipo,
                etiquetaNuevo = "Nuevo tipo de equipo",
                onNuevoTextoChange = onNuevoTipoEquipoChange,
                onAgregar = onAgregarTipoEquipo,
                onQuitar = onQuitarTipoEquipo,
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

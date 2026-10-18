package com.example.appnombre_grupo1.ui.screens.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.appnombre_grupo1.model.DatosGeneralesForm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroGeneralScreen(
    state: DatosGeneralesForm,
    mostrarErrores: Boolean,
    onStateChange: (DatosGeneralesForm) -> Unit,
    onContinuarMantenimiento: () -> Unit,
    onContinuarPruebas: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    val clienteError = state.errores.cliente
    val instalacionError = state.errores.instalacion
    val tecnicoError = state.errores.tecnico
    val tipoEquipoError = state.errores.tipoEquipo
    val codigoEquipoError = state.errores.codigoEquipo

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nuevo Registro de Terreno") }) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Datos del Cliente y Equipo", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = state.cliente,
                onValueChange = { onStateChange(state.copy(cliente = it)) },
                label = { Text("Cliente *") },
                isError = mostrarErrores && (clienteError != null),
                supportingText = {
                    if (mostrarErrores && (clienteError != null)) {
                        Text(clienteError, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.instalacion,
                onValueChange = { onStateChange(state.copy(instalacion = it)) },
                label = { Text("Instalación / Subestación *") },
                isError = mostrarErrores && (instalacionError != null),
                supportingText = {
                    if (mostrarErrores && (instalacionError != null)) {
                        Text(instalacionError, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.tecnico,
                onValueChange = {},
                // Se completa con el usuario que inició sesión y no se puede editar
                readOnly = true,
                label = { Text("Técnico / Ingeniero a cargo") },
                isError = mostrarErrores && (tecnicoError != null),
                supportingText = {
                    if (mostrarErrores && (tecnicoError != null)) {
                        Text(tecnicoError, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Usuario con sesión iniciada")
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.tipoEquipo,
                onValueChange = { onStateChange(state.copy(tipoEquipo = it)) },
                label = { Text("Tipo de Equipo (ej. Transformador, Celda) *") },
                isError = mostrarErrores && (tipoEquipoError != null),
                supportingText = {
                    if (mostrarErrores && (tipoEquipoError != null)) {
                        Text(tipoEquipoError, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.codigoEquipo,
                onValueChange = { onStateChange(state.copy(codigoEquipo = it)) },
                label = { Text("Código / ID / Tag del Equipo *") },
                isError = mostrarErrores && (codigoEquipoError != null),
                supportingText = {
                    if (mostrarErrores && (codigoEquipoError != null)) {
                        Text(codigoEquipoError, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Seleccione el Protocolo a Realizar:", style = MaterialTheme.typography.titleSmall)

            Button(
                onClick = onContinuarMantenimiento,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Ir a Protocolo de Mantenimiento")
            }

            Button(
                onClick = onContinuarPruebas,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            ) {
                Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Ir a Protocolo de Pruebas Eléctricas")
            }
        }
    }
}

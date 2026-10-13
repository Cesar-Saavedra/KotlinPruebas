package com.example.appnombre_grupo1.ui.screens.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appnombre_grupo1.model.DatosGeneralesForm
import com.example.appnombre_grupo1.model.DatosGeneralesValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroGeneralScreen(
    initialState: DatosGeneralesForm,
    onStateChange: (DatosGeneralesForm) -> Unit,
    onContinuarMantenimiento: (DatosGeneralesForm) -> Unit,
    onContinuarPruebas: (DatosGeneralesForm) -> Unit,
) {
    var showErrors by remember { mutableStateOf(value = false) }
    val validator = remember { DatosGeneralesValidator() }

    val validationResult = validator.validate(initialState)

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
                value = initialState.cliente,
                onValueChange = { onStateChange(initialState.copy(cliente = it)) },
                label = { Text("Cliente *") },
                isError = (showErrors && (validationResult.clienteError != null)),
                supportingText = {
                    if (showErrors && (validationResult.clienteError != null)) {
                        Text(validationResult.clienteError, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = initialState.instalacion,
                onValueChange = { onStateChange(initialState.copy(instalacion = it)) },
                label = { Text("Instalación / Subestación *") },
                isError = (showErrors && (validationResult.instalacionError != null)),
                supportingText = {
                    if (showErrors && (validationResult.instalacionError != null)) {
                        Text(validationResult.instalacionError, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = initialState.tecnico,
                onValueChange = { onStateChange(initialState.copy(tecnico = it)) },
                label = { Text("Técnico / Ingeniero a cargo *") },
                isError = (showErrors && (validationResult.tecnicoError != null)),
                supportingText = {
                    if (showErrors && (validationResult.tecnicoError != null)) {
                        Text(validationResult.tecnicoError, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = initialState.tipoEquipo,
                onValueChange = { onStateChange(initialState.copy(tipoEquipo = it)) },
                label = { Text("Tipo de Equipo (ej. Transformador, Celda) *") },
                isError = (showErrors && (validationResult.tipoEquipoError != null)),
                supportingText = {
                    if (showErrors && (validationResult.tipoEquipoError != null)) {
                        Text(validationResult.tipoEquipoError, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = initialState.codigoEquipo,
                onValueChange = { onStateChange(initialState.copy(codigoEquipo = it)) },
                label = { Text("Código / ID / Tag del Equipo *") },
                isError = (showErrors && (validationResult.codigoEquipoError != null)),
                supportingText = {
                    if (showErrors && (validationResult.codigoEquipoError != null)) {
                        Text(validationResult.codigoEquipoError, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Seleccione el Protocolo a Realizar:", style = MaterialTheme.typography.titleSmall)

            Button(
                onClick = {
                    showErrors = true
                    if (validationResult.isValid) {
                        onContinuarMantenimiento(initialState)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Ir a Protocolo de Mantenimiento")
            }

            Button(
                onClick = {
                    showErrors = true
                    if (validationResult.isValid) {
                        onContinuarPruebas(initialState)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Ir a Protocolo de Pruebas Eléctricas")
            }
        }
    }
}

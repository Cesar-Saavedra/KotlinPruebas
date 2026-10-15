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
    val focusManager = LocalFocusManager.current

    val form = validator.validate(initialState)

    val clienteError = form.errores.cliente
    val instalacionError = form.errores.instalacion
    val tecnicoError = form.errores.tecnico
    val tipoEquipoError = form.errores.tipoEquipo
    val codigoEquipoError = form.errores.codigoEquipo

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
                value = form.cliente,
                onValueChange = { onStateChange(validator.validate(form.copy(cliente = it))) },
                label = { Text("Cliente *") },
                isError = showErrors && (clienteError != null),
                supportingText = {
                    if (showErrors && (clienteError != null)) {
                        Text(clienteError, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = form.instalacion,
                onValueChange = { onStateChange(validator.validate(form.copy(instalacion = it))) },
                label = { Text("Instalación / Subestación *") },
                isError = showErrors && (instalacionError != null),
                supportingText = {
                    if (showErrors && (instalacionError != null)) {
                        Text(instalacionError, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = form.tecnico,
                onValueChange = { onStateChange(validator.validate(form.copy(tecnico = it))) },
                label = { Text("Técnico / Ingeniero a cargo *") },
                isError = showErrors && (tecnicoError != null),
                supportingText = {
                    if (showErrors && (tecnicoError != null)) {
                        Text(tecnicoError, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = form.tipoEquipo,
                onValueChange = { onStateChange(validator.validate(form.copy(tipoEquipo = it))) },
                label = { Text("Tipo de Equipo (ej. Transformador, Celda) *") },
                isError = showErrors && (tipoEquipoError != null),
                supportingText = {
                    if (showErrors && (tipoEquipoError != null)) {
                        Text(tipoEquipoError, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = form.codigoEquipo,
                onValueChange = { onStateChange(validator.validate(form.copy(codigoEquipo = it))) },
                label = { Text("Código / ID / Tag del Equipo *") },
                isError = showErrors && (codigoEquipoError != null),
                supportingText = {
                    if (showErrors && (codigoEquipoError != null)) {
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
                onClick = {
                    showErrors = true
                    val formValidated = validator.validate(form)
                    onStateChange(formValidated)
                    if (formValidated.errores.isValid) {
                        onContinuarMantenimiento(formValidated)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Ir a Protocolo de Mantenimiento")
            }

            Button(
                onClick = {
                    showErrors = true
                    val formValidated = validator.validate(form)
                    onStateChange(formValidated)
                    if (formValidated.errores.isValid) {
                        onContinuarPruebas(formValidated)
                    }
                },
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

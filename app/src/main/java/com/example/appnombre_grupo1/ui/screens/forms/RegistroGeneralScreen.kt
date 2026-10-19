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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.appnombre_grupo1.model.Cliente
import com.example.appnombre_grupo1.model.DatosGeneralesForm
import com.example.appnombre_grupo1.ui.components.SelectorDesplegable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroGeneralScreen(
    state: DatosGeneralesForm,
    clientes: List<Cliente>,
    mostrarErrores: Boolean,
    onStateChange: (DatosGeneralesForm) -> Unit,
    onClienteChange: (String) -> Unit,
    onInstalacionChange: (String) -> Unit,
    onContinuarMantenimiento: () -> Unit,
    onContinuarPruebas: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    // Las instalaciones dependen del cliente elegido y los equipos de la instalación elegida
    val clienteSeleccionado = clientes.firstOrNull { it.nombre == state.cliente }
    val instalacionSeleccionada = clienteSeleccionado?.instalaciones?.firstOrNull { it.nombre == state.instalacion }

    val clienteError = state.errores.cliente.takeIf { mostrarErrores }
    val instalacionError = state.errores.instalacion.takeIf { mostrarErrores }
    val tecnicoError = state.errores.tecnico.takeIf { mostrarErrores }
    val tipoEquipoError = state.errores.tipoEquipo.takeIf { mostrarErrores }
    val codigoEquipoError = state.errores.codigoEquipo.takeIf { mostrarErrores }

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

            SelectorDesplegable(
                etiqueta = "Cliente *",
                seleccion = state.cliente,
                opciones = clientes.map { it.nombre },
                onSeleccionar = onClienteChange,
                error = clienteError,
                textoDeshabilitado = "No hay clientes registrados. Contacte al administrador",
                modifier = Modifier.fillMaxWidth(),
            )

            SelectorDesplegable(
                etiqueta = "Instalación / Subestación *",
                seleccion = state.instalacion,
                opciones = clienteSeleccionado?.instalaciones.orEmpty().map { it.nombre },
                onSeleccionar = onInstalacionChange,
                error = instalacionError,
                textoDeshabilitado = "Seleccione primero un cliente",
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.tecnico,
                onValueChange = {},
                // Se completa con el usuario que inició sesión y no se puede editar
                readOnly = true,
                label = { Text("Técnico / Ingeniero a cargo") },
                isError = tecnicoError != null,
                supportingText = { Text(tecnicoError ?: "Usuario con sesión iniciada") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            SelectorDesplegable(
                etiqueta = "Tipo de Equipo *",
                seleccion = state.tipoEquipo,
                opciones = instalacionSeleccionada?.tiposEquipo.orEmpty(),
                onSeleccionar = { onStateChange(state.copy(tipoEquipo = it)) },
                error = tipoEquipoError,
                textoDeshabilitado = "Seleccione primero una instalación",
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.codigoEquipo,
                onValueChange = { onStateChange(state.copy(codigoEquipo = it)) },
                label = { Text("Código / ID / Tag del Equipo *") },
                isError = codigoEquipoError != null,
                supportingText = {
                    codigoEquipoError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
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

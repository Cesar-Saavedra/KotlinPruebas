package com.example.appnombre_grupo1.ui.screens.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.appnombre_grupo1.model.Cliente
import com.example.appnombre_grupo1.model.DatosGeneralesForm
import com.example.appnombre_grupo1.ui.components.SelectorDesplegable
import com.example.appnombre_grupo1.ui.utils.UbicacionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroGeneralScreen(
    state: DatosGeneralesForm,
    clientes: List<Cliente>,
    mostrarErrores: Boolean,
    distanciaMetros: Float? = null,
    buscandoUbicacion: Boolean = false,
    errorGps: String? = null,
    onStateChange: (DatosGeneralesForm) -> Unit,
    onClienteChange: (String) -> Unit,
    onInstalacionChange: (String) -> Unit,
    onDetectarInstalacionGps: () -> Unit = {},
    onActualizarUbicacionGps: () -> Unit = {},
    onContinuarMantenimiento: () -> Unit,
    onContinuarPruebas: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

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

            // --- Sección Verificación y Auto-detección GPS ---
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📍 Verificación GPS (Máx. 100m)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (buscandoUbicacion) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDetectarInstalacionGps,
                            modifier = Modifier.weight(1f),
                            enabled = !buscandoUbicacion
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Auto-detectar", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = onActualizarUbicacionGps,
                            enabled = !buscandoUbicacion
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }

                    if (instalacionSeleccionada != null) {
                        if (instalacionSeleccionada.latitud != null && instalacionSeleccionada.longitud != null) {
                            if (distanciaMetros != null) {
                                val enRango = distanciaMetros <= UbicacionUtils.RANGO_MAXIMO_METROS
                                Surface(
                                    color = if (enRango) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = if (enRango) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (enRango) {
                                                "✅ En rango permitido (%.0f m de la instalación)".format(distanciaMetros)
                                            } else {
                                                "⚠️ Fuera de rango (%.0f m de la instalación, máx 100m)".format(distanciaMetros)
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (enRango) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    "Presione 'Auto-detectar' o 'Actualizar GPS' para verificar distancia.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        } else {
                            Text(
                                "ℹ️ Esta instalación no tiene coordenadas GPS asignadas por el administrador.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    errorGps?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

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

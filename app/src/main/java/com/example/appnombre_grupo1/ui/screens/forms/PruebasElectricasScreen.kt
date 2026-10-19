package com.example.appnombre_grupo1.ui.screens.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.appnombre_grupo1.ui.components.CargandoOverlay
import com.example.appnombre_grupo1.model.PruebasElectricasState
import com.example.appnombre_grupo1.model.VeredictoPrueba

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PruebasElectricasScreen(
    state: PruebasElectricasState,
    guardando: Boolean,
    onStateChange: (PruebasElectricasState) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Protocolo de Pruebas Eléctricas") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar a la pantalla principal",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("1. Datos del Equipo", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = state.datosGenerales.codigoEquipo,
                onValueChange = { onStateChange(state.copy(datosGenerales = state.datosGenerales.copy(codigoEquipo = it))) },
                label = { Text("Código / Tag del Equipo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )

            Text("2. Mediciones de Aislamiento y Resistencia", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = state.resistenciaAislamientoMOhm,
                onValueChange = { onStateChange(state.copy(resistenciaAislamientoMOhm = it)) },
                label = { Text("Resistencia de Aislamiento (MΩ)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.resistenciaContactoMicroOhm,
                onValueChange = { onStateChange(state.copy(resistenciaContactoMicroOhm = it)) },
                label = { Text("Resistencia de Contacto (µΩ)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )

            Text("3. Magnitudes de Operación", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.voltajeLineaV,
                    onValueChange = { onStateChange(state.copy(voltajeLineaV = it)) },
                    label = { Text("Voltaje (V)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state.corrienteCargaA,
                    onValueChange = { onStateChange(state.copy(corrienteCargaA = it)) },
                    label = { Text("Corriente (A)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.weight(1f)
                )
            }

            Text("4. Resultado de la Prueba", style = MaterialTheme.typography.titleMedium)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                VeredictoPrueba.entries.forEach { veredicto ->
                    FilterChip(
                        selected = state.veredicto == veredicto,
                        onClick = { onStateChange(state.copy(veredicto = veredicto)) },
                        label = { Text(veredicto.label) }
                    )
                }
            }

            if ((state.veredicto == VeredictoPrueba.OBSERVADO) || (state.veredicto == VeredictoPrueba.RECHAZADO)) {
                OutlinedTextField(
                    value = state.notasTecnicas,
                    onValueChange = { onStateChange(state.copy(notasTecnicas = it)) },
                    label = { Text("Detalle / Observación del veredicto") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón de guardado
            Button(
                onClick = onSaveClick,
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Protocolo de Prueba")
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

    if (guardando) {
        CargandoOverlay(mensaje = "Guardando prueba...")
    }
}

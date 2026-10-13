package com.example.appnombre_grupo1.ui.screens.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.appnombre_grupo1.model.PruebasElectricasState
import com.example.appnombre_grupo1.model.VeredictoPrueba

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PruebasElectricasScreen(
    initialState: PruebasElectricasState,
    onSaveClick: (PruebasElectricasState) -> Unit,
    onBackClick: () -> Unit
) {
    var state by remember { mutableStateOf(initialState) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Protocolo de Pruebas Eléctricas") },
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
            Text("1. Datos del Equipo", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = state.datosGenerales.codigoEquipo,
                onValueChange = { state = state.copy(datosGenerales = state.datosGenerales.copy(codigoEquipo = it)) },
                label = { Text("Código / Tag del Equipo") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("2. Mediciones de Aislamiento y Resistencia", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = state.resistenciaAislamientoMOhm,
                onValueChange = { state = state.copy(resistenciaAislamientoMOhm = it) },
                label = { Text("Resistencia de Aislamiento (MΩ)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.resistenciaContactoMicroOhm,
                onValueChange = { state = state.copy(resistenciaContactoMicroOhm = it) },
                label = { Text("Resistencia de Contacto (µΩ)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Text("3. Magnitudes de Operación", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.voltajeLineaV,
                    onValueChange = { state = state.copy(voltajeLineaV = it) },
                    label = { Text("Voltaje (V)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state.corrienteCargaA,
                    onValueChange = { state = state.copy(corrienteCargaA = it) },
                    label = { Text("Corriente (A)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        onClick = { state = state.copy(veredicto = veredicto) },
                        label = { Text(veredicto.label) }
                    )
                }
            }

            if (state.veredicto == VeredictoPrueba.OBSERVADO || state.veredicto == VeredictoPrueba.RECHAZADO) {
                OutlinedTextField(
                    value = state.notasTecnicas,
                    onValueChange = { state = state.copy(notasTecnicas = it) },
                    label = { Text("Detalle / Observación del veredicto") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón de guardado
            Button(
                onClick = { onSaveClick(state) },
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
}
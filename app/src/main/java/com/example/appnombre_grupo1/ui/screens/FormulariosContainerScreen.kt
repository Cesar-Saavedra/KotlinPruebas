package com.example.appnombre_grupo1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appnombre_grupo1.ui.screens.forms.*
import com.example.appnombre_grupo1.viewmodels.FormularioViewModel

enum class PasoFormulario {
    INICIO,
    DATOS_GENERALES,
    MANTENIMIENTO,
    PRUEBAS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulariosContainerScreen(
    viewModel: FormularioViewModel = viewModel(),
) {
    // Observamos la navegación desde el ViewModel
    val pasoActual by viewModel.pasoActual.collectAsState()

    val datosGenerales by viewModel.datosGenerales.collectAsState()
    val mantenimientoState by viewModel.mantenimientoState.collectAsState()
    val pruebasState by viewModel.pruebasElectricasState.collectAsState()

    when (pasoActual) {
        PasoFormulario.INICIO -> {
            Scaffold(
                topBar = { TopAppBar(title = { Text("Induztek - Registro de Terreno") }) }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { viewModel.reiniciarRegistro() },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(56.dp)
                    ) {
                        Text("Nuevo Registro", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        PasoFormulario.DATOS_GENERALES -> {
            RegistroGeneralScreen(
                initialState = datosGenerales,
                onStateChange = { datos -> viewModel.actualizarDatosGenerales(datos) },
                onContinuarMantenimiento = { datos ->
                    viewModel.actualizarDatosGenerales(datos)
                    viewModel.navegarA(PasoFormulario.MANTENIMIENTO)
                },
                onContinuarPruebas = { datos ->
                    viewModel.actualizarDatosGenerales(datos)
                    viewModel.navegarA(PasoFormulario.PRUEBAS)
                }
            )
        }

        PasoFormulario.MANTENIMIENTO -> {
            MantenimientoScreen(
                initialState = mantenimientoState,
                onStateChange = { nuevoEstado -> viewModel.actualizarMantenimiento(nuevoEstado) },
                onSaveClick = { nuevoEstado ->
                    viewModel.actualizarMantenimiento(nuevoEstado)
                    // Guardado final local/remoto
                    viewModel.navegarA(PasoFormulario.INICIO)
                },
                onBackClick = { viewModel.navegarA(PasoFormulario.DATOS_GENERALES) }
            )
        }

        PasoFormulario.PRUEBAS -> {
            PruebasElectricasScreen(
                initialState = pruebasState,
                onStateChange = { nuevoEstado -> viewModel.actualizarPruebas(nuevoEstado) },
                onSaveClick = { nuevoEstado ->
                    viewModel.actualizarPruebas(nuevoEstado)
                    // Guardado final local/remoto
                    viewModel.navegarA(PasoFormulario.INICIO)
                },
                onBackClick = { viewModel.navegarA(PasoFormulario.DATOS_GENERALES) }
            )
        }
    }
}
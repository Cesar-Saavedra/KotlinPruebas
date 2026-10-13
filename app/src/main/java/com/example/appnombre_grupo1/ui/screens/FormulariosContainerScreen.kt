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
    viewModel: FormularioViewModel = viewModel()
) {
    var pasoActual by remember { mutableStateOf(PasoFormulario.INICIO) }

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
                        onClick = {
                            viewModel.reiniciarRegistro()
                            pasoActual = PasoFormulario.DATOS_GENERALES
                        },
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
                datosIniciales = datosGenerales,
                onContinuarMantenimiento = { datos ->
                    viewModel.actualizarDatosGenerales(datos)
                    pasoActual = PasoFormulario.MANTENIMIENTO
                },
                onContinuarPruebas = { datos ->
                    viewModel.actualizarDatosGenerales(datos)
                    pasoActual = PasoFormulario.PRUEBAS
                }
            )
        }

        PasoFormulario.MANTENIMIENTO -> {
            MantenimientoScreen(
                initialState = mantenimientoState,
                onSaveClick = { nuevoEstado ->
                    viewModel.actualizarMantenimiento(nuevoEstado)
                    // Guardado final en ViewModel / BD
                    pasoActual = PasoFormulario.INICIO
                },
                onBackClick = { pasoActual = PasoFormulario.DATOS_GENERALES }
            )
        }

        PasoFormulario.PRUEBAS -> {
            PruebasElectricasScreen(
                initialState = pruebasState,
                onSaveClick = { nuevoEstado ->
                    viewModel.actualizarPruebas(nuevoEstado)
                    // Guardado final en ViewModel / BD
                    pasoActual = PasoFormulario.INICIO
                },
                onBackClick = { pasoActual = PasoFormulario.DATOS_GENERALES }
            )
        }
    }
}
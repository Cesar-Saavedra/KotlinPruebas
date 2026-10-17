package com.example.appnombre_grupo1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appnombre_grupo1.navigation.Screen
import com.example.appnombre_grupo1.ui.screens.forms.*
import com.example.appnombre_grupo1.viewmodels.FormularioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulariosContainerScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: FormularioViewModel = viewModel(),
) {
    val datosGenerales by viewModel.datosGenerales.collectAsState()
    val mantenimientoState by viewModel.mantenimientoState.collectAsState()
    val pruebasState by viewModel.pruebasElectricasState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Form.route
    ) {
        composable(Screen.Form.route) {
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
                            navController.navigate(Screen.GeneralForm.route)
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

        composable(Screen.GeneralForm.route) {
            RegistroGeneralScreen(
                initialState = datosGenerales,
                onStateChange = { datos -> viewModel.actualizarDatosGenerales(datos) },
                onContinuarMantenimiento = { datos ->
                    viewModel.actualizarDatosGenerales(datos)
                    navController.navigate(Screen.Maintenance.route)
                },
                onContinuarPruebas = { datos ->
                    viewModel.actualizarDatosGenerales(datos)
                    navController.navigate(Screen.Test.route)
                }
            )
        }

        composable(Screen.Maintenance.route) {
            MantenimientoScreen(
                initialState = mantenimientoState,
                onStateChange = { nuevoEstado -> viewModel.actualizarMantenimiento(nuevoEstado) },
                onSaveClick = { nuevoEstado ->
                    viewModel.actualizarMantenimiento(nuevoEstado)
                    // Guardado final local/remoto y volver al inicio
                    navController.navigate(Screen.Form.route) {
                        popUpTo(Screen.Form.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Test.route) {
            PruebasElectricasScreen(
                initialState = pruebasState,
                onStateChange = { nuevoEstado -> viewModel.actualizarPruebas(nuevoEstado) },
                onSaveClick = { nuevoEstado ->
                    viewModel.actualizarPruebas(nuevoEstado)
                    // Guardado final local/remoto y volver al inicio
                    navController.navigate(Screen.Form.route) {
                        popUpTo(Screen.Form.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

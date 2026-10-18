package com.example.appnombre_grupo1.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appnombre_grupo1.ui.screens.HomeScreen
import com.example.appnombre_grupo1.ui.screens.LoginScreen
import com.example.appnombre_grupo1.ui.screens.ProfileScreen
import com.example.appnombre_grupo1.ui.screens.SettingsScreen
import com.example.appnombre_grupo1.ui.screens.forms.InicioRegistroScreen
import com.example.appnombre_grupo1.ui.screens.forms.MantenimientoScreen
import com.example.appnombre_grupo1.ui.screens.forms.PruebasElectricasScreen
import com.example.appnombre_grupo1.ui.screens.forms.RegistroGeneralScreen
import com.example.appnombre_grupo1.viewmodels.AuthViewModel
import com.example.appnombre_grupo1.viewmodels.FormularioViewModel
import com.example.appnombre_grupo1.viewmodels.MainViewModel
import kotlinx.coroutines.flow.collectLatest

// Único grafo de navegación de la app: conecta cada pantalla (View) con su ViewModel
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = viewModel(),
    formularioViewModel: FormularioViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
) {
    val usuarioActual by authViewModel.usuarioActual.collectAsState()

    // Si ya había una sesión guardada en el teléfono, se salta el login
    val startDestination = remember {
        if (authViewModel.usuarioActual.value != null) Screen.Form.route else Screen.Login.route
    }

    // Observa los eventos de navegación emitidos por el MainViewModel
    LaunchedEffect(navController) {
        mainViewModel.navigationEvents.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateTo -> {
                    navController.navigate(event.route.route) {
                        event.popUpToRoute?.let { target ->
                            popUpTo(target.route) {
                                inclusive = event.inclusive
                            }
                        }
                        launchSingleTop = event.singleTop
                        restoreState = true
                    }
                }
                is NavigationEvent.PopBackStack -> navController.popBackStack()
                is NavigationEvent.NavigateUp -> navController.navigateUp()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(Screen.Login.route) {
            val loginForm by authViewModel.loginForm.collectAsState()

            LoginScreen(
                state = loginForm,
                onUsernameChange = authViewModel::actualizarUsername,
                onPasswordChange = authViewModel::actualizarPassword,
                onLoginClick = {
                    if (authViewModel.iniciarSesion()) {
                        // Se quita el login del historial para que "atrás" no vuelva a él
                        mainViewModel.navigateTo(Screen.Form, popUpToRoute = Screen.Login, inclusive = true)
                    }
                }
            )
        }

        // Flujo de registro de terreno
        composable(Screen.Form.route) {
            // Tras cerrar sesión el usuario es null mientras se navega al login
            val usuario = usuarioActual ?: return@composable

            InicioRegistroScreen(
                usuario = usuario,
                onNuevoRegistro = {
                    formularioViewModel.reiniciarRegistro(usuario.nombre)
                    mainViewModel.navigateTo(Screen.GeneralForm)
                },
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    mainViewModel.navigateTo(Screen.Login, popUpToRoute = Screen.Form, inclusive = true)
                }
            )
        }

        composable(Screen.GeneralForm.route) {
            val datosGenerales by formularioViewModel.datosGenerales.collectAsState()
            val mostrarErrores by formularioViewModel.mostrarErrores.collectAsState()

            RegistroGeneralScreen(
                state = datosGenerales,
                mostrarErrores = mostrarErrores,
                onStateChange = formularioViewModel::actualizarDatosGenerales,
                onContinuarMantenimiento = {
                    if (formularioViewModel.validarDatosGenerales()) {
                        mainViewModel.navigateTo(Screen.Maintenance)
                    }
                },
                onContinuarPruebas = {
                    if (formularioViewModel.validarDatosGenerales()) {
                        mainViewModel.navigateTo(Screen.Test)
                    }
                }
            )
        }

        composable(Screen.Maintenance.route) {
            val mantenimientoState by formularioViewModel.mantenimientoState.collectAsState()

            MantenimientoScreen(
                state = mantenimientoState,
                onStateChange = formularioViewModel::actualizarMantenimiento,
                onSaveClick = {
                    formularioViewModel.guardarMantenimiento()
                    // Vuelve al inicio limpiando el historial del registro
                    mainViewModel.navigateTo(Screen.Form, popUpToRoute = Screen.Form, inclusive = true)
                },
                onBackClick = mainViewModel::navigateBack
            )
        }

        composable(Screen.Test.route) {
            val pruebasState by formularioViewModel.pruebasElectricasState.collectAsState()

            PruebasElectricasScreen(
                state = pruebasState,
                onStateChange = formularioViewModel::actualizarPruebas,
                onSaveClick = {
                    formularioViewModel.guardarPruebas()
                    mainViewModel.navigateTo(Screen.Form, popUpToRoute = Screen.Form, inclusive = true)
                },
                onBackClick = mainViewModel::navigateBack
            )
        }

        // Pantallas generales
        composable(Screen.Home.route) {
            HomeScreen(viewModel = mainViewModel)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(viewModel = mainViewModel)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = mainViewModel)
        }
    }
}

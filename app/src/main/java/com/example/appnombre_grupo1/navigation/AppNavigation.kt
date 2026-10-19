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
import com.example.appnombre_grupo1.model.Cliente
import com.example.appnombre_grupo1.model.RolUsuario
import com.example.appnombre_grupo1.model.Usuario
import com.example.appnombre_grupo1.ui.screens.HomeScreen
import com.example.appnombre_grupo1.ui.screens.LoginScreen
import com.example.appnombre_grupo1.ui.screens.ProfileScreen
import com.example.appnombre_grupo1.ui.screens.SettingsScreen
import com.example.appnombre_grupo1.ui.screens.admin.AdminInicioScreen
import com.example.appnombre_grupo1.ui.screens.admin.ClienteFormScreen
import com.example.appnombre_grupo1.ui.screens.admin.ClientesScreen
import com.example.appnombre_grupo1.ui.screens.admin.RegistroDetalleScreen
import com.example.appnombre_grupo1.ui.screens.admin.RegistrosScreen
import com.example.appnombre_grupo1.ui.screens.admin.UsuarioFormScreen
import com.example.appnombre_grupo1.ui.screens.admin.UsuariosScreen
import com.example.appnombre_grupo1.ui.screens.forms.InicioRegistroScreen
import com.example.appnombre_grupo1.ui.screens.forms.MantenimientoScreen
import com.example.appnombre_grupo1.ui.screens.forms.PruebasElectricasScreen
import com.example.appnombre_grupo1.ui.screens.forms.RegistroGeneralScreen
import com.example.appnombre_grupo1.viewmodels.AuthViewModel
import com.example.appnombre_grupo1.viewmodels.ClientesViewModel
import com.example.appnombre_grupo1.viewmodels.FormularioViewModel
import com.example.appnombre_grupo1.viewmodels.MainViewModel
import com.example.appnombre_grupo1.viewmodels.RegistrosViewModel
import com.example.appnombre_grupo1.viewmodels.UsuariosViewModel
import kotlinx.coroutines.flow.collectLatest

// Pantalla de inicio que corresponde a cada rol
private fun inicioSegunRol(usuario: Usuario): Screen =
    if (usuario.rol == RolUsuario.ADMINISTRADOR) Screen.AdminHome else Screen.Form

// Único grafo de navegación de la app: conecta cada pantalla (View) con su ViewModel
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    formularioViewModel: FormularioViewModel = viewModel(),
    clientesViewModel: ClientesViewModel = viewModel(),
    usuariosViewModel: UsuariosViewModel = viewModel(),
    registrosViewModel: RegistrosViewModel = viewModel(),
) {
    val usuarioActual by authViewModel.usuarioActual.collectAsState()

    // Si ya había una sesión guardada en el teléfono, se salta el login
    val startDestination = remember {
        authViewModel.usuarioActual.value?.let { inicioSegunRol(it).route } ?: Screen.Login.route
    }

    // Observa los eventos de navegación emitidos por el MainViewModel
    LaunchedEffect(navController) {
        mainViewModel.navigationEvents.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateTo -> {
                    navController.navigate(event.route.route) {
                        if (event.limpiarHistorial) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        } else {
                            event.popUpToRoute?.let { target ->
                                popUpTo(target.route) {
                                    inclusive = event.inclusive
                                }
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

    val cerrarSesion = {
        authViewModel.cerrarSesion()
        mainViewModel.navigateTo(Screen.Login, limpiarHistorial = true)
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
                    authViewModel.iniciarSesion()?.let { usuario ->
                        // Se limpia el historial para que "atrás" no vuelva al login
                        mainViewModel.navigateTo(inicioSegunRol(usuario), limpiarHistorial = true)
                    }
                },
            )
        }

        // Flujo de registro de terreno (técnico)
        composable(Screen.Form.route) {
            // Tras cerrar sesión el usuario es null mientras se navega al login
            val usuario = usuarioActual ?: return@composable

            InicioRegistroScreen(
                usuario = usuario,
                onNuevoRegistro = {
                    formularioViewModel.reiniciarRegistro(usuario)
                    mainViewModel.navigateTo(Screen.GeneralForm)
                },
                onCerrarSesion = cerrarSesion,
            )
        }

        composable(Screen.GeneralForm.route) {
            val datosGenerales by formularioViewModel.datosGenerales.collectAsState()
            val clientes by formularioViewModel.clientes.collectAsState()
            val mostrarErrores by formularioViewModel.mostrarErrores.collectAsState()

            RegistroGeneralScreen(
                state = datosGenerales,
                clientes = clientes,
                mostrarErrores = mostrarErrores,
                onStateChange = formularioViewModel::actualizarDatosGenerales,
                onClienteChange = formularioViewModel::seleccionarCliente,
                onInstalacionChange = formularioViewModel::seleccionarInstalacion,
                onContinuarMantenimiento = {
                    if (formularioViewModel.validarDatosGenerales()) {
                        mainViewModel.navigateTo(Screen.Maintenance)
                    }
                },
                onContinuarPruebas = {
                    if (formularioViewModel.validarDatosGenerales()) {
                        mainViewModel.navigateTo(Screen.Test)
                    }
                },
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
                onBackClick = mainViewModel::navigateBack,
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
                onBackClick = mainViewModel::navigateBack,
            )
        }

        // Panel de administración
        composable(Screen.AdminHome.route) {
            val usuario = usuarioActual ?: return@composable

            AdminInicioScreen(
                usuario = usuario,
                onClientesClick = { mainViewModel.navigateTo(Screen.Clientes) },
                onUsuariosClick = { mainViewModel.navigateTo(Screen.Usuarios) },
                onRegistrosClick = { mainViewModel.navigateTo(Screen.Registros) },
                onCerrarSesion = cerrarSesion,
            )
        }

        composable(Screen.Clientes.route) {
            val clientes by clientesViewModel.clientes.collectAsState()

            ClientesScreen(
                clientes = clientes,
                onClienteClick = { cliente: Cliente ->
                    clientesViewModel.editarCliente(cliente)
                    mainViewModel.navigateTo(Screen.ClienteForm)
                },
                onAgregarClick = {
                    clientesViewModel.nuevoCliente()
                    mainViewModel.navigateTo(Screen.ClienteForm)
                },
                onBackClick = mainViewModel::navigateBack,
            )
        }

        composable(Screen.ClienteForm.route) {
            val form by clientesViewModel.form.collectAsState()

            ClienteFormScreen(
                state = form,
                onNombreChange = clientesViewModel::actualizarNombre,
                onNuevaInstalacionChange = clientesViewModel::actualizarNuevaInstalacion,
                onAgregarInstalacion = clientesViewModel::agregarInstalacion,
                onQuitarInstalacion = clientesViewModel::quitarInstalacion,
                onNuevoTipoEquipoChange = clientesViewModel::actualizarNuevoTipoEquipo,
                onAgregarTipoEquipo = clientesViewModel::agregarTipoEquipo,
                onQuitarTipoEquipo = clientesViewModel::quitarTipoEquipo,
                onGuardarClick = {
                    if (clientesViewModel.guardar()) mainViewModel.navigateBack()
                },
                onEliminarClick = {
                    clientesViewModel.eliminar()
                    mainViewModel.navigateBack()
                },
                onBackClick = mainViewModel::navigateBack,
            )
        }

        composable(Screen.Usuarios.route) {
            val usuarios by usuariosViewModel.usuarios.collectAsState()

            UsuariosScreen(
                usuarios = usuarios,
                usernameActual = usuarioActual?.username.orEmpty(),
                onUsuarioClick = { usuario ->
                    usuariosViewModel.editarUsuario(usuario, usuarioActual?.username.orEmpty())
                    mainViewModel.navigateTo(Screen.UsuarioForm)
                },
                onAgregarClick = {
                    usuariosViewModel.nuevoUsuario()
                    mainViewModel.navigateTo(Screen.UsuarioForm)
                },
                onBackClick = mainViewModel::navigateBack,
            )
        }

        composable(Screen.UsuarioForm.route) {
            val form by usuariosViewModel.form.collectAsState()

            UsuarioFormScreen(
                state = form,
                onUsernameChange = usuariosViewModel::actualizarUsername,
                onNombreChange = usuariosViewModel::actualizarNombre,
                onRolChange = usuariosViewModel::actualizarRol,
                onPasswordChange = usuariosViewModel::actualizarPassword,
                onGuardarClick = {
                    if (usuariosViewModel.guardar()) mainViewModel.navigateBack()
                },
                onEliminarClick = {
                    usuariosViewModel.eliminar()
                    mainViewModel.navigateBack()
                },
                onBackClick = mainViewModel::navigateBack,
            )
        }

        composable(Screen.Registros.route) {
            val registros by registrosViewModel.registros.collectAsState()

            RegistrosScreen(
                registros = registros,
                onRegistroClick = { registro ->
                    registrosViewModel.seleccionar(registro)
                    mainViewModel.navigateTo(Screen.RegistroDetalle)
                },
                onBackClick = mainViewModel::navigateBack,
            )
        }

        composable(Screen.RegistroDetalle.route) {
            val registro by registrosViewModel.registroSeleccionado.collectAsState()

            registro?.let {
                RegistroDetalleScreen(
                    registro = it,
                    onBackClick = mainViewModel::navigateBack,
                )
            }
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

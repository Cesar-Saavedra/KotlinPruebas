package com.example.appnombre_grupo1.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appnombre_grupo1.model.Cliente
import com.example.appnombre_grupo1.model.RegistroGuardado
import com.example.appnombre_grupo1.model.RolUsuario
import com.example.appnombre_grupo1.model.Usuario
import com.example.appnombre_grupo1.ui.screens.HomeScreen
import com.example.appnombre_grupo1.ui.screens.LoginScreen
import com.example.appnombre_grupo1.ui.screens.ProfileScreen
import com.example.appnombre_grupo1.ui.screens.SettingsScreen
import com.example.appnombre_grupo1.ui.screens.admin.AdminInicioScreen
import com.example.appnombre_grupo1.ui.screens.admin.ClienteFormScreen
import com.example.appnombre_grupo1.ui.screens.admin.ClientesScreen
import com.example.appnombre_grupo1.ui.screens.admin.UsuarioFormScreen
import com.example.appnombre_grupo1.ui.screens.admin.UsuariosScreen
import com.example.appnombre_grupo1.ui.screens.forms.InicioRegistroScreen
import com.example.appnombre_grupo1.ui.screens.forms.MantenimientoScreen
import com.example.appnombre_grupo1.ui.screens.forms.PruebasElectricasScreen
import com.example.appnombre_grupo1.ui.screens.forms.RegistroGeneralScreen
import com.example.appnombre_grupo1.ui.screens.registros.RegistroDetalleScreen
import com.example.appnombre_grupo1.ui.screens.registros.RegistrosScreen
import com.example.appnombre_grupo1.viewmodels.AuthViewModel
import com.example.appnombre_grupo1.viewmodels.ClientesViewModel
import com.example.appnombre_grupo1.viewmodels.FormularioViewModel
import com.example.appnombre_grupo1.viewmodels.MainViewModel
import com.example.appnombre_grupo1.viewmodels.RegistrosViewModel
import com.example.appnombre_grupo1.viewmodels.ResultadoGuardado
import com.example.appnombre_grupo1.viewmodels.UsuariosViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
    // Segunda instancia del mismo ViewModel, solo para editar registros guardados,
    // así editar no borra un registro nuevo que se esté llenando
    edicionViewModel: FormularioViewModel = viewModel(key = "edicion"),
    clientesViewModel: ClientesViewModel = viewModel(),
    usuariosViewModel: UsuariosViewModel = viewModel(),
    registrosViewModel: RegistrosViewModel = viewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val usuarioActual by authViewModel.usuarioActual.collectAsState()
    val sesion by authViewModel.sesion.collectAsState()

    // Mientras se lee la base de datos al abrir la app se muestra un círculo de carga
    val sesionLeida = sesion ?: run {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Si ya había una sesión guardada en el teléfono, se salta el login
    val startDestination = remember {
        sesionLeida.usuario?.let { inicioSegunRol(it).route } ?: Screen.Login.route
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

    // Cuando el inicio de sesión es correcto se va al inicio que corresponde al rol.
    // Se limpia el historial para que "atrás" no vuelva al login.
    LaunchedEffect(Unit) {
        authViewModel.inicioSesion.collect { usuario ->
            mainViewModel.navigateTo(inicioSegunRol(usuario), limpiarHistorial = true)
        }
    }

    // Cuando termina de guardarse un registro nuevo: vuelve al inicio y muestra el mensaje
    LaunchedEffect(Unit) {
        formularioViewModel.registroGuardado.collect {
            mainViewModel.navigateTo(Screen.Form, popUpToRoute = Screen.Form, inclusive = true)
            snackbarHostState.showSnackbar("Registro guardado")
        }
    }

    // Cuando termina de guardarse una edición: vuelve al detalle del registro (ya actualizado)
    LaunchedEffect(Unit) {
        edicionViewModel.registroGuardado.collect { resultado ->
            mainViewModel.navigateTo(Screen.RegistroDetalle, popUpToRoute = Screen.RegistroDetalle, inclusive = true)
            snackbarHostState.showSnackbar(
                if (resultado == ResultadoGuardado.EDITADO) "Cambios guardados" else "No se detectaron cambios",
            )
        }
    }

    // Para mostrar mensajes desde los botones (ej. "Registro archivado")
    val scope = rememberCoroutineScope()

    // Abre el detalle de un registro guardado (lo usan el administrador y el técnico)
    val verRegistro: (RegistroGuardado) -> Unit = { registro ->
        registrosViewModel.seleccionar(registro)
        mainViewModel.navigateTo(Screen.RegistroDetalle)
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
                onLoginClick = authViewModel::iniciarSesion,
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
                onRegistrosAnteriores = { mainViewModel.navigateTo(Screen.Registros) },
                onCerrarSesion = cerrarSesion,
            )
        }

        composable(Screen.GeneralForm.route) {
            val datosGenerales by formularioViewModel.datosGenerales.collectAsState()
            val clientes by formularioViewModel.clientes.collectAsState()
            val mostrarErrores by formularioViewModel.mostrarErrores.collectAsState()
            val distanciaMetros by formularioViewModel.distanciaMetros.collectAsState()
            val buscandoUbicacion by formularioViewModel.buscandoUbicacion.collectAsState()
            val errorGps by formularioViewModel.errorGps.collectAsState()
            val context = androidx.compose.ui.platform.LocalContext.current

            RegistroGeneralScreen(
                state = datosGenerales,
                clientes = clientes,
                mostrarErrores = mostrarErrores,
                distanciaMetros = distanciaMetros,
                buscandoUbicacion = buscandoUbicacion,
                errorGps = errorGps,
                onStateChange = formularioViewModel::actualizarDatosGenerales,
                onClienteChange = formularioViewModel::seleccionarCliente,
                onInstalacionChange = formularioViewModel::seleccionarInstalacion,
                onDetectarInstalacionGps = { formularioViewModel.detectarInstalacionAutomatica(context) },
                onActualizarUbicacionGps = { formularioViewModel.actualizarUbicacionGps(context) },
                onContinuarMantenimiento = {
                    if (formularioViewModel.validarDatosGenerales() && formularioViewModel.validarRangoGps()) {
                        mainViewModel.navigateTo(Screen.Maintenance)
                    }
                },
                onContinuarPruebas = {
                    if (formularioViewModel.validarDatosGenerales() && formularioViewModel.validarRangoGps()) {
                        mainViewModel.navigateTo(Screen.Test)
                    }
                },
                onBackCLick = mainViewModel::navigateBack
            )
        }

        composable(Screen.Maintenance.route) {
            FormularioMantenimiento(
                viewModel = formularioViewModel,
                onVerRegistroAnterior = verRegistro,
                onBackClick = mainViewModel::navigateBack,
            )
        }

        composable(Screen.Test.route) {
            FormularioPruebas(
                viewModel = formularioViewModel,
                onVerRegistroAnterior = verRegistro,
                onBackClick = mainViewModel::navigateBack,
            )
        }

        // Edición de un registro guardado (técnicos y administradores)
        composable(Screen.EditarGeneral.route) {
            FormularioGeneral(
                viewModel = edicionViewModel,
                onContinuarMantenimiento = { mainViewModel.navigateTo(Screen.EditarMantenimiento) },
                onContinuarPruebas = { mainViewModel.navigateTo(Screen.EditarPrueba) },
                onBackClick = mainViewModel::navigateBack,
            )
        }

        composable(Screen.EditarMantenimiento.route) {
            FormularioMantenimiento(
                viewModel = edicionViewModel,
                onVerRegistroAnterior = verRegistro,
                onBackClick = mainViewModel::navigateBack,
            )
        }

        composable(Screen.EditarPrueba.route) {
            FormularioPruebas(
                viewModel = edicionViewModel,
                onVerRegistroAnterior = verRegistro,
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
                onArchivadosClick = { mainViewModel.navigateTo(Screen.Archivados) },
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
            val context = androidx.compose.ui.platform.LocalContext.current

            ClienteFormScreen(
                state = form,
                onNombreChange = clientesViewModel::actualizarNombre,
                onNuevaInstalacionChange = clientesViewModel::actualizarNuevaInstalacion,
                onAgregarInstalacion = clientesViewModel::agregarInstalacion,
                onQuitarInstalacion = clientesViewModel::quitarInstalacion,
                onNuevoTipoEquipoChange = clientesViewModel::actualizarNuevoTipoEquipo,
                onAgregarTipoEquipo = clientesViewModel::agregarTipoEquipo,
                onQuitarTipoEquipo = clientesViewModel::quitarTipoEquipo,
                onCoordenadasChange = clientesViewModel::actualizarCoordenadasInstalacion,
                onCapturarGps = { instalacionNombre ->
                    clientesViewModel.capturarUbicacionActualInstalacion(context, instalacionNombre)
                },
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
                onRegistroClick = verRegistro,
                onBackClick = mainViewModel::navigateBack,
            )
        }

        // Solo los administradores llegan aquí (desde su panel)
        composable(Screen.Archivados.route) {
            val archivados by registrosViewModel.archivados.collectAsState()

            RegistrosScreen(
                registros = archivados,
                onRegistroClick = verRegistro,
                onBackClick = mainViewModel::navigateBack,
                titulo = "Archivados",
                textoVacio = "No hay registros archivados",
            )
        }

        composable(Screen.RegistroDetalle.route) {
            val registro by registrosViewModel.registroSeleccionado.collectAsState()
            val usuario = usuarioActual ?: return@composable
            val esAdmin = usuario.rol == RolUsuario.ADMINISTRADOR

            registro?.let { actual ->
                val archivado = actual.archivado != null
                RegistroDetalleScreen(
                    registro = actual,
                    // Cualquier usuario puede editar un registro activo; los archivados no se editan
                    puedeEditar = !archivado,
                    puedeArchivar = esAdmin && !archivado,
                    puedeRestaurar = esAdmin && archivado,
                    onEditarClick = {
                        edicionViewModel.iniciarEdicion(actual, usuario)
                        mainViewModel.navigateTo(Screen.EditarGeneral)
                    },
                    onArchivarClick = {
                        registrosViewModel.archivar(usuario)
                        mainViewModel.navigateBack()
                        scope.launch { snackbarHostState.showSnackbar("Registro archivado") }
                    },
                    onRestaurarClick = {
                        registrosViewModel.restaurar()
                        mainViewModel.navigateBack()
                        scope.launch { snackbarHostState.showSnackbar("Registro restaurado") }
                    },
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

// Las pantallas del registro se usan con dos ViewModels: el de registros nuevos y el de edición.
// Estas funciones conectan cada pantalla con el ViewModel que reciben.

@Composable
private fun FormularioGeneral(
    viewModel: FormularioViewModel,
    onContinuarMantenimiento: () -> Unit,
    onContinuarPruebas: () -> Unit,
    onBackClick: () -> Unit,
) {
    val datosGenerales by viewModel.datosGenerales.collectAsState()
    val clientes by viewModel.clientes.collectAsState()
    val mostrarErrores by viewModel.mostrarErrores.collectAsState()
    val registroEnEdicion by viewModel.registroEnEdicion.collectAsState()

    RegistroGeneralScreen(
        state = datosGenerales,
        clientes = clientes,
        mostrarErrores = mostrarErrores,
        onStateChange = viewModel::actualizarDatosGenerales,
        onClienteChange = viewModel::seleccionarCliente,
        onInstalacionChange = viewModel::seleccionarInstalacion,
        onContinuarMantenimiento = {
            if (viewModel.validarDatosGenerales()) onContinuarMantenimiento()
        },
        onContinuarPruebas = {
            if (viewModel.validarDatosGenerales()) onContinuarPruebas()
        },
        protocoloEnEdicion = registroEnEdicion?.tipo,
        onBackCLick = onBackClick
    )
}

@Composable
private fun FormularioMantenimiento(
    viewModel: FormularioViewModel,
    onVerRegistroAnterior: (RegistroGuardado) -> Unit,
    onBackClick: () -> Unit,
) {
    val mantenimientoState by viewModel.mantenimientoState.collectAsState()
    val guardando by viewModel.guardando.collectAsState()
    val registroAnterior by viewModel.mantenimientoAnterior.collectAsState()
    val procesandoFoto by viewModel.procesandoFoto.collectAsState()
    val registroEnEdicion by viewModel.registroEnEdicion.collectAsState()

    MantenimientoScreen(
        state = mantenimientoState,
        guardando = guardando,
        editando = registroEnEdicion != null,
        registroAnterior = registroAnterior,
        procesandoFoto = procesandoFoto,
        onStateChange = viewModel::actualizarMantenimiento,
        onVerRegistroAnterior = onVerRegistroAnterior,
        onPrepararCamara = viewModel::prepararFotoCamara,
        onResultadoCamara = viewModel::resultadoCamara,
        onFotoGaleria = viewModel::fotoDeGaleria,
        onQuitarFoto = viewModel::quitarFoto,
        onSaveClick = viewModel::guardarMantenimiento,
        onBackClick = onBackClick,
    )
}

@Composable
private fun FormularioPruebas(
    viewModel: FormularioViewModel,
    onVerRegistroAnterior: (RegistroGuardado) -> Unit,
    onBackClick: () -> Unit,
) {
    val pruebasState by viewModel.pruebasElectricasState.collectAsState()
    val guardando by viewModel.guardando.collectAsState()
    val registroAnterior by viewModel.pruebaAnterior.collectAsState()
    val registroEnEdicion by viewModel.registroEnEdicion.collectAsState()

    PruebasElectricasScreen(
        state = pruebasState,
        guardando = guardando,
        editando = registroEnEdicion != null,
        registroAnterior = registroAnterior,
        onStateChange = viewModel::actualizarPruebas,
        onVerRegistroAnterior = onVerRegistroAnterior,
        onSaveClick = viewModel::guardarPruebas,
        onBackClick = onBackClick,
    )
}

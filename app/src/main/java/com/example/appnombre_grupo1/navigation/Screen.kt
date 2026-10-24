package com.example.appnombre_grupo1.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login_page")
    data object Home : Screen("home_page")
    data object Profile : Screen("profile_page")
    data object Settings : Screen("settings_page")

    data object Form : Screen("forms_page")

    data object GeneralForm : Screen("general_form_page")

    data object Maintenance : Screen("maintenance_page")

    data object Test : Screen("test_page")

    // Pantallas del administrador
    data object AdminHome : Screen("admin_home_page")
    data object Clientes : Screen("clientes_page")
    data object ClienteForm : Screen("cliente_form_page")
    data object Usuarios : Screen("usuarios_page")
    data object UsuarioForm : Screen("usuario_form_page")
    data object Registros : Screen("registros_page")
    data object RegistroDetalle : Screen("registro_detalle_page")

    // Ejemplo para pantallas que reciban parámetros (opcional)
    data class Detail(val itemId: String) : Screen("detail_page/{itemId}") {
        fun buildRoute(): String = route.replace("{itemId}", itemId)
    }
}
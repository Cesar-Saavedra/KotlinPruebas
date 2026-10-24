package com.example.appnombre_grupo1.navigation
sealed class NavigationEvent {
    data class NavigateTo(
        val route: Screen,
        val popUpToRoute: Screen? = null,
        val inclusive: Boolean = false,
        val singleTop: Boolean = false,
        // Borra todo el historial (ej. al iniciar o cerrar sesión)
        val limpiarHistorial: Boolean = false,
    ) : NavigationEvent()

    data object PopBackStack : NavigationEvent()
    data object NavigateUp : NavigationEvent()
}
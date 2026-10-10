package com.example.appnombre_grupo1.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.appnombre_grupo1.ui.screens.forms.MantenimientoScreen
import com.example.appnombre_grupo1.ui.screens.forms.MenuFormulariosScreen
import com.example.appnombre_grupo1.ui.screens.forms.PruebasElectricasScreen

enum class EstadoNavegacionFormularios {
    MENU,
    MANTENIMIENTO,
    PRUEBAS
}

@Composable
fun FormulariosContainerScreen() {
    var pantallaActual by rememberSaveable{ mutableStateOf(EstadoNavegacionFormularios.MENU) }

    when (pantallaActual) {
        EstadoNavegacionFormularios.MENU -> MenuFormulariosScreen(
            onNavigateToMantenimiento = { pantallaActual = EstadoNavegacionFormularios.MANTENIMIENTO },
            onNavigateToPruebas = { pantallaActual = EstadoNavegacionFormularios.PRUEBAS }
        )

        EstadoNavegacionFormularios.MANTENIMIENTO -> MantenimientoScreen(
            onSaveClick = { _ ->
                // Aquí guardarás los datos cuando integres la base de datos local (Room)
                pantallaActual = EstadoNavegacionFormularios.MENU
            },
            onBackClick = { pantallaActual = EstadoNavegacionFormularios.MENU }
        )

        EstadoNavegacionFormularios.PRUEBAS -> PruebasElectricasScreen(
            onSaveClick = { _ ->
                // Aquí guardarás los datos cuando integres la base de datos local (Room)
                pantallaActual = EstadoNavegacionFormularios.MENU
            },
            onBackClick = { pantallaActual = EstadoNavegacionFormularios.MENU }
        )
    }
}

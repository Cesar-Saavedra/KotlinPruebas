package com.example.appnombre_grupo1.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.appnombre_grupo1.ui.utils.UbicacionUtils

// Devuelve una función que ejecuta una acción que necesita la ubicación: si ya hay permiso
// la ejecuta de inmediato; si no, lo pide y la ejecuta cuando el usuario lo concede.
@Composable
fun rememberAccionConPermisoUbicacion(): (accion: () -> Unit) -> Unit {
    val context = LocalContext.current
    var accionPendiente by remember { mutableStateOf<(() -> Unit)?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permisos ->
        // Basta con la ubicación precisa (FINE) o la aproximada (COARSE)
        if (permisos.values.any { it }) accionPendiente?.invoke()
        accionPendiente = null
    }

    return { accion ->
        if (UbicacionUtils.tienePermisoUbicacion(context)) {
            accion()
        } else {
            accionPendiente = accion
            launcher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            )
        }
    }
}

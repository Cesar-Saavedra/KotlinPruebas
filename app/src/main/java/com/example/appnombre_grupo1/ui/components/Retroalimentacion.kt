package com.example.appnombre_grupo1.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// Ventana con un círculo de carga que bloquea la pantalla mientras se guarda
@Composable
fun CargandoOverlay(mensaje: String) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Card(shape = MaterialTheme.shapes.large) {
            Column(
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(56.dp),
                    strokeWidth = 5.dp,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(mensaje, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// Mensaje emergente de éxito que aparece en la parte inferior de la pantalla
@Composable
fun MensajeExito(data: SnackbarData) {
    Snackbar(
        modifier = Modifier.padding(16.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(data.visuals.message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

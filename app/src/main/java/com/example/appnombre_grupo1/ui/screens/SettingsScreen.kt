package com.example.appnombre_grupo1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appnombre_grupo1.navigation.Screen
import com.example.appnombre_grupo1.viewmodels.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
) {
    var notificaciones by remember { mutableStateOf(value = true) }
    var modoOscuro by remember { mutableStateOf(value = false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text("Ajustes generales", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Switch de Notificaciones
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Notificaciones push", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Alertas sobre cambios y novedades",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Switch(
                checked = notificaciones,
                onCheckedChange = { notificaciones = it }
            )
        }

        HorizontalDivider()

        // Switch de Tema
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Tema oscuro", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Forzar interfaz con contraste alto",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Switch(
                checked = modoOscuro,
                onCheckedChange = { modoOscuro = it }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botón de cierre o regreso abajo
        Button(
            onClick = { viewModel.navigateTo(Screen.Home) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar y volver al inicio")
        }
    }
}

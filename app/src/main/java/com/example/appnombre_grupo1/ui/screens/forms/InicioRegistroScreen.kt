package com.example.appnombre_grupo1.ui.screens.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioRegistroScreen(
    onNuevoRegistro: () -> Unit,
) {
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
                onClick = onNuevoRegistro,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
            ) {
                Text("Nuevo Registro", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

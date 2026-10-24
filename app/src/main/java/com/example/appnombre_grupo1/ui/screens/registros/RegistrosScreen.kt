package com.example.appnombre_grupo1.ui.screens.registros

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appnombre_grupo1.model.RegistroGuardado
import com.example.appnombre_grupo1.model.TipoRegistro
import com.example.appnombre_grupo1.ui.utils.formatearFecha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrosScreen(
    registros: List<RegistroGuardado>,
    onRegistroClick: (RegistroGuardado) -> Unit,
    onBackClick: () -> Unit,
) {
    // Filtro por tipo de protocolo; null muestra todos
    var filtro by remember { mutableStateOf<TipoRegistro?>(null) }
    val visibles = registros.filter { (filtro == null) || (it.tipo == filtro) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registros anteriores") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filtro == null,
                    onClick = { filtro = null },
                    label = { Text("Todos") },
                )
                TipoRegistro.entries.forEach { tipo ->
                    FilterChip(
                        selected = filtro == tipo,
                        onClick = { filtro = tipo },
                        label = { Text(tipo.label) },
                    )
                }
            }

            if (visibles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No hay registros guardados")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(visibles, key = { it.id }) { registro ->
                        val datos = registro.datosGenerales
                        OutlinedCard(onClick = { onRegistroClick(registro) }, modifier = Modifier.fillMaxWidth()) {
                            ListItem(
                                headlineContent = { Text("${datos.cliente} · ${datos.instalacion}") },
                                supportingContent = {
                                    Text(
                                        "${registro.tipo.label} · ${datos.tipoEquipo} ${datos.codigoEquipo}\n" +
                                            "${datos.tecnico} · ${formatearFecha(registro.fechaMillis)}",
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        imageVector = if (registro.tipo == TipoRegistro.MANTENIMIENTO) {
                                            Icons.Default.Build
                                        } else {
                                            Icons.Default.ElectricBolt
                                        },
                                        contentDescription = registro.tipo.label,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

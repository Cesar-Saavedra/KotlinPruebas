package com.example.appnombre_grupo1.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appnombre_grupo1.model.RegistroGuardado
import com.example.appnombre_grupo1.ui.utils.formatearFecha

// Avisa si el equipo ya tiene un registro anterior y permite abrirlo para compararlo
@Composable
fun TarjetaRegistroAnterior(
    registro: RegistroGuardado?,
    onVerClick: (RegistroGuardado) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.History, contentDescription = null)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                if (registro == null) {
                    Text("Sin registros anteriores", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Es el primer registro de este equipo",
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    Text("Registro anterior de este equipo", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${formatearFecha(registro.fechaMillis)} · ${registro.datosGenerales.tecnico}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            if (registro != null) {
                TextButton(onClick = { onVerClick(registro) }) { Text("Ver") }
            }
        }
    }
}

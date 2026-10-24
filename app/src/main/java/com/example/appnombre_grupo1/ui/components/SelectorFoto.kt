package com.example.appnombre_grupo1.ui.components

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

// Muestra la foto elegida y permite tomarla con la cámara o elegirla de la galería
@Composable
fun SelectorFoto(
    fotoPath: String?,
    procesando: Boolean,
    onPrepararCamara: () -> Uri,
    onResultadoCamara: (Boolean) -> Unit,
    onFotoGaleria: (Uri) -> Unit,
    onQuitarFoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var sinCamara by remember { mutableStateOf(value = false) }
    val forma = RoundedCornerShape(12.dp)

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { tomada ->
        onResultadoCamara(tomada)
    }
    // El selector de fotos del sistema no necesita pedir permisos
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(onFotoGaleria)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when {
            procesando -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            fotoPath != null -> Box {
                FotoArchivo(
                    path = fotoPath,
                    contentDescription = "Foto del mantenimiento",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(forma),
                )
                FilledTonalIconButton(
                    onClick = onQuitarFoto,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Quitar foto")
                }
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, forma),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                Text(
                    "Sin foto (opcional)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    try {
                        camara.launch(onPrepararCamara())
                    } catch (e: ActivityNotFoundException) {
                        onResultadoCamara(false)
                        sinCamara = true
                    }
                },
                enabled = !procesando,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(if (fotoPath == null) "Tomar foto" else "Otra foto")
            }
            OutlinedButton(
                onClick = {
                    galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                enabled = !procesando,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Galería")
            }
        }

        if (sinCamara) {
            Text(
                "No se encontró una app de cámara en este teléfono",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

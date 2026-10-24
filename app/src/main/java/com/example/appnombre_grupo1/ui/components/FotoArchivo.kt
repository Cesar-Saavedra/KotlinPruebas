package com.example.appnombre_grupo1.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// Resultado de cargar la imagen: cargando, cargada o no disponible
private sealed interface EstadoImagen {
    data object Cargando : EstadoImagen
    data class Lista(val imagen: ImageBitmap) : EstadoImagen
    data object NoDisponible : EstadoImagen
}

// Carga la foto reducida (para no usar demasiada memoria) y con la orientación correcta
private fun cargarImagen(path: String, ladoMaximo: Int = 1280): ImageBitmap? {
    if (!File(path).exists()) return null

    val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, medidas)
    var escala = 1
    while (maxOf(medidas.outWidth, medidas.outHeight) / (escala * 2) >= ladoMaximo) escala *= 2

    val bitmap = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = escala })
        ?: return null

    // Las cámaras guardan la rotación en los datos EXIF en vez de rotar los píxeles
    val rotacion = when (
        ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    ) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
    val final = if (rotacion == 0f) {
        bitmap
    } else {
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(rotacion) }, true)
    }
    return final.asImageBitmap()
}

// Muestra una foto guardada en el almacenamiento interno a partir de su ruta
@Composable
fun FotoArchivo(
    path: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val estado by produceState<EstadoImagen>(EstadoImagen.Cargando, path) {
        value = withContext(Dispatchers.IO) { cargarImagen(path) }
            ?.let { EstadoImagen.Lista(it) }
            ?: EstadoImagen.NoDisponible
    }

    when (val actual = estado) {
        is EstadoImagen.Lista -> Image(
            bitmap = actual.imagen,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
        EstadoImagen.Cargando -> Box(modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        EstadoImagen.NoDisponible -> Box(modifier, contentAlignment = Alignment.Center) {
            Text("Foto no disponible")
        }
    }
}

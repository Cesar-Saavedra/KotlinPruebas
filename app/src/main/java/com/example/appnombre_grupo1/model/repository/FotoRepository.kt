package com.example.appnombre_grupo1.model.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

// Maneja los archivos de las fotos en el almacenamiento interno de la app
class FotoRepository(private val context: Context) {

    private val carpeta: File
        get() = File(context.filesDir, CARPETA_FOTOS).apply { mkdirs() }

    fun nuevoArchivo(): File = File(carpeta, "foto_${UUID.randomUUID()}.jpg")

    // Uri que se entrega a la app de cámara para que escriba la foto en el archivo
    fun uriParaCamara(archivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

    // Copia una imagen de la galería a la carpeta de la app; devuelve la ruta o null si falla
    suspend fun copiarDesdeGaleria(uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val archivo = nuevoArchivo()
            context.contentResolver.openInputStream(uri)!!.use { entrada ->
                archivo.outputStream().use { salida -> entrada.copyTo(salida) }
            }
            archivo.absolutePath
        }.getOrNull()
    }

    fun eliminar(path: String) {
        File(path).delete()
    }

    private companion object {
        const val CARPETA_FOTOS = "fotos"
    }
}

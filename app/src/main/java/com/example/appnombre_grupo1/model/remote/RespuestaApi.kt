package com.example.appnombre_grupo1.model.remote

import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException

sealed interface RespuestaApi<out T> {
    data class Exito<T>(val datos: T) : RespuestaApi<T>
    data class Error(val codigo: Int, val mensaje: String) : RespuestaApi<Nothing>
    data object SinConexion : RespuestaApi<Nothing>
}

const val MENSAJE_SIN_CONEXION = "No se pudo conectar con el servidor. Revise su conexión e intente de nuevo."

// Ejecuta la llamada como en la guía (try/catch + isSuccessful) y la convierte en un resultado
@Suppress("UNCHECKED_CAST")
suspend fun <T> llamarApi(llamada: suspend () -> Response<T>): RespuestaApi<T> =
    try {
        val respuesta = llamada()
        if (respuesta.isSuccessful) {
            // Las respuestas 204 (DELETE) no traen cuerpo
            RespuestaApi.Exito((respuesta.body() ?: Unit) as T)
        } else {
            RespuestaApi.Error(respuesta.code(), mensajeDeError(respuesta))
        }
    } catch (e: IOException) {
        RespuestaApi.SinConexion
    }

// El backend responde los errores como {"status": 409, "message": "..."}
private fun mensajeDeError(respuesta: Response<*>): String {
    val texto = respuesta.errorBody()?.string().orEmpty()
    val mensaje = runCatching { Gson().fromJson(texto, ErrorDto::class.java)?.message }.getOrNull()
    return mensaje ?: "Error del servidor (${respuesta.code()})"
}

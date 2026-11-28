package com.example.appnombre_grupo1.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton que contiene la configuración de Retrofit para localhost (emulador Android)
object RetrofitInstance {

    // Se instancia el servicio de la API una sola vez
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.16.48.105:8080/") // URL base de la API en localhost
            .addConverterFactory(GsonConverterFactory.create()) // Conversor JSON
            .build()
            .create(ApiService::class.java)
    }
}

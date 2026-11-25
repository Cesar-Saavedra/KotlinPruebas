package com.example.appnombre_grupo1.model.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Backend en Render. Para probar con el backend local en el emulador: "http://10.0.2.2:8080/"
    // (requiere android:usesCleartextTraffic="true" en el Manifest)
    private const val BASE_URL = "https://backend-induztek.onrender.com/"

    // El plan gratis de Render tarda hasta ~1 minuto en despertar
    private val cliente = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

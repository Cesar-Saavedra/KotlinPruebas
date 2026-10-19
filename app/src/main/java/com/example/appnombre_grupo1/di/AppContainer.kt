package com.example.appnombre_grupo1.di

import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.appnombre_grupo1.InduztekApp
import com.example.appnombre_grupo1.model.repository.ClienteRepository
import com.example.appnombre_grupo1.model.repository.FotoRepository
import com.example.appnombre_grupo1.model.repository.RegistroRepository
import com.example.appnombre_grupo1.model.repository.UsuarioRepository
import com.google.gson.Gson

// Crea y comparte los repositorios, así todos los ViewModels ven los mismos datos
class AppContainer(context: Context) {
    private val gson = Gson()

    val usuarioRepository = UsuarioRepository(context, gson)
    val clienteRepository = ClienteRepository(context, gson)
    val registroRepository = RegistroRepository(context, gson)
    val fotoRepository = FotoRepository(context)
}

val AndroidViewModel.container: AppContainer
    get() = getApplication<InduztekApp>().container

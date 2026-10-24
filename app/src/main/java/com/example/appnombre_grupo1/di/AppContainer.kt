package com.example.appnombre_grupo1.di

import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.appnombre_grupo1.InduztekApp
import com.example.appnombre_grupo1.model.database.DatosIniciales
import com.example.appnombre_grupo1.model.database.InduztekDatabase
import com.example.appnombre_grupo1.model.repository.ClienteRepository
import com.example.appnombre_grupo1.model.repository.FotoRepository
import com.example.appnombre_grupo1.model.repository.RegistroRepository
import com.example.appnombre_grupo1.model.repository.UsuarioRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// Crea y comparte la base de datos y los repositorios, así todos los ViewModels ven los mismos datos
class AppContainer(context: Context) {

    // Corrutinas que deben terminar aunque se cierre una pantalla (ej. cargar datos iniciales)
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Una sola instancia de la base de datos para toda la app
    val database: InduztekDatabase by lazy {
        Room.databaseBuilder(context, InduztekDatabase::class.java, InduztekDatabase.NOMBRE)
            .addCallback(
                object : RoomDatabase.Callback() {
                    // Solo se ejecuta la primera vez, cuando se crea el archivo de la base de datos
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        appScope.launch { DatosIniciales(context, Gson()).cargar(database) }
                    }
                },
            )
            .build()
    }

    val usuarioRepository by lazy { UsuarioRepository(context, database.usuarioDao()) }
    val clienteRepository by lazy { ClienteRepository(database.clienteDao()) }
    val registroRepository by lazy { RegistroRepository(database.registroDao()) }
    val fotoRepository = FotoRepository(context)
}

val AndroidViewModel.container: AppContainer
    get() = getApplication<InduztekApp>().container

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
import com.example.appnombre_grupo1.model.repository.SincronizacionRepository
import com.example.appnombre_grupo1.model.remote.ApiService
import com.example.appnombre_grupo1.model.remote.RetrofitClient
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
            // Actualiza la base de datos de los teléfonos que tenían una versión anterior,
            // paso a paso (1 → 2 → 3), sin borrar los datos guardados
            .addMigrations(InduztekDatabase.MIGRACION_1_2, InduztekDatabase.MIGRACION_2_3, InduztekDatabase.MIGRACION_3_4)
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

    private val api: ApiService = RetrofitClient.instance

    val usuarioRepository by lazy { UsuarioRepository(context, database.usuarioDao(), api) }
    val clienteRepository by lazy { ClienteRepository(database.clienteDao(), api) }
    val registroRepository by lazy { RegistroRepository(database.registroDao(), api) }
    val sincronizacionRepository by lazy {
        SincronizacionRepository(usuarioRepository, clienteRepository, registroRepository)
    }
    val fotoRepository = FotoRepository(context)
}

val AndroidViewModel.container: AppContainer
    get() = getApplication<InduztekApp>().container

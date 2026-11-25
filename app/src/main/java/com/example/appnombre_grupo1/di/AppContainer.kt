package com.example.appnombre_grupo1.di

import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.appnombre_grupo1.InduztekApp
import com.example.appnombre_grupo1.data.model.database.DatosIniciales
import com.example.appnombre_grupo1.data.model.database.InduztekDatabase
import com.example.appnombre_grupo1.data.model.repository.ClienteRepository
import com.example.appnombre_grupo1.data.model.repository.FotoRepository
import com.example.appnombre_grupo1.data.model.repository.RegistroRepository
import com.example.appnombre_grupo1.data.model.repository.UsuarioRepository
import com.example.appnombre_grupo1.data.remote.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.google.gson.Gson

class AppContainer(private val context: Context) {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val apiService by lazy { RetrofitInstance.api }

    val database: InduztekDatabase by lazy {
        Room.databaseBuilder(context, InduztekDatabase::class.java, InduztekDatabase.NOMBRE)
            .addMigrations(InduztekDatabase.MIGRACION_1_2, InduztekDatabase.MIGRACION_2_3,
                InduztekDatabase.MIGRACION_3_4, InduztekDatabase.MIGRACION_4_5)
            .addCallback(
                object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        appScope.launch { DatosIniciales(context, Gson()).cargar(database) }
                    }
                },
            )
            .build()
    }

    val usuarioRepository by lazy { UsuarioRepository(context, database.usuarioDao(), apiService) }
    val clienteRepository by lazy { ClienteRepository(database.clienteDao(), apiService) }
    val registroRepository by lazy { RegistroRepository(database.registroDao(), apiService) }
    val fotoRepository = FotoRepository(context)
}


val AndroidViewModel.container: AppContainer
    get() = getApplication<InduztekApp>().container

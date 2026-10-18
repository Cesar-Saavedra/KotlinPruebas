package com.example.appnombre_grupo1

import android.app.Application
import com.example.appnombre_grupo1.di.AppContainer

class InduztekApp : Application() {
    // Una sola instancia de cada repositorio para toda la app
    val container: AppContainer by lazy { AppContainer(this) }
}

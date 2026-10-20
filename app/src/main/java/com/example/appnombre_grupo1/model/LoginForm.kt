package com.example.appnombre_grupo1.model

data class LoginForm(
    val username: String = "",
    val password: String = "",
    val error: String? = null,
    // true mientras se consultan las credenciales en la base de datos
    val verificando: Boolean = false,
)

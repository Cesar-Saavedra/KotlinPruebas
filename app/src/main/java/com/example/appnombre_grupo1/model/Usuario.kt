package com.example.appnombre_grupo1.model

enum class RolUsuario(val label: String) {
    ADMINISTRADOR("Administrador"),
    TECNICO("Técnico")
}

data class Usuario(
    val username: String,
    val nombre: String,
    val rol: RolUsuario,
)

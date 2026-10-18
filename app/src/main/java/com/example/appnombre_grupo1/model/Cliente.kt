package com.example.appnombre_grupo1.model

data class Cliente(
    val id: String = "",
    val nombre: String = "",
    val instalaciones: List<String> = emptyList(),
    val tiposEquipo: List<String> = emptyList(),
)

// Estado del formulario para crear o editar un cliente
data class ClienteForm(
    val id: String? = null,
    val nombre: String = "",
    val instalaciones: List<String> = emptyList(),
    val tiposEquipo: List<String> = emptyList(),
    val nuevaInstalacion: String = "",
    val nuevoTipoEquipo: String = "",
    val error: String? = null,
)

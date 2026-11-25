package com.example.appnombre_grupo1.data.model

data class TipoEquipo(
    val id: String = "",
    val nombre: String = "",
)

// Cada instalación tiene sus propios tipos de equipo
data class Instalacion(
    val id: String = "",
    val nombre: String = "",
    val tiposEquipo: List<TipoEquipo> = emptyList(),
    val latitud: Double? = null,
    val longitud: Double? = null,
)

data class Cliente(
    val id: String = "",
    val nombre: String = "",
    val instalaciones: List<Instalacion> = emptyList(),
)

// Estado del formulario para crear o editar un cliente
data class ClienteForm(
    val id: String? = null,
    val nombre: String = "",
    val instalaciones: List<Instalacion> = emptyList(),
    val nuevaInstalacion: String = "",
    // Texto que se está escribiendo para agregar un equipo, por nombre de instalación
    val nuevoTipoEquipo: Map<String, String> = emptyMap(),
    val error: String? = null,
)

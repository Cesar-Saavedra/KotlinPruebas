package com.example.appnombre_grupo1.data.model

import com.google.gson.annotations.SerializedName

enum class RolUsuario(val label: String) {
    ADMINISTRADOR("Administrador"),
    TECNICO("Técnico")
}

data class Usuario(
    val id: String = "",
    val username: String = "",
    @SerializedName("name")
    val nombre: String = "",
    @SerializedName("role")
    val rol: RolUsuario = RolUsuario.TECNICO,
)

// Estado del formulario para crear o editar un usuario
data class UsuarioForm(
    val esNuevo: Boolean = true,
    val username: String = "",
    val nombre: String = "",
    val rol: RolUsuario = RolUsuario.TECNICO,
    // Al editar, si se deja vacía se mantiene la contraseña actual
    val password: String = "",
    // true cuando el administrador se edita a sí mismo (no puede cambiar su rol)
    val esUsuarioActual: Boolean = false,
    val error: String? = null,
)

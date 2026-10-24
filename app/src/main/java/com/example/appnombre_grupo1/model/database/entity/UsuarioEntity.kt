package com.example.appnombre_grupo1.model.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.appnombre_grupo1.model.RolUsuario

// Tabla "usuarios": el nombre de usuario es la clave primaria
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val username: String,
    val nombre: String,
    // Room guarda los enum como texto (ej. "ADMINISTRADOR")
    val rol: RolUsuario,
    val passwordHash: String,
)

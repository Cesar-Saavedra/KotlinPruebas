package com.example.appnombre_grupo1.model.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.appnombre_grupo1.model.database.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UsuarioDao {

    // Flow: emite de nuevo la lista cada vez que cambia la tabla
    @Query("SELECT * FROM usuarios ORDER BY nombre COLLATE NOCASE")
    abstract fun observarTodos(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE username = :username LIMIT 1")
    abstract suspend fun buscar(username: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios")
    abstract suspend fun obtenerTodos(): List<UsuarioEntity>

    // Inserta si no existe o actualiza si ya existe
    @Upsert
    abstract suspend fun guardar(usuario: UsuarioEntity)

    @Insert
    abstract suspend fun insertarTodos(usuarios: List<UsuarioEntity>)

    @Query("DELETE FROM usuarios WHERE username = :username")
    abstract suspend fun eliminar(username: String)

    @Query("DELETE FROM usuarios")
    abstract suspend fun eliminarTodos()

    // Deja la tabla igual a la lista del servidor, conservando los hash locales
    // (el servidor no los envía) para poder iniciar sesión sin conexión
    @Transaction
    open suspend fun reemplazarTodos(remotos: List<UsuarioEntity>) {
        val hashes = obtenerTodos().associate { it.username to it.passwordHash }
        eliminarTodos()
        insertarTodos(remotos.map { it.copy(passwordHash = hashes[it.username] ?: it.passwordHash) })
    }
}

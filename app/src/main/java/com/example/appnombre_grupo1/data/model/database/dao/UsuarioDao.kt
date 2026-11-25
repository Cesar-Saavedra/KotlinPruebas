package com.example.appnombre_grupo1.data.model.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.appnombre_grupo1.data.model.database.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios ORDER BY nombre COLLATE NOCASE")
    fun observarTodos(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE username = :username LIMIT 1")
    suspend fun buscar(username: String): UsuarioEntity?

    @Upsert
    suspend fun guardar(usuario: UsuarioEntity)

    @Query("DELETE FROM usuarios WHERE username = :username")
    suspend fun eliminar(username: String)
}

package com.example.appnombre_grupo1.data.remote

import com.example.appnombre_grupo1.data.model.Usuario
import com.example.appnombre_grupo1.data.remote.dto.ClienteDto
import com.example.appnombre_grupo1.data.remote.dto.CrearRegistroTerrenoRequestDto
import com.example.appnombre_grupo1.data.remote.dto.EdicionRegistroDto
import com.example.appnombre_grupo1.data.remote.dto.EstadoArchivoRegistroDto
import com.example.appnombre_grupo1.data.remote.dto.LoginRequestDto
import com.example.appnombre_grupo1.data.remote.dto.LoginResponseDto
import com.example.appnombre_grupo1.data.remote.dto.RegistroTerrenoResponseDto
import com.example.appnombre_grupo1.data.remote.dto.UsuarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // === Autenticación y Usuarios ===

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    @GET("/api/usuarios")
    suspend fun getUsuarios(): Response<List<Usuario>>

    @POST("/api/usuarios")
    suspend fun crearUsuario(@Body usuarioDto: UsuarioDto): Response<Usuario>

    @PUT("/api/usuarios/{id}")
    suspend fun actualizarUsuario(
        @Path("id") id: String,
        @Header("X-Current-User") currentUser: String = "admin",
        @Body usuarioDto: UsuarioDto,
    ): Response<Usuario>

    @DELETE("/api/usuarios/{id}")
    suspend fun eliminarUsuario(
        @Path("id") id: String,
        @Header("X-Current-User") currentUser: String = "admin"
    ): Response<Unit>

    // === Clientes ===

    @GET("/api/clientes")
    suspend fun getClientes(): Response<List<ClienteDto>>

    @POST("/api/clientes")
    suspend fun guardarCliente(@Body cliente: ClienteDto): Response<ClienteDto>

    @DELETE("/api/clientes/{id}")
    suspend fun eliminarCliente(@Path("id") id: String): Response<Unit>

    // === Registros de Terreno (Mantenimiento + Pruebas Eléctricas) ===

    @GET("/api/registros")
    suspend fun getRegistros(
        @Query("archived") archived: Boolean = false,
        @Query("equipmentTypeId") equipmentTypeId: String? = null
    ): Response<List<RegistroTerrenoResponseDto>>

    @POST("/api/registros")
    suspend fun guardarRegistro(
        @Body registro: CrearRegistroTerrenoRequestDto
    ): Response<RegistroTerrenoResponseDto>

    @PUT("/api/registros/{id}")
    suspend fun actualizarRegistro(
        @Path("id") id: String,
        @Body edicion: EdicionRegistroDto,
    ): Response<RegistroTerrenoResponseDto>

    // === Archivado y Restauración ===

    @PUT("/api/registros/archive/{id}")
    suspend fun archivarRegistro(@Path("id") id: String): Response<EstadoArchivoRegistroDto>

    @PUT("/api/registros/restore/{id}")
    suspend fun restaurarRegistro(@Path("id") id: String): Response<EstadoArchivoRegistroDto>
}

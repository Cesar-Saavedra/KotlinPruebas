package com.example.appnombre_grupo1.model.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/usuarios/login")
    suspend fun login(@Body body: LoginRequestDto): Response<UsuarioDto>

    @GET("api/usuarios")
    suspend fun obtenerUsuarios(): Response<List<UsuarioDto>>

    @POST("api/usuarios")
    suspend fun crearUsuario(@Body body: UsuarioRequestDto): Response<UsuarioDto>

    @PUT("api/usuarios/{username}")
    suspend fun editarUsuario(@Path("username") username: String, @Body body: UsuarioRequestDto): Response<UsuarioDto>

    @DELETE("api/usuarios/{username}")
    suspend fun eliminarUsuario(@Path("username") username: String): Response<Unit>

    @GET("api/clientes")
    suspend fun obtenerClientes(): Response<List<ClienteDto>>

    @POST("api/clientes")
    suspend fun crearCliente(@Body body: ClienteDto): Response<ClienteDto>

    @PUT("api/clientes/{id}")
    suspend fun editarCliente(@Path("id") id: String, @Body body: ClienteDto): Response<ClienteDto>

    @DELETE("api/clientes/{id}")
    suspend fun eliminarCliente(@Path("id") id: String): Response<Unit>

    @GET("api/registros")
    suspend fun obtenerRegistros(@Query("archivados") archivados: Boolean): Response<List<RegistroDto>>

    @GET("api/registros/{id}")
    suspend fun obtenerRegistro(@Path("id") id: String): Response<RegistroDto>

    @POST("api/registros")
    suspend fun crearRegistro(@Body body: RegistroDto): Response<RegistroDto>

    @PUT("api/registros/{id}")
    suspend fun editarRegistro(
        @Path("id") id: String,
        @Query("editor") editor: String,
        @Body body: RegistroDto,
    ): Response<RegistroDto>

    @POST("api/registros/{id}/archivar")
    suspend fun archivarRegistro(@Path("id") id: String, @Query("admin") admin: String): Response<RegistroDto>

    @POST("api/registros/{id}/restaurar")
    suspend fun restaurarRegistro(@Path("id") id: String, @Query("admin") admin: String): Response<RegistroDto>
}

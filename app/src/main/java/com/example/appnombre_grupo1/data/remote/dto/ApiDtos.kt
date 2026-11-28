package com.example.appnombre_grupo1.data.remote.dto

import com.example.appnombre_grupo1.data.model.MantenimientoState
import com.example.appnombre_grupo1.data.model.PruebasElectricasState
import com.example.appnombre_grupo1.data.model.RolUsuario
import com.google.gson.annotations.SerializedName

// === Autenticación y Usuarios ===

data class LoginRequestDto(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String,
)

// Coincide con AuthResponseDTO(token, id, username, role) de UsuarioService
data class LoginResponseDto(
    @SerializedName("token")
    val token: String? = null,
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("role")
    val rol: RolUsuario? = null,
)

// Coincide con UsuarioRequestDTO / UsuarioResponseDTO (username, password, role)
data class UsuarioDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("username")
    val username: String,
    @SerializedName("name")
    val nombre: String = "",
    @SerializedName("role")
    val rol: RolUsuario,
    @SerializedName("password")
    val password: String? = null,
)

// === Clientes, Instalaciones y Equipos ===

data class TipoEquipoDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("facilityId")
    val facilityId: String? = null,
    @SerializedName("name")
    val nombre: String,
)

data class InstalacionDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("clientId")
    val clientId: String? = null,
    @SerializedName("name")
    val nombre: String,
    @SerializedName("latitude")
    val latitud: Double? = null,
    @SerializedName("longitude")
    val longitud: Double? = null,
    @SerializedName("equipmentTypes")
    val tiposEquipo: List<TipoEquipoDto> = emptyList(),
)

data class ClienteDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val nombre: String,
    @SerializedName("facilities")
    val instalaciones: List<InstalacionDto> = emptyList(),
)

// === Registros de Terreno, Edición y Archivado ===

data class DetalleMantenimientoDto(
    @SerializedName("componentName")
    val nombreComponente: String,
    @SerializedName("status")
    val estado: String,
    @SerializedName("observations")
    val observaciones: String? = null,
)

data class PruebaElectricaDto(
    @SerializedName("insulationA")
    val aislacionA: Double? = null,
    @SerializedName("insulationV")
    val aislacionV: Double? = null,
    @SerializedName("contact")
    val contacto: Double? = null,
    @SerializedName("voltage")
    val voltaje: Double? = null,
    @SerializedName("current")
    val corriente: Double? = null,
    @SerializedName("verdict")
    val veredicto: String? = null,
    @SerializedName("technicalNotes")
    val notasTecnicas: String? = null,
)

// Coincide con RegistroTerrenoCreateDTO en el backend
data class CrearRegistroTerrenoRequestDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("clientId")
    val clientId: String,
    @SerializedName("facilityId")
    val facilityId: String,
    @SerializedName("equipmentTypeId")
    val equipmentTypeId: String,
    @SerializedName("codEquipo")
    val codEquipo: String? = null,
    @SerializedName("technicianId")
    val technicianId: String,
    @SerializedName("technicianUsername")
    val technicianUsername: String,
    @SerializedName("userLatitude")
    val latitudUsuario: Double,
    @SerializedName("userLongitude")
    val longitudUsuario: Double,
    @SerializedName("photoUrlOrBase64")
    val fotoUrlOrBase64: String? = null,
    @SerializedName("maintenanceItems")
    val itemsMantenimiento: List<DetalleMantenimientoDto> = emptyList(),
    @SerializedName("electricalTest")
    val pruebaElectrica: PruebaElectricaDto? = null,
)

// Coincide con RegistroTerrenoUpdateDTO (photoUrlOrBase64, maintenanceItems, electricalTest, editorUsername)
data class EdicionRegistroDto(
    @SerializedName("editorUsername")
    val editorUsername: String,
    @SerializedName("photoUrlOrBase64")
    val fotoUrlOrBase64: String? = null,
    @SerializedName("maintenanceItems")
    val itemsMantenimiento: List<DetalleMantenimientoDto> = emptyList(),
    @SerializedName("electricalTest")
    val pruebaElectrica: PruebaElectricaDto? = null,
    // Mantén estos si aún los usas internamente antes de convertir a los campos anteriores:
    val editorNombre: String? = null,
    val mantenimiento: MantenimientoState? = null,
    val pruebas: PruebasElectricasState? = null,
)

// Coincide con EstadoArchivoRegistroDTO devuelto por /archive/{id} y /restore/{id}
data class EstadoArchivoRegistroDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("archived")
    val archivado: Boolean,
    @SerializedName("action")
    val accion: String? = null,
    @SerializedName("updatedAt")
    val fechaHora: String? = null,
    @SerializedName("adminName")
    val nombreAdmin: String? = null,
)

data class DetalleMantenimientoResponseDto(
    @SerializedName("componentName")
    val componentName: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("observations")
    val observations: String? = null,
)

data class PruebaElectricaResponseDto(
    @SerializedName("insulationA")
    val insulationA: Double? = null,
    @SerializedName("insulationV")
    val insulationV: Double? = null,
    @SerializedName("contact")
    val contact: Double? = null,
    @SerializedName("voltage")
    val voltage: Double? = null,
    @SerializedName("current")
    val current: Double? = null,
    @SerializedName("verdict")
    val verdict: String? = null,
    @SerializedName("technicalNotes")
    val technicalNotes: String? = null,
)

data class RegistroTerrenoResponseDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("clientId")
    val clientId: String? = null,
    @SerializedName("clientName")
    val clientName: String? = null,
    @SerializedName("facilityId")
    val facilityId: String? = null,
    @SerializedName("facilityName")
    val facilityName: String? = null,
    @SerializedName("equipmentTypeId")
    val equipmentTypeId: String? = null,
    @SerializedName("equipmentTypeName")
    val equipmentTypeName: String? = null,
    @SerializedName("technicianId")
    val technicianId: String? = null,
    @SerializedName("technicianUsername")
    val technicianUsername: String? = null,
    @SerializedName("codEquipo")
    val codEquipo: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("photoUrl")
    val photoUrl: String? = null,
    @SerializedName("archived")
    val archived: Boolean = false,
    @SerializedName("maintenanceItems")
    val maintenanceItems: List<DetalleMantenimientoResponseDto> = emptyList(),
    @SerializedName("electricalTest")
    val electricalTest: PruebaElectricaResponseDto? = null,
)


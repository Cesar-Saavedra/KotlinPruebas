package com.example.appnombre_grupo1.data.remote

import com.example.appnombre_grupo1.data.model.ArchivoRegistro
import com.example.appnombre_grupo1.data.model.DatosGeneralesForm
import com.example.appnombre_grupo1.data.model.EstadoComponente
import com.example.appnombre_grupo1.data.model.MantenimientoState
import com.example.appnombre_grupo1.data.model.PruebasElectricasState
import com.example.appnombre_grupo1.data.model.RegistroGuardado
import com.example.appnombre_grupo1.data.model.TipoRegistro
import com.example.appnombre_grupo1.data.model.VeredictoPrueba
import com.example.appnombre_grupo1.data.remote.dto.RegistroTerrenoResponseDto
import com.example.appnombre_grupo1.data.model.Usuario
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

fun RegistroTerrenoResponseDto.aRegistroGuardado(): RegistroGuardado {
    val fechaMillis = parseFechaToMillis(createdAt)
    val tienePruebaElectrica = electricalTest != null && maintenanceItems.isEmpty()
    val tipoRegistro = if (tienePruebaElectrica) {
        TipoRegistro.PRUEBAS_ELECTRICAS
    } else {
        TipoRegistro.MANTENIMIENTO
    }

    val tecnicoUsernameResuelto = technicianUsername
        ?.takeIf { it.isNotBlank() }
        ?: technicianId.orEmpty()

    val tecnicoNombreResuelto = resolverNombreTecnico(
        username = technicianUsername,
        technicianId = technicianId,
    )

    val datosGenerales = DatosGeneralesForm(
        cliente = clientName?.takeIf { it.isNotBlank() } ?: clientId.orEmpty(),
        instalacion = facilityName?.takeIf { it.isNotBlank() } ?: facilityId.orEmpty(),
        tipoEquipo = equipmentTypeName?.takeIf { it.isNotBlank() } ?: equipmentTypeId.orEmpty(),
        codigoEquipo = codEquipo.orEmpty(),
        tecnico = tecnicoNombreResuelto,
    )


    val mantenimientoState = if (tipoRegistro == TipoRegistro.MANTENIMIENTO) {
        val itemsParseados = maintenanceItems.map { dtoItem ->
            val parseada = separarObservacion(dtoItem.observations)
            dtoItem to parseada
        }

        val itemsPorNombre = itemsParseados.associateBy { (dtoItem, _) ->
            dtoItem.componentName?.trim()?.lowercase()
        }

        fun estadoDe(vararg claves: String): EstadoComponente {
            val match = claves.firstNotNullOfOrNull { clave ->
                itemsPorNombre[clave.trim().lowercase()]?.first
            }
            return parseEstadoComponente(match?.status)
        }

        fun obsDe(vararg claves: String): String {
            return claves.firstNotNullOfOrNull { clave ->
                itemsPorNombre[clave.trim().lowercase()]?.second?.obsComponente?.takeIf { it.isNotBlank() }
            }.orEmpty()
        }

        val observacionesGeneralesRecuperadas = itemsParseados
            .firstNotNullOfOrNull { (_, parseada) -> parseada.obsGeneral }
            .orEmpty()

        MantenimientoState(
            datosGenerales = datosGenerales,
            estadoFisicoExterior = estadoDe(
                "físico exterior",
                "fisico exterior",
                "estado físico exterior",
                "estado fisico exterior",
            ),
            descripcionFisicoExterior = obsDe(
                "físico exterior",
                "fisico exterior",
                "estado físico exterior",
                "estado fisico exterior",
            ),
            estadoAisladores = estadoDe("aisladores / bushing", "aisladores", "bushing"),
            descripcionAisladores = obsDe("aisladores / bushing", "aisladores", "bushing"),
            estadoRegletas = estadoDe("regletas"),
            descripcionRegletas = obsDe("regletas"),
            estadoContactores = estadoDe("contactores"),
            descripcionContactores = obsDe("contactores"),
            estadoCables = estadoDe("cables"),
            descripcionCables = obsDe("cables"),
            estadoLucesPiloto = estadoDe("luces piloto"),
            descripcionLucesPiloto = obsDe("luces piloto"),
            estadoCanaletas = estadoDe("canaletas"),
            descripcionCanaletas = obsDe("canaletas"),
            sistemaTierraOk = true,
            sinFugasAceite = true,
            limpiezaRealizada = false,
            reajusteTorqueRealizado = false,
            lubricacionRealizada = false,
            observaciones = observacionesGeneralesRecuperadas,
            fotoPath = photoUrl,
        )
    } else {
        null
    }

    val pruebasState = if (tipoRegistro == TipoRegistro.PRUEBAS_ELECTRICAS) {
        val test = electricalTest
        val veredictoEnum = parseVeredictoPrueba(test?.verdict)
        PruebasElectricasState(
            datosGenerales = datosGenerales,
            tensionPruebaAislamiento = "1000 V",
            resistenciaAislamientoMOhm = test?.insulation?.toString().orEmpty(),
            resistenciaContactoMicroOhm = test?.contact?.toString().orEmpty(),
            voltajeLineaV = test?.voltage?.toString().orEmpty(),
            corrienteCargaA = test?.current?.toString().orEmpty(),
            veredicto = veredictoEnum,
            notasTecnicas =  test?.technicalNotes.orEmpty(),
        )
    } else {
        null
    }

    return RegistroGuardado(
        id = id,
        tipo = tipoRegistro,
        fechaMillis = fechaMillis,
        tecnicoUsername = tecnicoUsernameResuelto,
        mantenimiento = mantenimientoState,
        pruebas = pruebasState,
        archivado = if (archived) ArchivoRegistro(fechaMillis = fechaMillis) else null,
    )
}

private const val PREFIJO_OBS_GENERAL = "[OBS_GRAL]: "

private data class ObservacionParseada(
    val obsComponente: String,
    val obsGeneral: String?,
)

private fun separarObservacion(raw: String?): ObservacionParseada {
    if (raw.isNullOrBlank()) return ObservacionParseada(obsComponente = "", obsGeneral = null)

    val lineas = raw.lines()
    val general = lineas
        .firstOrNull { it.trimStart().startsWith(PREFIJO_OBS_GENERAL) }
        ?.trimStart()
        ?.removePrefix(PREFIJO_OBS_GENERAL)
        ?.trim()
        ?.takeIf { it.isNotBlank() }

    val componente = lineas
        .filterNot { it.trimStart().startsWith(PREFIJO_OBS_GENERAL) }
        .joinToString("\n")
        .trim()

    return ObservacionParseada(obsComponente = componente, obsGeneral = general)
}
private fun parseEstadoComponente(raw: String?): EstadoComponente {
    if (raw.isNullOrBlank()) return EstadoComponente.BUENO
    val normalizado = raw.trim().uppercase()
        .replace("Á", "A")
        .replace("É", "E")
        .replace("Í", "I")
        .replace("Ó", "O")
        .replace("Ú", "U")
        .replace(" ", "_")
    return try {
        EstadoComponente.valueOf(normalizado)
    } catch (_: IllegalArgumentException) {
        EstadoComponente.BUENO
    }
}


private fun parseVeredictoPrueba(raw: String?): VeredictoPrueba {
    if (raw.isNullOrBlank()) return VeredictoPrueba.APROBADO
    return try {
        VeredictoPrueba.valueOf(raw.trim().uppercase())
    } catch (_: IllegalArgumentException) {
        VeredictoPrueba.APROBADO
    }
}

private fun parseFechaToMillis(raw: String?): Long {
    if (raw.isNullOrBlank()) return System.currentTimeMillis()
    return try {
        OffsetDateTime.parse(raw).toInstant().toEpochMilli()
    } catch (_: Exception) {
        try {
            LocalDateTime.parse(raw).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: Exception) {
            try {
                Instant.parse(raw).toEpochMilli()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }
    }
}

private fun resolverNombreTecnico(
    username: String?,
    technicianId: String?,
    usuarios: List<Usuario> = emptyList(),
): String {
    val cleanUser = username?.trim().orEmpty()
    val cleanId = technicianId?.trim().orEmpty()

    val encontrado = usuarios.firstOrNull { u ->
        (cleanUser.isNotEmpty() && u.username.equals(cleanUser, ignoreCase = true)) ||
                (cleanId.isNotEmpty() && u.id == cleanId)
    }

    return encontrado?.nombre?.takeIf { it.isNotBlank() }
        ?: cleanUser.ifEmpty { cleanId }
}

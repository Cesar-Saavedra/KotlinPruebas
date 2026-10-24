package com.example.appnombre_grupo1.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

// Campo de solo lectura que despliega una lista de opciones al tocarlo
@Composable
fun SelectorDesplegable(
    etiqueta: String,
    seleccion: String,
    opciones: List<String>,
    onSeleccionar: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    // Texto de ayuda cuando el selector está deshabilitado (ej. "Seleccione primero un cliente")
    textoDeshabilitado: String? = null,
) {
    var expandido by remember { mutableStateOf(value = false) }
    val habilitado = opciones.isNotEmpty()

    Box(modifier = modifier) {
        OutlinedTextField(
            value = seleccion,
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            label = { Text(etiqueta) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            isError = error != null,
            supportingText = {
                when {
                    error != null -> Text(error, color = MaterialTheme.colorScheme.error)
                    !habilitado && textoDeshabilitado != null -> Text(textoDeshabilitado)
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        // Capa transparente que captura el toque sobre todo el campo
        if (habilitado) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expandido = true }
            )
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false },
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        expandido = false
                        onSeleccionar(opcion)
                    },
                )
            }
        }
    }
}

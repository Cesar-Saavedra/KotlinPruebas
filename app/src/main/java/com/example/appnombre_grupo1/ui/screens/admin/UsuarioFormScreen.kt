package com.example.appnombre_grupo1.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.appnombre_grupo1.model.RolUsuario
import com.example.appnombre_grupo1.model.UsuarioForm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioFormScreen(
    state: UsuarioForm,
    onUsernameChange: (String) -> Unit,
    onNombreChange: (String) -> Unit,
    onRolChange: (RolUsuario) -> Unit,
    onPasswordChange: (String) -> Unit,
    onGuardarClick: () -> Unit,
    onEliminarClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    var confirmarEliminar by remember { mutableStateOf(value = false) }
    var mostrarPassword by remember { mutableStateOf(value = false) }
    val puedeEliminar = !state.esNuevo && !state.esUsuarioActual

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.esNuevo) "Nuevo Usuario" else "Editar Usuario") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    if (puedeEliminar) {
                        IconButton(onClick = { confirmarEliminar = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar usuario")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.username,
                onValueChange = onUsernameChange,
                // El nombre de usuario identifica al usuario, no se cambia al editar
                readOnly = !state.esNuevo,
                enabled = state.esNuevo,
                label = { Text("Nombre de usuario *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre completo *") },
                supportingText = { Text("Aparece como técnico a cargo en los registros") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Rol", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RolUsuario.entries.forEach { rol ->
                    FilterChip(
                        selected = state.rol == rol,
                        onClick = { onRolChange(rol) },
                        enabled = !state.esUsuarioActual,
                        label = { Text(rol.label) },
                    )
                }
            }
            if (state.esUsuarioActual) {
                Text(
                    "No puede cambiar su propio rol",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = { Text(if (state.esNuevo) "Contraseña *" else "Nueva contraseña") },
                supportingText = {
                    if (!state.esNuevo) Text("Déjela vacía para mantener la contraseña actual")
                },
                trailingIcon = {
                    IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                        Icon(
                            imageVector = if (mostrarPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (mostrarPassword) "Ocultar contraseña" else "Mostrar contraseña",
                        )
                    }
                },
                visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = onGuardarClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text("Guardar Usuario")
            }
        }
    }

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("Eliminar usuario") },
            text = { Text("¿Seguro que desea eliminar a ${state.nombre}? Ya no podrá iniciar sesión.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    onEliminarClick()
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") }
            },
        )
    }
}

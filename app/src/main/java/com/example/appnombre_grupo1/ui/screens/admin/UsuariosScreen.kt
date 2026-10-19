package com.example.appnombre_grupo1.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appnombre_grupo1.model.RolUsuario
import com.example.appnombre_grupo1.model.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosScreen(
    usuarios: List<Usuario>,
    usernameActual: String,
    onUsuarioClick: (Usuario) -> Unit,
    onAgregarClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usuarios") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAgregarClick,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Nuevo usuario") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(usuarios, key = { it.username }) { usuario ->
                OutlinedCard(onClick = { onUsuarioClick(usuario) }, modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = {
                            Text(if (usuario.username == usernameActual) "${usuario.nombre} (tú)" else usuario.nombre)
                        },
                        supportingContent = { Text("@${usuario.username} · ${usuario.rol.label}") },
                        leadingContent = {
                            Icon(
                                imageVector = if (usuario.rol == RolUsuario.ADMINISTRADOR) {
                                    Icons.Default.AdminPanelSettings
                                } else {
                                    Icons.Default.Engineering
                                },
                                contentDescription = null,
                            )
                        },
                    )
                }
            }
        }
    }
}

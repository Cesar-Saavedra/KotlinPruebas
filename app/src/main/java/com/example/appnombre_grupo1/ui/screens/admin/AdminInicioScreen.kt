package com.example.appnombre_grupo1.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.appnombre_grupo1.model.Usuario

@Composable
private fun OpcionAdmin(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    onClick: () -> Unit,
) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(32.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            ) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Text(descripcion, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInicioScreen(
    usuario: Usuario,
    onClientesClick: () -> Unit,
    onUsuariosClick: () -> Unit,
    onRegistrosClick: () -> Unit,
    onCerrarSesion: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administración") },
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Bienvenido, ${usuario.nombre}", style = MaterialTheme.typography.titleLarge)
            Text(
                usuario.rol.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )

            Spacer(modifier = Modifier.height(8.dp))

            OpcionAdmin(
                icono = Icons.Default.Business,
                titulo = "Clientes",
                descripcion = "Agregar o editar clientes, sus instalaciones y tipos de equipo",
                onClick = onClientesClick,
            )
            OpcionAdmin(
                icono = Icons.Default.Group,
                titulo = "Usuarios",
                descripcion = "Agregar o editar técnicos y administradores",
                onClick = onUsuariosClick,
            )
            OpcionAdmin(
                icono = Icons.AutoMirrored.Filled.Assignment,
                titulo = "Registros anteriores",
                descripcion = "Revisar los registros hechos por los técnicos",
                onClick = onRegistrosClick,
            )
        }
    }
}

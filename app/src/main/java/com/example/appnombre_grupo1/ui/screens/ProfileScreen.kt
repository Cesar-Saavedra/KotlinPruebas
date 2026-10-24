package com.example.appnombre_grupo1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appnombre_grupo1.navigation.Screen
import com.example.appnombre_grupo1.viewmodels.MainViewModel

@Composable
fun ProfileScreen(
    viewModel: MainViewModel
) {
    val items = listOf(Screen.Home, Screen.Profile)
    var selectedItem by remember { mutableIntStateOf(1) } // 1 = Perfil

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            viewModel.navigateTo(screen, singleTop = true)
                        },
                        label = { Text(if (screen == Screen.Home) "Home" else "Perfil") },
                        icon = {
                            Icon(
                                imageVector = if (screen == Screen.Home) Icons.Default.Home else Icons.Default.Person,
                                contentDescription = screen.route
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar circular con icono o inicial
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Usuario Demo", style = MaterialTheme.typography.headlineSmall)
            Text(text = "cliente.demo@empresa.com", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()

            // Opciones del perfil tipo lista
            ListItem(
                headlineContent = { Text("Plan actual") },
                supportingContent = { Text("Plan Premium - Renovación Octubre") },
                leadingContent = { Icon(Icons.Default.Star, contentDescription = null) }
            )
            ListItem(
                headlineContent = { Text("Métodos de pago") },
                supportingContent = { Text("Tarjeta terminada en **** 4321") },
                leadingContent = { Icon(Icons.Default.CreditCard, contentDescription = null) }
            )
            ListItem(
                headlineContent = { Text("Historial de actividad") },
                leadingContent = { Icon(Icons.Default.History, contentDescription = null) }
            )
        }
    }
}

package com.mora.matritech.screens.superadmin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mora.matritech.data.remote.SupabaseClient
import com.mora.matritech.data.repository.InstitucionRepository
import kotlinx.coroutines.launch

// Rutas de navegación
sealed class SuperAdminRoute(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : SuperAdminRoute("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Instituciones : SuperAdminRoute("instituciones", "Instituciones", Icons.Default.AccountBalance)
    object Usuarios : SuperAdminRoute("usuarios", "Usuarios", Icons.Default.People)
    object Reportes : SuperAdminRoute("reportes", "Reportes", Icons.Default.Assessment)
    object Configuracion : SuperAdminRoute("configuracion", "Configuración", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentRoute by remember(navController) {
        derivedStateOf {
            navController.currentBackStackEntry?.destination?.route ?: SuperAdminRoute.Dashboard.route
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SuperAdminScreen() {
        // MUEVE EL navController FUERA del composable (clave #1)
        val navController = rememberNavController()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        // Usa una clave estable para evitar recrear el flow en cada recomposición (clave #2)

        // CLAVE #3: Usa snapshotFlow en vez de currentBackStackEntryFlow + clave estable
        // REEMPLAZA todo el LaunchedEffect por esto:
        val currentRoute by produceState(initialValue = SuperAdminRoute.Dashboard.route, key1 = navController) {
            snapshotFlow { navController.currentBackStackEntry?.destination?.route }
                .collect { value = it ?: SuperAdminRoute.Dashboard.route }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                SuperAdminDrawer(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        scope.launch { drawerState.close() }
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(getCurrentTitle(currentRoute)) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menú")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            ) { padding ->
                SuperAdminNavHost(
                    navController = navController,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SuperAdminDrawer(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(getCurrentTitle(currentRoute)) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { padding ->
            SuperAdminNavHost(
                navController = navController,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun SuperAdminNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Inicializar repository (manual sin Hilt)
    val repository = remember {
        InstitucionRepository(SupabaseClient.client)
    }

    NavHost(
        navController = navController,
        startDestination = SuperAdminRoute.Dashboard.route,
        modifier = modifier
    ) {
        composable(SuperAdminRoute.Dashboard.route) {
            DashboardScreen()
        }
        composable(SuperAdminRoute.Instituciones.route) {
            val viewModel = remember { InstitucionViewModel(repository) }
            InstitucionesScreen(viewModel = viewModel)
        }
        composable(SuperAdminRoute.Usuarios.route) {
            UsuariosPlaceholderScreen()
        }
        composable(SuperAdminRoute.Reportes.route) {
            ReportesPlaceholderScreen()
        }
        composable(SuperAdminRoute.Configuracion.route) {
            ConfiguracionPlaceholderScreen()
        }
    }
}

@Composable
fun SuperAdminDrawer(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp)
        ) {
            // Header
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Super Admin",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Panel de Control",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Menu items
            val menuItems = listOf(
                SuperAdminRoute.Dashboard,
                SuperAdminRoute.Instituciones,
                SuperAdminRoute.Usuarios,
                SuperAdminRoute.Reportes,
                SuperAdminRoute.Configuracion
            )

            menuItems.forEach { item ->
                NavigationDrawerItem(
                    icon = { Icon(item.icon, contentDescription = null) },
                    label = { Text(item.title) },
                    selected = currentRoute == item.route,
                    onClick = { onNavigate(item.route) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            HorizontalDivider()

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Logout, contentDescription = null) },
                label = { Text("Cerrar Sesión") },
                selected = false,
                onClick = { /* TODO: Implementar logout */ },
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedIconColor = MaterialTheme.colorScheme.error,
                    unselectedTextColor = MaterialTheme.colorScheme.error
                )
            )
        }
    }
}

@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Panel de Control",
            style = MaterialTheme.typography.headlineMedium
        )

        // Cards de estadísticas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DashboardCard(
                title = "Instituciones",
                value = "12",
                icon = Icons.Default.AccountBalance,
                modifier = Modifier.weight(1f)
            )
            DashboardCard(
                title = "Usuarios",
                value = "248",
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DashboardCard(
                title = "Admins",
                value = "24",
                icon = Icons.Default.AdminPanelSettings,
                modifier = Modifier.weight(1f)
            )
            DashboardCard(
                title = "Activos",
                value = "189",
                icon = Icons.Default.CheckCircle,
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Acciones Rápidas",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Nueva Institución")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Nuevo Usuario")
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Placeholders para las demás pantallas
@Composable
fun UsuariosPlaceholderScreen() {
    PlaceholderScreen("Usuarios", "CRUD de usuarios próximamente")
}

@Composable
fun ReportesPlaceholderScreen() {
    PlaceholderScreen("Reportes", "Reportes y estadísticas próximamente")
}

@Composable
fun ConfiguracionPlaceholderScreen() {
    PlaceholderScreen("Configuración", "Configuración del sistema próximamente")
}

@Composable
fun PlaceholderScreen(title: String, message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Construction,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getCurrentTitle(route: String): String {
    return when (route) {
        SuperAdminRoute.Dashboard.route -> "Dashboard"
        SuperAdminRoute.Instituciones.route -> "Instituciones"
        SuperAdminRoute.Usuarios.route -> "Usuarios"
        SuperAdminRoute.Reportes.route -> "Reportes"
        SuperAdminRoute.Configuracion.route -> "Configuración"
        else -> "Super Admin"
    }
}
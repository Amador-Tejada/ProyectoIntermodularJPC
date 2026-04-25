package com.example.proyectointermodularjpc.Menu

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyectointermodularjpc.Paginas.Clientes.EditarCliente
import com.example.proyectointermodularjpc.Paginas.Clientes.FormularioNuevoCliente
import com.example.proyectointermodularjpc.Paginas.Clientes.ListarClientes
import com.example.proyectointermodularjpc.Paginas.Clientes.TareasCliente
import com.example.proyectointermodularjpc.Paginas.PrincipalCalendario
import com.example.proyectointermodularjpc.Paginas.Productos.Inventario
import com.example.proyectointermodularjpc.Paginas.Productos.NuevoProducto
import com.example.proyectointermodularjpc.Paginas.Trabajos.DetallesTareas
import com.example.proyectointermodularjpc.Paginas.Trabajos.EditarTarea
import com.example.proyectointermodularjpc.Paginas.Trabajos.ListaTareas
import com.example.proyectointermodularjpc.Paginas.Trabajos.NuevasTareas
import kotlinx.coroutines.launch

@Immutable
data class PantallaMenu(
    // Ruta única de navegación (identificador) para esta pantalla en el NavHost.
    val ruta: String,
    // Título legible para mostrar en el menú y en la TopAppBar.
    val titulo: String,
    // Icono del elemento del menú (composable para permitir cualquier icono).
    val icono: @Composable () -> Unit,
)

/**
 * Rutas de navegación de la app.
 */
object Rutas {
    const val CALENDARIO = "calendario"
    const val LISTATAREAS = "listatareas"
    const val INVENTARIO = "inventario"
    const val CLIENTES = "clientes"
    const val NUEVOCLIENTE = "nuevo_cliente"
    const val NUEVOPRODUCTO = "nuevo_producto"
    const val DETALLETAREA = "detalle_tarea"
    const val NUEVATAREA = "nueva_tarea"
    const val EDITARTAREA = "editar_tarea"
    const val TAREAS_CLIENTE = "tareas_cliente"
    const val EDITARCLIENTE = "editar_cliente"
}

/**
 * Lista de pantallas que aparecen en el menú hamburguesa.
 */
val pantallasMenu: List<PantallaMenu> = listOf(
    PantallaMenu(Rutas.CALENDARIO, "Calendario") { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
    PantallaMenu(Rutas.LISTATAREAS, "Tareas") { Icon(Icons.Filled.Task, contentDescription = null) },
    PantallaMenu(Rutas.INVENTARIO, "Inventario") { Icon(Icons.Filled.Inventory2, contentDescription = null) },
    PantallaMenu(Rutas.CLIENTES, "Clientes") { Icon(Icons.Filled.Groups, contentDescription = null) },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppConMenuHamburguesa() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                pantallasMenu.forEach { pantalla ->
                    // Comprobamos si la ruta actual pertenece a la sección de esta pantalla del menú
                    val esRutaDeSeccion = when (pantalla.ruta) {
                        Rutas.LISTATAREAS -> rutaActual?.startsWith(Rutas.DETALLETAREA) == true || rutaActual?.startsWith(Rutas.NUEVATAREA) == true || rutaActual?.startsWith(Rutas.EDITARTAREA) == true
                        Rutas.CLIENTES -> rutaActual?.startsWith(Rutas.TAREAS_CLIENTE) == true || rutaActual?.startsWith(Rutas.NUEVOCLIENTE) == true || rutaActual?.startsWith(Rutas.EDITARCLIENTE) == true
                        Rutas.INVENTARIO -> rutaActual?.startsWith(Rutas.NUEVOPRODUCTO) == true
                        else -> false
                    }

                    NavigationDrawerItem(
                        label = { Text(pantalla.titulo) },
                        selected = rutaActual == pantalla.ruta || esRutaDeSeccion,
                        onClick = {
                            scope.launch { drawerState.close() }
                            
                            val esCalendario = pantalla.ruta == Rutas.CALENDARIO
                            val yaEnSeccion = (rutaActual == pantalla.ruta || esRutaDeSeccion)

                            navController.navigate(pantalla.ruta) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = !yaEnSeccion && !esCalendario
                                }
                                launchSingleTop = true
                                restoreState = !yaEnSeccion && !esCalendario
                            }
                        },
                        icon = pantalla.icono
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val pantalla = pantallasMenu.find { it.ruta == rutaActual }
                        Text(pantalla?.titulo ?: "Detalle")
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Rutas.CALENDARIO,
                modifier = Modifier.padding(padding)
            ) {
                composable(Rutas.CALENDARIO) {
                    PrincipalCalendario(alPulsarTarea = { id -> navController.navigate("${Rutas.DETALLETAREA}/$id") })
                }
                composable(Rutas.LISTATAREAS) {
                    ListaTareas(
                        alPulsarTarea = { id: Long -> navController.navigate("${Rutas.DETALLETAREA}/$id") },
                        alPulsarNuevaTarea = { navController.navigate(Rutas.NUEVATAREA) }
                    )
                }
                composable(Rutas.INVENTARIO) { backStackEntry ->
                    Inventario(
                        alPulsarBorrarProducto = { /* No implementado */ },
                        alPulsarNuevoProducto = { navController.navigate(Rutas.NUEVOPRODUCTO) },
                        backStackEntry = backStackEntry
                    )
                }
                composable(Rutas.CLIENTES) { backStackEntry ->
                    ListarClientes(
                        alPulsarAnadirCliente = { navController.navigate(Rutas.NUEVOCLIENTE) },
                        backStackEntry = backStackEntry,
                        alPulsarCliente = { id -> navController.navigate("${Rutas.TAREAS_CLIENTE}/$id") },
                        alPulsarEditarCliente = { id -> navController.navigate("${Rutas.EDITARCLIENTE}/$id") }
                    )
                }
                composable(Rutas.NUEVOCLIENTE) {
                    FormularioNuevoCliente(
                        alGuardar = { navController.popBackStack() },
                        alCancelar = { navController.popBackStack() }
                    )
                }
                composable(Rutas.NUEVOPRODUCTO) {
                    NuevoProducto(
                        alGuardado = { navController.popBackStack() },
                        alCancelar = { navController.popBackStack() }
                    )
                }
                composable(Rutas.NUEVATAREA) {
                    NuevasTareas(
                        alFinalizar = { navController.popBackStack() },
                        alCancelar = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "${Rutas.DETALLETAREA}/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.LongType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("id") ?: 0L
                    DetallesTareas(
                        trabajoId = id,
                        alEditar = { navController.navigate("${Rutas.EDITARTAREA}/$id") },
                        alVolver = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "${Rutas.EDITARTAREA}/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.LongType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("id") ?: 0L
                    EditarTarea(
                        trabajoId = id,
                        alFinalizar = { navController.popBackStack() },
                        alCancelar = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "${Rutas.TAREAS_CLIENTE}/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.LongType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("id") ?: 0L
                    TareasCliente(
                        clienteId = id,
                        alAbrirDetalle = { tareaId: Long -> navController.navigate("${Rutas.DETALLETAREA}/$tareaId") }
                    )
                }
                composable(
                    route = "${Rutas.EDITARCLIENTE}/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.LongType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("id") ?: 0L
                    EditarCliente(
                        clienteId = id,
                        alFinalizar = { 
                            navController.previousBackStackEntry?.savedStateHandle?.set("refrescar_clientes", true)
                            navController.popBackStack() 
                        },
                        alCancelar = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

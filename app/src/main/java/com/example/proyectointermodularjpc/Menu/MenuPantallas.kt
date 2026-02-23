package com.example.proyectointermodularjpc.Menu

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Cliente
import com.example.proyectointermodularjpc.Paginas.PrincipalCalendario
import com.example.proyectointermodularjpc.Paginas.Productos.Inventario
import com.example.proyectointermodularjpc.Paginas.Clientes.ListarClientes
import com.example.proyectointermodularjpc.Paginas.Clientes.FormularioNuevoCliente
import com.example.proyectointermodularjpc.Paginas.Productos.NuevoProducto
import com.example.proyectointermodularjpc.Paginas.Trabajos.ListaTareas
import com.example.proyectointermodularjpc.Paginas.Trabajos.DetallesTareas
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
 *
 * Cada constante es una route del NavHost. Mantén los strings estables para evitar romper deep-links
 * o restauración de estado.
 */
object Rutas {
    const val CALENDARIO = "calendario"
    const val LISTATAREAS = "listatareas"
    const val INVENTARIO = "inventario"
    const val CLIENTES = "clientes"
    const val PRESUPUESTOS = "presupuestos"
    const val NUEVOCLIENTE = "nuevo_cliente"
    const val NUEVOPRODUCTO = "nuevo_producto"
    const val DETALLETAREA = "detalle_tarea"
    const val NUEVATAREA = "nueva_tarea"
}

/**
 * Lista de pantallas que aparecen en el menú hamburguesa.
 *
 * El orden de esta lista define el orden visual de los items del drawer.
 */
val pantallasMenu: List<PantallaMenu> = listOf(
    PantallaMenu(Rutas.CALENDARIO, "Calendario") { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
    PantallaMenu(Rutas.LISTATAREAS, "Tareas") { Icon(Icons.Filled.Task, contentDescription = null) },
    PantallaMenu(Rutas.INVENTARIO, "Inventario") { Icon(Icons.Filled.Inventory2, contentDescription = null) },
    PantallaMenu(Rutas.CLIENTES, "Clientes") { Icon(Icons.Filled.Groups, contentDescription = null) },
 )

/**
 * Contenedor principal de la app con:
 * - Menú hamburguesa (drawer) para navegar.
 * - TopAppBar con icono de menú.
 * - NavHost con las pantallas.
 *
 * Úsalo como raíz de la app para que el menú exista en todas las pantallas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppConMenuHamburguesa(
    modifier: Modifier = Modifier,
    // Controlador de navegación. Se expone para poder inyectarlo en tests o en previews si hiciera falta.
    navController: NavHostController = rememberNavController(),
) {
    // Estado de apertura/cierre del drawer.
    val estadoDrawer = rememberDrawerState(initialValue = DrawerValue.Closed)
    // Scope para lanzar corrutinas (abrir/cerrar drawer es un suspend).
    val scope = rememberCoroutineScope()

    // Entrada actual del backstack para saber en qué ruta estamos.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route ?: Rutas.CALENDARIO

    // Título dinámico de la AppBar según pantalla.
    val pantallaActual = pantallasMenu.firstOrNull { it.ruta == rutaActual }
    val tituloAppBar = pantallaActual?.titulo ?: "Proyecto"

    // Drawer modal (se superpone al contenido).
    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = estadoDrawer,
        drawerContent = {
            ModalDrawerSheet {
                // Encabezado del drawer.
                Text(
                    text = "Menú",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                )

                // Items del menú (uno por cada pantalla).
                pantallasMenu.forEach { pantalla ->
                    val seleccionada = pantalla.ruta == rutaActual
                    NavigationDrawerItem(
                        label = { Text(pantalla.titulo) },
                        selected = seleccionada,
                        onClick = {
                            // Navegación con:
                            // - popUpTo(start) para no apilar infinitamente destinos.
                            // - restore/saveState para recordar estado al cambiar de pantalla.
                            navController.navigate(pantalla.ruta) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            // Cerramos el drawer tras seleccionar.
                            scope.launch { estadoDrawer.close() }
                        },
                        icon = pantalla.icono,
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )
                }
            }
        },
    ) {
        // Scaffold con TopAppBar.
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(tituloAppBar) },
                    navigationIcon = {
                        // Botón hamburguesa: abre el drawer.
                        IconButton(onClick = { scope.launch { estadoDrawer.open() } }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Abrir menú",
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            // NavHost: aquí declaramos qué composable se muestra para cada ruta.
            NavHost(
                navController = navController,
                // Pantalla de inicio: PrincipalCalendario.
                startDestination = Rutas.CALENDARIO,
                modifier = Modifier.padding(paddingValues),
            ) {
                composable(Rutas.CALENDARIO) {
                    // Pantalla principal: calendario.
                    PrincipalCalendario(trabajos = emptyList())
                }
                composable(Rutas.LISTATAREAS) {
                    ListaTareas(
                        alPulsarTarea = { idTrabajo ->
                            navController.navigate("${Rutas.DETALLETAREA}/$idTrabajo")
                        },
                        alPulsarNuevaTarea = {
                            navController.navigate(Rutas.NUEVATAREA)
                        },
                    )
                }
                composable(Rutas.INVENTARIO) {
                    // Inventario: carga productos desde la API.
                    Inventario(
                        alPulsarBorrarProducto = {

                        },
                        alPulsarNuevoProducto = {
                            navController.navigate(Rutas.NUEVOPRODUCTO)
                        },
                    )
                }
                composable(Rutas.CLIENTES) {
                    // Lista de clientes con datos de ejemplo (temporal). Sustituir por datos reales de la API/DB.
                    ListarClientes(
                        clientes = listOf(
                            Cliente(id = 1, nombre = "Juan Pérez", telefono = "600123123", direccion = "C/ Mayor 1", correoElectronico = "juan@example.com"),
                            Cliente(id = 2, nombre = "María López", telefono = "611222333", direccion = null, correoElectronico = "maria@example.com"),
                            Cliente(id = 3, nombre = "Taller García", telefono = "955000111", direccion = "Pol. Industrial", correoElectronico = "taller@example.com"),
                        ),
                        alPulsarAnadirCliente = {
                            navController.navigate(Rutas.NUEVOCLIENTE)
                        },
                    )
                }
                composable(Rutas.NUEVOCLIENTE) {
                    FormularioNuevoCliente(
                        alGuardar = {
                            navController.popBackStack()
                        },
                        alCancelar = {
                            navController.popBackStack()
                        },
                    )
                }
                composable(Rutas.PRESUPUESTOS) {
                    // TODO: reemplazar por la pantalla real cuando exista.
                    PantallaPlaceholder("Presupuestos (pendiente)")
                }
                composable(Rutas.NUEVOPRODUCTO) {
                    NuevoProducto(
                        alGuardar = {
                            // TODO: guardar en API/DB y refrescar lista.
                            navController.popBackStack()
                        },
                        alCancelar = {
                            navController.popBackStack()
                        },
                    )
                }
                composable(
                    route = "${Rutas.DETALLETAREA}/{idTrabajo}",
                ) { backStackEntry ->
                    val idTrabajo = backStackEntry.arguments?.getString("idTrabajo")?.toLongOrNull()
                    if (idTrabajo != null) {
                        DetallesTareas(
                            idTrabajo = idTrabajo,
                            alVolver = { navController.popBackStack() },
                            alEditar = {
                                // TODO: pantalla de edición.
                            },
                        )
                    } else {
                        PantallaPlaceholder("Detalle de tarea: id inválido")
                    }
                }
                composable(Rutas.NUEVATAREA) {
                    NuevasTareas(
                        alGuardar = { navController.popBackStack() },
                        alCancelar = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

/**
 * Pantalla sencilla de texto para destinos aún no implementados.
 *
 * Útil para comprobar que la navegación funciona mientras desarrollas el resto de pantallas.
 */
@Composable
private fun PantallaPlaceholder(texto: String) {
    Text(
        text = texto,
        modifier = Modifier.padding(16.dp),
        style = MaterialTheme.typography.bodyLarge,
    )
}

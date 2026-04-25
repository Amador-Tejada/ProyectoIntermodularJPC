package com.example.proyectointermodularjpc.Paginas.Productos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Producto
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitClient
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Modelo de UI que combina Producto e Inventario
data class ItemInventarioUI(
    val producto: Producto,
    val cantidad: Double,
    val inventarioId: Long? // Por si necesitamos actualizarlo luego
)

@Composable
fun Inventario(
    alPulsarBorrarProducto: (Long) -> Unit,
    alPulsarNuevoProducto: () -> Unit,
    backStackEntry: NavBackStackEntry,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var listaUI by remember { mutableStateOf<List<ItemInventarioUI>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun cargarDatos() {
        scope.launch {
            cargando = true
            error = null
            try {
                // Cargamos ambas listas en paralelo
                val resProductos =
                    withContext(Dispatchers.IO) { RetrofitClient.apiService.getProductos().execute() }
                val resInventario =
                    withContext(Dispatchers.IO) { RetrofitClient.apiService.getInventario().execute() }

                if (resProductos.isSuccessful && resInventario.isSuccessful) {
                    val productos = resProductos.body().orEmpty()
                    val inventarios = resInventario.body().orEmpty()

                    // Combinamos: Para cada producto buscamos su stock
                    listaUI = productos.map { prod ->
                        val inv = inventarios.find { it.producto?.id == prod.id }
                        ItemInventarioUI(
                            producto = prod,
                            cantidad = inv?.cantidadDisponible ?: 0.0,
                            inventarioId = inv?.id
                        )
                    }
                } else {
                    error = "Error al obtener datos del servidor"
                }
            } catch (e: Exception) {
                error = "Error de conexión: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(backStackEntry) {
        cargarDatos()
    }

    fun eliminar(id: Long) {
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.eliminarProducto(id).execute()
                }
                if (response.isSuccessful) {
                    listaUI = listaUI.filter { it.producto.id != id }
                } else {
                    error = RetrofitUtils.errorMessage(response)
                }
            } catch (e: Exception) {
                error = "Error al eliminar: ${e.message}"
            }
        }
    }

    val filtrados = remember(query, listaUI) {
        if (query.isBlank()) listaUI
        else listaUI.filter {
            it.producto.nombre.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = alPulsarNuevoProducto) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Producto")
            }
        }
    ) { padding ->
        Column(modifier = modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar producto...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (cargando && listaUI.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }

                if (filtrados.isEmpty() && !cargando) {
                    Text(
                        text = if (query.isEmpty()) "No hay productos" else "No hay coincidencias",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtrados, key = { it.producto.id ?: it.hashCode() }) { item ->
                        ProductoItemUI(
                            item = item,
                            onBorrar = { item.producto.id?.let { eliminar(it) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductoItemUI(item: ItemInventarioUI, onBorrar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.4f
            )
        )
    ) {
        ListItem(
            headlineContent = {
                Text(
                    item.producto.nombre,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            supportingContent = {
                item.producto.descripcion?.let {
                    Text(
                        it,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            trailingContent = {
                IconButton(onClick = onBorrar) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Borrar producto",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}
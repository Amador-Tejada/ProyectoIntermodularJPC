package com.example.proyectointermodularjpc.Paginas.Productos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Producto
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.ApiService
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitClient
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/* En esta pantalla tenemos una barra de búsqueda para buscar un producto por nombre usando la ApiService y RetrofitClient.
 * La lista que hay debajo de la barra de búsqueda muestra los productos que coincidan con la búsqueda
 * y se actualiza por cada cambio de texto en la barra de búsqueda (con un pequeño debounce).
 *
 * Los 2 botones debajo de la barra: uno borrará el artículo de la DB y el otro abrirá
 * la pantalla de NuevoProducto que tiene un formulario para añadir un nuevo producto.
 */

/**
 * Pantalla de inventario.
 *
 * Contrato:
 * - Descarga productos desde la API y filtra en memoria por nombre.
 * - Actualiza resultados en tiempo real según el texto introducido en la barra de búsqueda.
 * - [alPulsarBorrarProducto] y [alPulsarNuevoProducto] conectan acciones externas (API/DB y navegación).
 */
@Composable
fun Inventario(
    modifier: Modifier = Modifier,
    alPulsarBorrarProducto: () -> Unit = {},
    alPulsarNuevoProducto: () -> Unit = {},
) {
    var textoBusqueda by remember { mutableStateOf("") }

    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }

    // Cliente retrofit + service.
    val apiService: ApiService = remember { RetrofitClient.apiService }

    // Carga inicial.
    LaunchedEffect(Unit) {
        cargando = true
        error = null

        try {
            val lista: List<Producto> = withContext(Dispatchers.IO) {
                val response = apiService.getProductos().execute()
                if (!response.isSuccessful) {
                    throw IllegalStateException(RetrofitUtils.errorMessage(response))
                }
                response.body() ?: emptyList()
            }
            productos = lista
        } catch (t: Throwable) {
            error = t.message ?: "${t::class.java.simpleName}: ${t.localizedMessage ?: "Error al cargar productos"}"
            productos = emptyList()
        } finally {
            cargando = false
        }
    }

    // Debounce de búsqueda: si más adelante cambias a endpoint de búsqueda, aquí es donde dispararías la llamada.
    var terminoBusquedaDebounced by remember { mutableStateOf("") }
    LaunchedEffect(textoBusqueda) {
        delay(250)
        terminoBusquedaDebounced = textoBusqueda
    }

    val productosFiltrados = remember(productos, terminoBusquedaDebounced) {
        val consulta = terminoBusquedaDebounced.trim()
        if (consulta.isEmpty()) {
            productos
        } else {
            productos.filter { it.nombre.contains(consulta, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Buscar por nombre") },
            placeholder = { Text("Ej: tornillo, pintura, cable…") },
        )

        // Botones de acción del inventario.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(onClick = alPulsarBorrarProducto) {
                Text("Borrar")
            }
            Button(onClick = alPulsarNuevoProducto) {
                Text("Nuevo producto")
            }
        }

        when {
            cargando -> {
                Text(
                    text = "Cargando productos…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            error != null -> {
                Text(
                    text = error ?: "Error desconocido",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            else -> {
                Text(
                    text = "Resultados: ${productosFiltrados.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (productosFiltrados.isEmpty()) {
                    Text(
                        text = "No hay productos que coincidan con la búsqueda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            items = productosFiltrados,
                            key = { it.id ?: it.nombre },
                        ) { producto ->
                            FilaProducto(producto = producto)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Fila visual para un [Producto] dentro del listado.
 */
@Composable
private fun FilaProducto(producto: Producto) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = producto.nombre.ifBlank { "(Sin nombre)" },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Mostramos la unidad de medida como dato rápido.
                Text(
                    text = producto.unidadMedida,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            val descripcion = producto.descripcion
            if (!descripcion.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

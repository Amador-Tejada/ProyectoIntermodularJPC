package com.example.proyectointermodularjpc.Paginas.Clientes

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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Cliente
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitClient
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/* En la pantalla tenemos una barra de busqueda para buscar un cliente, un boton para añadir un cliente nuevo
 * y una lista de clientes que mostrara todos los clientes. Si la barra de busqueda tiene parametros la lista
 * mostrara los clientes que coincidan con la busqueda.
 */

/**
 * Pantalla para listar clientes.
 *
 * Carga la lista desde la API y filtra localmente según la búsqueda.
 */
@Composable
fun ListarClientes(
    modifier: Modifier = Modifier,
    alPulsarAnadirCliente: () -> Unit = {},
    backStackEntry: androidx.navigation.NavBackStackEntry? = null // Agregado para recibir el backStackEntry
) {
    var textoBusqueda by remember { mutableStateOf("") }

    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var clientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }

    suspend fun cargarClientes() {
        cargando = true
        error = null
        try {
            val lista = withContext(Dispatchers.IO) {
                val response = RetrofitClient.apiService.getClientes().execute()
                if (!response.isSuccessful) throw IllegalStateException(RetrofitUtils.errorMessage(response))
                response.body().orEmpty()
            }
            clientes = lista
        } catch (t: Throwable) {
            error = t.message ?: "${t::class.java.simpleName}: ${t.localizedMessage ?: "Error al cargar clientes"}"
            clientes = emptyList()
        } finally {
            cargando = false
        }
    }

    LaunchedEffect(Unit) {
        cargarClientes()
    }

    // Si venimos de crear un cliente, refrescamos automáticamente.
    // El flag lo pone MenuPantallas en SavedStateHandle.
    val refrescar = backStackEntry?.savedStateHandle?.get<Boolean>("refrescar_clientes") == true
    LaunchedEffect(refrescar) {
        if (refrescar) {
            cargarClientes()
            backStackEntry?.savedStateHandle?.set("refrescar_clientes", false)
        }
    }

    val clientesFiltrados = remember(clientes, textoBusqueda) {
        val consulta = textoBusqueda.trim()
        if (consulta.isEmpty()) clientes
        else clientes.filter { cliente ->
            cliente.nombre.contains(consulta, ignoreCase = true) ||
                cliente.correoElectronico.contains(consulta, ignoreCase = true) ||
                cliente.telefono.contains(consulta, ignoreCase = true) ||
                (cliente.direccion?.contains(consulta, ignoreCase = true) == true)
        }
    }

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Clientes",
            style = MaterialTheme.typography.titleLarge,
        )

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Buscar cliente") },
            placeholder = { Text("Nombre, correo o teléfono") },
            enabled = !cargando,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (cargando) "Cargando…" else "Resultados: ${clientesFiltrados.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {

                Button(onClick = alPulsarAnadirCliente, enabled = !cargando) {
                    Text("Añadir")
                }
            }
        }

        if (error != null) {
            Text(
                text = error ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        when {
            cargando -> {
                Text(
                    text = "Cargando clientes…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            clientesFiltrados.isEmpty() -> {
                Text(
                    text = "No hay clientes que coincidan con la búsqueda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = clientesFiltrados,
                        key = { it.id ?: it.correoElectronico },
                    ) { cliente ->
                        FilaCliente(cliente = cliente)
                    }
                }
            }
        }
    }
}

/**
 * Fila visual de un cliente.
 */
@Composable
private fun FilaCliente(cliente: Cliente) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(12.dp)
            .fillMaxWidth()) {
            Text(
                text = cliente.nombre.ifBlank { "(Sin nombre)" },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (cliente.correoElectronico.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = cliente.correoElectronico,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (cliente.telefono.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = cliente.telefono,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            val direccion = cliente.direccion
            if (!direccion.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = direccion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

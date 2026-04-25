package com.example.proyectointermodularjpc.Paginas.Trabajos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Trabajo
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitClient
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ListaTareas(
    modifier: Modifier = Modifier,
    alPulsarTarea: (Long) -> Unit,
    alPulsarNuevaTarea: () -> Unit
) {
    var textoBusqueda by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var trabajos by remember { mutableStateOf<List<Trabajo>>(emptyList()) }

    fun cargarTrabajos() {
        cargando = true
        error = null
        RetrofitClient.apiService.getTrabajos().enqueue(object : retrofit2.Callback<List<Trabajo>> {
            override fun onResponse(call: retrofit2.Call<List<Trabajo>>, response: retrofit2.Response<List<Trabajo>>) {
                if (response.isSuccessful) {
                    trabajos = response.body().orEmpty()
                } else {
                    error = RetrofitUtils.errorMessage(response)
                }
                cargando = false
            }

            override fun onFailure(call: retrofit2.Call<List<Trabajo>>, t: Throwable) {
                error = t.message ?: "Error de red"
                cargando = false
            }
        })
    }

    LaunchedEffect(Unit) {
        cargarTrabajos()
    }

    val trabajosFiltrados = remember(trabajos, textoBusqueda) {
        if (textoBusqueda.isBlank()) trabajos
        else trabajos.filter { it.titulo.contains(textoBusqueda, ignoreCase = true) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                label = { Text("Buscar tarea") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (error != null) {
                Text(text = error!!, color = MaterialTheme.colorScheme.error)
            } else if (trabajosFiltrados.isEmpty()) {
                Text("No se encontraron tareas.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(trabajosFiltrados) { trabajo ->
                        FilaTarea(trabajo = trabajo, alPulsar = { trabajo.id?.let(alPulsarTarea) })
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = alPulsarNuevaTarea,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nueva Tarea")
        }
    }
}

@Composable
fun FilaTarea(trabajo: Trabajo, alPulsar: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alPulsar),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = trabajo.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = trabajo.prioridad.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Estado: ${trabajo.estado.name}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            
            if (!trabajo.descripcion.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = trabajo.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Fecha: ${trabajo.fechaProgramada}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

package com.example.proyectointermodularjpc.Paginas.Clientes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Trabajo
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitClient
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TareasCliente(
    clienteId: Long,
    modifier: Modifier = Modifier,
    alAbrirDetalle: (Long) -> Unit
) {
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var tareas by remember { mutableStateOf<List<Trabajo>>(emptyList()) }

    LaunchedEffect(clienteId) {
        cargando = true
        error = null
        try {
            val lista = withContext(Dispatchers.IO) {
                val response = RetrofitClient.apiService.getTrabajos().execute()
                if (!response.isSuccessful) throw IllegalStateException(RetrofitUtils.errorMessage(response))
                response.body().orEmpty().filter { it.cliente?.id == clienteId }
            }
            tareas = lista
        } catch (t: Throwable) {
            error = t.message ?: "Error al cargar tareas"
            tareas = emptyList()
        } finally {
            cargando = false
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Tareas del cliente",
            style = MaterialTheme.typography.titleLarge,
        )

        when {
            cargando -> {
                Text(
                    text = "Cargando tareas…",
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
            tareas.isEmpty() -> {
                Text(
                    text = "No hay tareas para este cliente.",
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
                        items = tareas,
                        key = { it.id ?: it.titulo },
                    ) { trabajo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable { trabajo.id?.let(alAbrirDetalle) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = trabajo.titulo.ifBlank { "(Sin título)" },
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                val descripcion = trabajo.descripcion
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
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Estado: ${trabajo.estado} · Fecha: ${trabajo.fechaProgramada}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

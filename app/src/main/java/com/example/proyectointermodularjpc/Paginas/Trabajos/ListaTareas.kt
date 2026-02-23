package com.example.proyectointermodularjpc.Paginas.Trabajos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.EstadoTrabajo
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Trabajo
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitClient
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/* Pantalla que tendra una lista de tareas, un boton para añadir una nueva
 * y una selecion para ver los que estan pendientes y los que estan completados.
 * La lista se cargara con la ApiService.
 *
 * Si pulsamos en una tarea pasaremos a la pantalla DetallesTareas que nos
 * mostrara toda la informacion que contiene la tarea
 */

private enum class FiltroTareas { PENDIENTES, COMPLETADAS }

/**
 * ListaTareas carga los trabajos desde la API y permite filtrarlos.
 */
@Composable
fun ListaTareas(
    modifier: Modifier = Modifier,
    alPulsarTarea: (Long) -> Unit,
    alPulsarNuevaTarea: () -> Unit,
) {
    var filtro by remember { mutableStateOf(FiltroTareas.PENDIENTES) }

    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var trabajos by remember { mutableStateOf<List<Trabajo>>(emptyList()) }

    fun esCompletado(estado: EstadoTrabajo): Boolean = estado == EstadoTrabajo.COMPLETADO

    LaunchedEffect(Unit) {
        cargando = true
        error = null
        try {
            val lista = withContext(Dispatchers.IO) {
                val response = RetrofitClient.apiService.getTrabajos().execute()
                if (!response.isSuccessful) throw IllegalStateException(RetrofitUtils.errorMessage(response))
                response.body().orEmpty()
            }
            trabajos = lista
        } catch (t: Throwable) {
            error = t.message ?: "Error al cargar tareas"
            trabajos = emptyList()
        } finally {
            cargando = false
        }
    }

    val trabajosFiltrados = remember(trabajos, filtro) {
        when (filtro) {
            FiltroTareas.PENDIENTES -> trabajos.filterNot { esCompletado(it.estado) }
            FiltroTareas.COMPLETADAS -> trabajos.filter { esCompletado(it.estado) }
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Tareas", style = MaterialTheme.typography.titleLarge)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = { filtro = FiltroTareas.PENDIENTES },
                enabled = filtro != FiltroTareas.PENDIENTES,
            ) {
                Text("Pendientes")
            }
            OutlinedButton(
                onClick = { filtro = FiltroTareas.COMPLETADAS },
                enabled = filtro != FiltroTareas.COMPLETADAS,
            ) {
                Text("Completadas")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(onClick = alPulsarNuevaTarea) {
                Text("Nueva")
            }
        }

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

            else -> {
                Text(
                    text = "Resultados: ${trabajosFiltrados.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (trabajosFiltrados.isEmpty()) {
                    Text(
                        text = "No hay tareas para este filtro.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            items = trabajosFiltrados,
                            key = { it.id ?: (it.titulo + it.fechaProgramada) },
                        ) { trabajo ->
                            FilaTarea(
                                trabajo = trabajo,
                                alPulsar = { trabajo.id?.let(alPulsarTarea) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaTarea(
    trabajo: Trabajo,
    alPulsar: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
        onClick = alPulsar,
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

package com.example.proyectointermodularjpc.Paginas.Trabajos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Trabajo
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitClient
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/* Esta es la pantalla que nos muestra en detalle la informacion de la tarea que
 * queremos ver o modificar.
 *
 * Tenemos un boton para editar y otro para borrar que ejecutaran la accion en la base de datos
 * y al pulsarlos nos llevara a la pantalla de ListarTareas .
 */

@Composable
fun DetallesTareas(
    modifier: Modifier = Modifier,
    idTrabajo: Long,
    alVolver: () -> Unit,
    alEditar: (Long) -> Unit,
) {
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var trabajo by remember { mutableStateOf<Trabajo?>(null) }
    var borrando by remember { mutableStateOf(false) }

    LaunchedEffect(idTrabajo) {
        cargando = true
        error = null
        try {
            val item = withContext(Dispatchers.IO) {
                val response = RetrofitClient.apiService.getTrabajo(idTrabajo).execute()
                if (!response.isSuccessful) throw IllegalStateException(RetrofitUtils.errorMessage(response))
                response.body()
            }
            trabajo = item
        } catch (t: Throwable) {
            error = t.message ?: "Error al cargar detalle"
            trabajo = null
        } finally {
            cargando = false
        }
    }

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Detalle de la tarea", style = MaterialTheme.typography.titleLarge)

        when {
            cargando -> Text(
                text = "Cargando…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            error != null -> Text(
                text = error ?: "Error desconocido",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )

            trabajo == null -> Text(
                text = "No se encontró la tarea.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            else -> {
                val t = trabajo!!
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = t.titulo, style = MaterialTheme.typography.titleMedium)
                        val descripcion = t.descripcion
                        if (!descripcion.isNullOrBlank()) {
                            Text(text = descripcion, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(text = "Fecha: ${t.fechaProgramada}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Estado: ${t.estado}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Prioridad: ${t.prioridad}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Cliente: ${t.cliente.nombre}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Trabajador: ${t.trabajador?.nombre}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(onClick = { alEditar(idTrabajo) }, enabled = !borrando) {
                        Text("Editar")
                    }
                    Button(
                        onClick = {
                            if (borrando) return@Button
                            borrando = true
                            error = null
                            scope.launch {
                                try {
                                    withContext(Dispatchers.IO) {
                                        val resp = RetrofitClient.apiService.eliminarTrabajo(idTrabajo).execute()
                                        if (!resp.isSuccessful) throw IllegalStateException(RetrofitUtils.errorMessage(resp))
                                    }
                                    alVolver()
                                } catch (t2: Throwable) {
                                    error = t2.message ?: "Error al borrar"
                                    borrando = false
                                }
                            }
                        },
                        enabled = !borrando,
                    ) {
                        Text(if (borrando) "Borrando…" else "Borrar")
                    }
                }
            }
        }

        OutlinedButton(onClick = alVolver) {
            Text("Volver")
        }
    }
}

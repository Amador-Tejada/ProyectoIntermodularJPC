package com.example.proyectointermodularjpc.Paginas.Trabajos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Cliente
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.EstadoTrabajo
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.PrioridadTrabajo
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Trabajador
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Trabajo
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitClient
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/* Aqui tenemos un formulario para añadir un nuevo trabajo, un boton para guardar el trabajo
 * en la base de datos mediante la Api y otro para cancelar.
 * Ambos nos devolvera a la pantalla de ListaTareas.
 */

/**
 * Formulario para crear un nuevo [Trabajo] mediante la API.
 */
@Composable
fun NuevasTareas(
    modifier: Modifier = Modifier,
    alGuardar: () -> Unit,
    alCancelar: () -> Unit,
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaProgramada by remember { mutableStateOf("") }

    var estado by remember { mutableStateOf(EstadoTrabajo.PENDIENTE) }
    var prioridad by remember { mutableStateOf(PrioridadTrabajo.MEDIA) }

    var clientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var trabajadores by remember { mutableStateOf<List<Trabajador>>(emptyList()) }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var trabajadorSeleccionado by remember { mutableStateOf<Trabajador?>(null) }

    var cargandoDatos by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val tituloValido = remember(titulo) { titulo.trim().isNotEmpty() }
    val fechaValida = remember(fechaProgramada) { fechaProgramada.trim().isNotEmpty() }

    // Cargamos clientes y trabajadores para poder asociarlos al trabajo.
    LaunchedEffect(Unit) {
        cargandoDatos = true
        error = null
        try {
            val (listaClientes, listaTrabajadores) = withContext(Dispatchers.IO) {
                val respClientes = RetrofitClient.apiService.getClientes().execute()
                if (!respClientes.isSuccessful) throw IllegalStateException(RetrofitUtils.errorMessage(respClientes))

                val respTrabajadores = RetrofitClient.apiService.getTrabajadores().execute()
                if (!respTrabajadores.isSuccessful) throw IllegalStateException(RetrofitUtils.errorMessage(respTrabajadores))

                respClientes.body().orEmpty() to respTrabajadores.body().orEmpty()
            }

            clientes = listaClientes
            trabajadores = listaTrabajadores
            if (clienteSeleccionado == null) clienteSeleccionado = listaClientes.firstOrNull()
            if (trabajadorSeleccionado == null) trabajadorSeleccionado = listaTrabajadores.firstOrNull()
        } catch (t: Throwable) {
            error = t.message ?: "Error al cargar datos"
        } finally {
            cargandoDatos = false
        }
    }

    fun guardarTrabajo() {
        if (guardando) return
        error = null

        val cliente = clienteSeleccionado
        val trabajador = trabajadorSeleccionado
        if (!tituloValido || !fechaValida || cliente == null || trabajador == null) {
            error = "Revisa los campos obligatorios"
            return
        }

        val trabajo = Trabajo(
            id = null,
            titulo = titulo.trim(),
            descripcion = descripcion.trim().ifBlank { null },
            fechaProgramada = fechaProgramada.trim(),
            estado = estado,
            prioridad = prioridad,
            cliente = cliente,
            trabajador = trabajador,
        )

        guardando = true
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = RetrofitClient.apiService.crearTrabajo(trabajo).execute()
                    if (!response.isSuccessful) throw IllegalStateException(RetrofitUtils.errorMessage(response))
                }
                alGuardar()
            } catch (t: Throwable) {
                error = t.message ?: "No se pudo guardar la tarea"
            } finally {
                guardando = false
            }
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Nueva tarea", style = MaterialTheme.typography.titleLarge)

        if (error != null) {
            Text(
                text = error ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Título *") },
            enabled = !guardando,
            supportingText = { if (!tituloValido) Text("El título es obligatorio") },
            isError = !tituloValido,
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descripción") },
            enabled = !guardando,
            minLines = 2,
        )

        OutlinedTextField(
            value = fechaProgramada,
            onValueChange = { fechaProgramada = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Fecha programada *") },
            placeholder = { Text("yyyy-MM-dd") },
            enabled = !guardando,
            supportingText = { if (!fechaValida) Text("La fecha es obligatoria") },
            isError = !fechaValida,
        )

        // Selects simples (ciclo) para Estado/Prioridad.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = {
                    val valores = EstadoTrabajo.entries
                    val idx = valores.indexOf(estado)
                    estado = valores[(idx + 1) % valores.size]
                },
                enabled = !guardando,
            ) {
                Text("Estado: ${estado.name}")
            }

            OutlinedButton(
                onClick = {
                    val valores = PrioridadTrabajo.entries
                    val idx = valores.indexOf(prioridad)
                    prioridad = valores[(idx + 1) % valores.size]
                },
                enabled = !guardando,
            ) {
                Text("Prioridad: ${prioridad.name}")
            }
        }

        // Cliente / trabajador (selector simple por ciclo) evitando añadir dependencias extra.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = {
                    if (clientes.isEmpty()) return@OutlinedButton
                    val actual = clienteSeleccionado
                    val idx = clientes.indexOfFirst { it.id == actual?.id }
                    val siguiente = if (idx == -1) clientes.first() else clientes[(idx + 1) % clientes.size]
                    clienteSeleccionado = siguiente
                },
                enabled = !guardando && !cargandoDatos && clientes.isNotEmpty(),
            ) {
                Text("Cliente: ${clienteSeleccionado?.nombre ?: "(sin)"}")
            }

            OutlinedButton(
                onClick = {
                    if (trabajadores.isEmpty()) return@OutlinedButton
                    val actual = trabajadorSeleccionado
                    val idx = trabajadores.indexOfFirst { it.id == actual?.id }
                    val siguiente = if (idx == -1) trabajadores.first() else trabajadores[(idx + 1) % trabajadores.size]
                    trabajadorSeleccionado = siguiente
                },
                enabled = !guardando && !cargandoDatos && trabajadores.isNotEmpty(),
            ) {
                Text("Trabajador: ${trabajadorSeleccionado?.nombre ?: "(sin)"}")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = alCancelar, enabled = !guardando) {
                Text("Cancelar")
            }
            Button(
                onClick = { guardarTrabajo() },
                enabled = !guardando && !cargandoDatos && tituloValido && fechaValida && clienteSeleccionado != null && trabajadorSeleccionado != null,
            ) {
                Text(if (guardando) "Guardando…" else "Guardar")
            }
        }

        if (cargandoDatos) {
            Text(
                text = "Cargando clientes y trabajadores…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

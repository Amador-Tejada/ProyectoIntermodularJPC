package com.example.proyectointermodularjpc.Paginas.Trabajos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.CrearTrabajoRequest
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.EstadoTrabajo
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.PrioridadTrabajo
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitClient
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formulario para crear un nuevo trabajo.
 * - Botón "Cancelar" → vuelve a ListaTareas.
 * - Botón "Añadir tarea" → guarda en la BD mediante la API y vuelve a ListaTareas.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }

    var cargandoDatos by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val tituloValido = remember(titulo) { titulo.trim().isNotEmpty() }
    val fechaValida = remember(fechaProgramada) { fechaProgramada.trim().isNotEmpty() }

    // Carga inicial de clientes (el backend exige cliente NOT NULL).
    LaunchedEffect(Unit) {
        cargandoDatos = true
        error = null
        try {
            val lista = withContext(Dispatchers.IO) {
                val resp = RetrofitClient.apiService.getClientes().execute()
                if (!resp.isSuccessful) throw IllegalStateException(RetrofitUtils.errorMessage(resp))
                resp.body().orEmpty()
            }
            clientes = lista
            if (clienteSeleccionado == null) clienteSeleccionado = lista.firstOrNull()
        } catch (t: Throwable) {
            error = t.message ?: "Error al cargar clientes"
        } finally {
            cargandoDatos = false
        }
    }

    // ── Guardar trabajo ──────────────────────────────────────────────
    fun guardarTrabajo() {
        if (guardando) return
        error = null

        val cliente = clienteSeleccionado
        if (!tituloValido || !fechaValida || cliente?.id == null) {
            error = "Revisa los campos obligatorios"
            return
        }

        // El backend recibe un TrabajoDTO con clienteId (Long, @NotNull), NO un objeto cliente.
        // Los enums se serializan como PENDIENTE, MEDIA… (name()) porque el backend usa @Enumerated(EnumType.STRING).
        val trabajo = CrearTrabajoRequest(
            titulo = titulo.trim(),
            descripcion = descripcion.trim().ifBlank { null },
            fechaProgramada = fechaProgramada.trim(),
            estado = estado,
            prioridad = prioridad,
            clienteId = cliente.id,
        )

        guardando = true
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = RetrofitClient.apiService.crearTrabajo(trabajo).execute()
                    if (!response.isSuccessful)
                        throw IllegalStateException(RetrofitUtils.errorMessage(response))
                }
                alGuardar()
            } catch (t: Throwable) {
                error = t.message ?: "No se pudo guardar la tarea"
            } finally {
                guardando = false
            }
        }
    }

    // ── Estado de diálogos ───────────────────────────────────────────
    var mostrarSelectorCliente by remember { mutableStateOf(false) }
    var filtroCliente by remember { mutableStateOf("") }
    val clientesFiltrados = remember(clientes, filtroCliente) {
        val q = filtroCliente.trim()
        if (q.isEmpty()) clientes else clientes.filter { it.nombre.contains(q, ignoreCase = true) }
    }

    var mostrarSelectorFecha by remember { mutableStateOf(false) }
    val estadoDatePicker = rememberDatePickerState()
    val formateadorFecha = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // ── UI ───────────────────────────────────────────────────────────
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Nueva tarea", style = MaterialTheme.typography.titleLarge)

        if (error != null) {
            Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
        }

        // Título
        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Título *") },
            enabled = !guardando,
            isError = !tituloValido,
            supportingText = { if (!tituloValido) Text("El título es obligatorio") },
        )

        // Descripción
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descripción") },
            enabled = !guardando,
            minLines = 2,
        )

        // Fecha
        OutlinedButton(
            onClick = { mostrarSelectorFecha = true },
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (fechaProgramada.isBlank()) "Selecciona una fecha *" else "Fecha: $fechaProgramada")
        }
        if (!fechaValida) {
            Text("La fecha es obligatoria", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }

        // Estado y Prioridad
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = {
                    val v = EstadoTrabajo.entries
                    estado = v[(v.indexOf(estado) + 1) % v.size]
                },
                enabled = !guardando,
            ) { Text("Estado: ${estado.name}") }

            OutlinedButton(
                onClick = {
                    val v = PrioridadTrabajo.entries
                    prioridad = v[(v.indexOf(prioridad) + 1) % v.size]
                },
                enabled = !guardando,
            ) { Text("Prioridad: ${prioridad.name}") }
        }

        // Cliente
        OutlinedTextField(
            value = clienteSeleccionado?.nombre ?: "",
            onValueChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !guardando && !cargandoDatos && clientes.isNotEmpty()) {
                    filtroCliente = ""
                    mostrarSelectorCliente = true
                },
            readOnly = true,
            singleLine = true,
            label = { Text("Cliente *") },
            placeholder = { Text("Selecciona un cliente") },
            enabled = !guardando && !cargandoDatos,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Botones: Cancelar / Añadir tarea
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
                enabled = !guardando && !cargandoDatos && tituloValido && fechaValida && clienteSeleccionado?.id != null,
            ) {
                Text(if (guardando) "Guardando…" else "Añadir tarea")
            }
        }
    }

    // ── Diálogo: selector de cliente ─────────────────────────────────
    if (mostrarSelectorCliente) {
        AlertDialog(
            onDismissRequest = { mostrarSelectorCliente = false },
            title = { Text("Seleccionar cliente") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = filtroCliente,
                        onValueChange = { filtroCliente = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Buscar por nombre") },
                    )
                    if (clientesFiltrados.isEmpty()) {
                        Text("No hay clientes que coincidan.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn {
                            items(clientesFiltrados, key = { it.id ?: it.correoElectronico }) { c ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            clienteSeleccionado = c
                                            mostrarSelectorCliente = false
                                        }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) { Text(c.nombre, style = MaterialTheme.typography.bodyLarge) }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarSelectorCliente = false }) { Text("Cerrar") }
            },
        )
    }

    // ── Diálogo: selector de fecha ───────────────────────────────────
    if (mostrarSelectorFecha) {
        DatePickerDialog(
            onDismissRequest = { mostrarSelectorFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoDatePicker.selectedDateMillis?.let {
                        fechaProgramada = formateadorFecha.format(Date(it))
                    }
                    mostrarSelectorFecha = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarSelectorFecha = false }) { Text("Cancelar") }
            },
        ) { DatePicker(state = estadoDatePicker) }
    }
}

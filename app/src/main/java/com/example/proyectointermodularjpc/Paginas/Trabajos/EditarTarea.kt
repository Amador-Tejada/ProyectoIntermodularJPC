package com.example.proyectointermodularjpc.Paginas.Trabajos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.*
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitClient
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarTarea(
    trabajoId: Long,
    alFinalizar: () -> Unit,
    alCancelar: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaProgramada by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf(EstadoTrabajo.PENDIENTE) }
    var prioridad by remember { mutableStateOf(PrioridadTrabajo.MEDIA) }
    
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var trabajadorSeleccionado by remember { mutableStateOf<Trabajador?>(null) }
    var productosSeleccionados by remember { mutableStateOf<List<Producto>>(emptyList()) }
    
    var clientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var trabajadores by remember { mutableStateOf<List<Trabajador>>(emptyList()) }
    
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var mostrarDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(trabajoId) {
        try {
            val cResp = withContext(Dispatchers.IO) { RetrofitClient.apiService.getClientes().execute() }
            if (cResp.isSuccessful) clientes = cResp.body().orEmpty()
            
            val tResp = withContext(Dispatchers.IO) { RetrofitClient.apiService.getTrabajadores().execute() }
            if (tResp.isSuccessful) trabajadores = tResp.body().orEmpty()

            val jobResp = withContext(Dispatchers.IO) { RetrofitClient.apiService.getTrabajo(trabajoId).execute() }
            if (jobResp.isSuccessful) {
                val t = jobResp.body()!!
                titulo = t.titulo
                descripcion = t.descripcion ?: ""
                fechaProgramada = t.fechaProgramada
                estado = t.estado
                prioridad = t.prioridad
                clienteSeleccionado = t.cliente
                trabajadorSeleccionado = t.trabajador
                productosSeleccionados = t.productos ?: emptyList()
            } else {
                error = RetrofitUtils.errorMessage(jobResp)
            }
        } catch (e: Exception) {
            error = "Error al cargar datos"
        } finally {
            cargando = false
        }
    }

    fun guardar() {
        if (titulo.isBlank() || clienteSeleccionado == null || trabajadorSeleccionado == null) {
            error = "Título, Cliente y Trabajador son obligatorios"
            return
        }
        
        scope.launch {
            cargando = true
            try {
                // Usamos un mapa para enviar solo los campos necesarios y asegurar el formato esperado
                val campos = mutableMapOf<String, Any?>(
                    "titulo" to titulo,
                    "descripcion" to if (descripcion.isBlank()) null else descripcion,
                    "fechaProgramada" to fechaProgramada,
                    "estado" to estado.name,
                    "prioridad" to prioridad.name
                )
                
                // Enviamos solo los IDs para las relaciones, como sugiere la documentación del backend
                clienteSeleccionado?.id?.let { campos["cliente"] = mapOf("id" to it) }
                trabajadorSeleccionado?.id?.let { campos["trabajador"] = mapOf("id" to it) }
                campos["productos"] = productosSeleccionados

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.patchTrabajo(trabajoId, campos).execute()
                }
                if (response.isSuccessful) {
                    alFinalizar()
                } else {
                    error = RetrofitUtils.errorMessage(response)
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                cargando = false
            }
        }
    }

    if (cargando && titulo.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Editar Tarea",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            // Selector de Fecha con Calendario
            OutlinedTextField(
                value = fechaProgramada,
                onValueChange = { },
                readOnly = true,
                label = { Text("Fecha Programada") },
                trailingIcon = {
                    IconButton(onClick = { mostrarDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (mostrarDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { mostrarDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                fechaProgramada = formatter.format(Date(millis))
                            }
                            mostrarDatePicker = false
                        }) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarDatePicker = false }) {
                            Text("Cancelar")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            SelectorEnum("Estado", estado, EstadoTrabajo.entries) { estado = it }
            SelectorEnum("Prioridad", prioridad, PrioridadTrabajo.entries) { prioridad = it }

            var expandedCliente by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedCliente,
                onExpandedChange = { expandedCliente = !expandedCliente }
            ) {
                OutlinedTextField(
                    value = clienteSeleccionado?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cliente") },
                    placeholder = { Text("Seleccionar Cliente") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCliente) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedCliente,
                    onDismissRequest = { expandedCliente = false }
                ) {
                    if (clientes.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay clientes disponibles") },
                            onClick = { expandedCliente = false }
                        )
                    } else {
                        clientes.forEach { cliente ->
                            DropdownMenuItem(
                                text = { Text(cliente.nombre) },
                                onClick = {
                                    clienteSeleccionado = cliente
                                    expandedCliente = false
                                }
                            )
                        }
                    }
                }
            }

            var expandedTrabajador by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedTrabajador,
                onExpandedChange = { expandedTrabajador = !expandedTrabajador }
            ) {
                OutlinedTextField(
                    value = trabajadorSeleccionado?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Trabajador") },
                    placeholder = { Text("Seleccionar Trabajador") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTrabajador) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedTrabajador,
                    onDismissRequest = { expandedTrabajador = false }
                ) {
                    if (trabajadores.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay trabajadores disponibles") },
                            onClick = { expandedTrabajador = false }
                        )
                    } else {
                        trabajadores.forEach { trabajador ->
                            DropdownMenuItem(
                                text = { Text(trabajador.nombre) },
                                onClick = {
                                    trabajadorSeleccionado = trabajador
                                    expandedTrabajador = false
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { guardar() },
                    modifier = Modifier.weight(1f),
                    enabled = !cargando
                ) {
                    if (cargando) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    else Text("Guardar")
                }
                OutlinedButton(
                    onClick = alCancelar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T : Enum<T>> SelectorEnum(
    label: String,
    seleccionado: T,
    opciones: List<T>,
    alSeleccionar: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = seleccionado.name,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion.name) },
                    onClick = {
                        alSeleccionar(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}

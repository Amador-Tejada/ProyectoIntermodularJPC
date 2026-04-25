package com.example.proyectointermodularjpc.Paginas.Trabajos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevasTareas(
    alFinalizar: () -> Unit,
    alCancelar: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaProgramada by remember { mutableStateOf(LocalDate.now().toString()) }
    var estado by remember { mutableStateOf(EstadoTrabajo.PENDIENTE) }
    var prioridad by remember { mutableStateOf(PrioridadTrabajo.MEDIA) }
    
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var trabajadorSeleccionado by remember { mutableStateOf<Trabajador?>(null) }
    
    var clientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var trabajadores by remember { mutableStateOf<List<Trabajador>>(emptyList()) }
    
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val cResp = withContext(Dispatchers.IO) { RetrofitClient.apiService.getClientes().execute() }
            if (cResp.isSuccessful) clientes = cResp.body().orEmpty()
            
            val tResp = withContext(Dispatchers.IO) { RetrofitClient.apiService.getTrabajadores().execute() }
            if (tResp.isSuccessful) trabajadores = tResp.body().orEmpty()
        } catch (e: Exception) {
            error = "Error al cargar datos auxiliares"
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
                // Creamos los objetos solo con el ID para evitar problemas de recursividad o validación en el servidor
                val request = CrearTrabajoRequest(
                    titulo = titulo,
                    descripcion = if (descripcion.isBlank()) null else descripcion,
                    fechaProgramada = fechaProgramada,
                    estado = estado,
                    prioridad = prioridad,
                    cliente = clienteSeleccionado,
                    trabajador = trabajadorSeleccionado,
                    productos = emptyList()
                )
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.crearTrabajo(request).execute()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Nueva Tarea",
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

        OutlinedTextField(
            value = fechaProgramada,
            onValueChange = { fechaProgramada = it },
            label = { Text("Fecha (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Selectores de Enum
        SelectorEnum("Estado", estado, EstadoTrabajo.entries) { estado = it }
        SelectorEnum("Prioridad", prioridad, PrioridadTrabajo.entries) { prioridad = it }

        // Selector Cliente
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

        // Selector Trabajador
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

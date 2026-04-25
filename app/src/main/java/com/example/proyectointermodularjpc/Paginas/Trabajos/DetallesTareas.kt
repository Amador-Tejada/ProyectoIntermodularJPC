package com.example.proyectointermodularjpc.Paginas.Trabajos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Trabajo
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitClient
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DetallesTareas(
    trabajoId: Long,
    alEditar: () -> Unit,
    alVolver: () -> Unit
) {
    var trabajo by remember { mutableStateOf<Trabajo?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(trabajoId) {
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getTrabajo(trabajoId).execute()
            }
            if (response.isSuccessful) {
                trabajo = response.body()
            } else {
                error = RetrofitUtils.errorMessage(response)
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            cargando = false
        }
    }

    if (cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (error != null) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = alVolver) { Text("Volver") }
        }
    } else trabajo?.let { t ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = t.titulo,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider()
            
            ItemDetalle("Descripción", t.descripcion ?: "Sin descripción")
            ItemDetalle("Fecha Programada", t.fechaProgramada)
            ItemDetalle("Estado", t.estado.name)
            ItemDetalle("Prioridad", t.prioridad.name)
            ItemDetalle("Cliente", t.cliente?.nombre ?: "N/A")
            ItemDetalle("Trabajador Asignado", t.trabajador?.nombre ?: "No asignado")
            
            if (!t.productos.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Productos Relacionados:", style = MaterialTheme.typography.titleMedium)
                t.productos.forEach { producto ->
                    Text("• ${producto.nombre} (${producto.unidadMedida})", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = alEditar, modifier = Modifier.weight(1f)) {
                    Text("Editar")
                }
                OutlinedButton(onClick = alVolver, modifier = Modifier.weight(1f)) {
                    Text("Volver")
                }
            }
        }
    }
}

@Composable
fun ItemDetalle(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

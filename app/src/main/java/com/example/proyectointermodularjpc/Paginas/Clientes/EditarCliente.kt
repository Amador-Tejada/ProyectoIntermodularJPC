package com.example.proyectointermodularjpc.Paginas.Clientes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Cliente
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitClient
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarCliente(
    clienteId: Long,
    alFinalizar: () -> Unit,
    alCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    var cargando by remember { mutableStateOf(true) }
    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(clienteId) {
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getCliente(clienteId).execute()
            }
            if (response.isSuccessful) {
                response.body()?.let {
                    nombre = it.nombre
                    telefono = it.telefono
                    correo = it.correoElectronico
                    direccion = it.direccion
                }
            } else {
                error = RetrofitUtils.errorMessage(response)
            }
        } catch (e: Exception) {
            error = "Error al cargar el cliente"
        } finally {
            cargando = false
        }
    }

    fun guardar() {
        if (nombre.isBlank() || telefono.isBlank() || correo.isBlank()) {
            error = "Nombre, teléfono y correo son obligatorios"
            return
        }

        scope.launch {
            guardando = true
            error = null
            try {
                val clienteActualizado = Cliente(
                    id = clienteId,
                    nombre = nombre,
                    telefono = telefono,
                    correoElectronico = correo,
                    direccion = direccion
                )
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.actualizarCliente(clienteId, clienteActualizado).execute()
                }
                if (response.isSuccessful) {
                    alFinalizar()
                } else {
                    error = RetrofitUtils.errorMessage(response)
                }
            } catch (e: Exception) {
                error = "Error al guardar los cambios"
            } finally {
                guardando = false
            }
        }
    }

    if (cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Editar Cliente", style = MaterialTheme.typography.headlineMedium)

            if (error != null) {
                Text(text = error!!, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !guardando
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !guardando
            )

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !guardando
            )

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !guardando
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { guardar() },
                    modifier = Modifier.weight(1f),
                    enabled = !guardando
                ) {
                    if (guardando) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    else Text("Guardar")
                }
                OutlinedButton(
                    onClick = alCancelar,
                    modifier = Modifier.weight(1f),
                    enabled = !guardando
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

package com.example.proyectointermodularjpc.Paginas.Clientes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Cliente
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitClient
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/* Esta pantalla se abre al pulsar el boton de nuevo cliente de la pantalla de ListarClientes.
 * En ella tenemos un formulario para añadir un nuevo cliente a la DB mediante la Api.
 * Un boton para guardar los cambios y otro para cancelar. Ambos nos devolvera a la pantalla de ListaClientes.
 */

@Composable
fun FormularioNuevoCliente(
    modifier: Modifier = Modifier,
    alGuardar: () -> Unit,
    alCancelar: () -> Unit,
) {
    var nombre by remember { mutableStateOf("") }
    var correoElectronico by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    val nombreValido = remember(nombre) { nombre.trim().isNotEmpty() }
    val telefonoValido = remember(telefono) { telefono.trim().isNotEmpty() }
    val correoValido = remember(correoElectronico) { correoElectronico.trim().isNotEmpty() }

    var guardando by remember { mutableStateOf(false) }
    var errorGuardar by remember { mutableStateOf<String?>(null) }

    val scope = remember { CoroutineScope(Dispatchers.Main) }

    fun guardarEnApi() {
        if (guardando) return
        errorGuardar = null

        val cliente = Cliente(
            id = null,
            nombre = nombre.trim(),
            telefono = telefono.trim(),
            direccion = direccion.trim().ifBlank { null },
            correoElectronico = correoElectronico.trim(),
        )

        guardando = true
        scope.launch {
            val resultado = runCatching {
                withContext(Dispatchers.IO) {
                    val response = RetrofitClient.apiService.crearCliente(cliente).execute()
                    if (!response.isSuccessful) {
                        throw IllegalStateException(RetrofitUtils.errorMessage(response))
                    }
                }
            }

            guardando = false

            resultado.onSuccess {
                alGuardar()
            }.onFailure {
                errorGuardar = it.message ?: "${it::class.java.simpleName}: ${it.localizedMessage ?: "No se pudo guardar el cliente"}"
            }
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Nuevo cliente",
            style = MaterialTheme.typography.titleLarge,
        )

        if (errorGuardar != null) {
            Text(
                text = errorGuardar ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Nombre *") },
            placeholder = { Text("Ej: Juan Pérez") },
            supportingText = {
                if (!nombreValido) Text("El nombre es obligatorio")
            },
            isError = !nombreValido,
            enabled = !guardando,
        )

        OutlinedTextField(
            value = correoElectronico,
            onValueChange = { correoElectronico = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Correo electrónico *") },
            placeholder = { Text("ejemplo@correo.com") },
            supportingText = {
                if (!correoValido) Text("El correo electrónico es obligatorio")
            },
            isError = !correoValido,
            enabled = !guardando,
        )

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Teléfono *") },
            placeholder = { Text("600123123") },
            supportingText = {
                if (!telefonoValido) Text("El teléfono es obligatorio")
            },
            isError = !telefonoValido,
            enabled = !guardando,
        )

        OutlinedTextField(
            value = direccion,
            onValueChange = { direccion = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Dirección") },
            placeholder = { Text("C/ Mayor 1") },
            enabled = !guardando,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = alCancelar,
                enabled = !guardando,
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = { guardarEnApi() },
                enabled = !guardando && nombreValido && telefonoValido && correoValido,
            ) {
                Text(if (guardando) "Guardando…" else "Guardar")
            }
        }
    }
}

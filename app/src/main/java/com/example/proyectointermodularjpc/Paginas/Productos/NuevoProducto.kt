package com.example.proyectointermodularjpc.Paginas.Productos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Producto
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitClient
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote.RetrofitUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/* Pantalla formulario que permite crear un nuevo producto y que tiene
 * 2 botones, guardar en la DB y cancelar.
 */

@Composable
fun NuevoProducto(
    modifier: Modifier = Modifier,
    alGuardado: () -> Unit,
    alCancelar: () -> Unit,
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var unidadMedida by remember { mutableStateOf("") }

    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val nombreValido = remember(nombre) { nombre.trim().isNotEmpty() }
    val unidadValida = remember(unidadMedida) { unidadMedida.trim().isNotEmpty() }

    fun guardarProducto() {
        if (guardando) return
        error = null

        if (!nombreValido || !unidadValida) {
            error = "Revisa los campos obligatorios"
            return
        }

        val producto = Producto(
            id = null, // La BD debería generar el id.
            nombre = nombre.trim(),
            descripcion = descripcion.trim().ifBlank { null },
            unidadMedida = unidadMedida.trim(),
        )

        guardando = true
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = RetrofitClient.apiService.crearProducto(producto).execute()
                    if (!response.isSuccessful) {
                        throw IllegalStateException(RetrofitUtils.errorMessage(response))
                    }
                }
                alGuardado()
            } catch (t: Throwable) {
                error = t.message
                    ?: "${t::class.java.simpleName}: ${t.localizedMessage ?: "No se pudo guardar el producto"}"
            } finally {
                guardando = false
            }
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Nuevo producto",
            style = MaterialTheme.typography.titleLarge,
        )

        if (error != null) {
            Text(
                text = error ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !guardando,
            label = { Text("Nombre *") },
            placeholder = { Text("Ej: Tornillo") },
            supportingText = {
                if (!nombreValido) Text("El nombre es obligatorio")
            },
            isError = !nombreValido,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando,
            label = { Text("Descripción") },
            placeholder = { Text("Descripción del producto") },
            minLines = 2,
        )

        OutlinedTextField(
            value = unidadMedida,
            onValueChange = { unidadMedida = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !guardando,
            label = { Text("Unidad de medida *") },
            placeholder = { Text("Ej: ud, m, kg, L") },
            supportingText = {
                if (!unidadValida) Text("La unidad de medida es obligatoria")
            },
            isError = !unidadValida,
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
                onClick = { guardarProducto() },
                enabled = !guardando && nombreValido && unidadValida,
            ) {
                Text(if (guardando) "Guardando…" else "Añadir")
            }
        }
    }
}

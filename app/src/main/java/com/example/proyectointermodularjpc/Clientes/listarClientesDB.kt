package com.example.proyectointermodularjpc.Clientes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SearchScreen() {
    val query = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "Listado de Clientes") // AQUI VA LA PROGRAMACION DEL TITULO DE LA PANTALLA

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AQUI VA LA PROGRAMACION DEL TEXTFIELD:
            // Ej: validaciones, debounce, formato, bindings con ViewModel, etc.
            TextField(
                value = query.value,
                onValueChange = { new -> query.value = new /* AQUI VA LA PROGRAMACION DEL onValueChange: actualizar estado / ViewModel */ },
                placeholder = { /* AQUI VA LA PROGRAMACION DEL placeholder: muestra texto guía si lo deseas */ },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // AQUI VA LA PROGRAMACION DEL BOTON:
            // Ej: acción de búsqueda, navegación, mostrar carga, llamadas a repositorio/ViewModel.
            Button(
                onClick = {
                    // AQUI VA LA PROGRAMACION DEL onClick: ejecutar búsqueda usando `query.value`
                },
                modifier = Modifier.height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar" // accesibilidad; no es texto visible en UI
                )
                Spacer(modifier = Modifier.width(8.dp))
                // AQUI VA LA PROGRAMACION DEL LABEL_DEL_BOTON: si quieres mostrar texto junto al ícono
                Text(text = "")
            }
        }
    }
}

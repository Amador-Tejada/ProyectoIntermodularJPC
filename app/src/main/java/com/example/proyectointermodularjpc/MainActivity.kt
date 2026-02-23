package com.example.proyectointermodularjpc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.example.proyectointermodularjpc.Menu.AppConMenuHamburguesa

/*
 * En el main tenemos reflejado el menu hamburguesa para navegar entre pantallas.
 * Establecemos como principal la pantalla de calendario y las demas podremos acceder a ellas
 * desde el menu hamburguesa.
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                AppConMenuHamburguesa(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

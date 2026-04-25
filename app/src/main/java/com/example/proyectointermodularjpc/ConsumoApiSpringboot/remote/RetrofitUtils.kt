package com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote

import retrofit2.Response

object RetrofitUtils {
    /**
     * Extrae un mensaje de error legible de una respuesta de Retrofit que no fue exitosa.
     */
    fun errorMessage(response: Response<*>): String {
        val errorBody = response.errorBody()?.string()
        return if (!errorBody.isNullOrBlank()) {
            // Aquí podrías parsear el JSON si el backend devuelve un objeto de error (ej. { "message": "..." })
            // Por ahora devolvemos el body tal cual o el mensaje de la respuesta.
            errorBody
        } else {
            response.message().ifBlank { "Error desconocido (Código: ${response.code()})" }
        }
    }
}

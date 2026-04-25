package com.example.proyectointermodularjpc.ConsumoApiSpringboot.model

import com.google.gson.annotations.SerializedName

data class Inventario(
    val id: Long? = null,
    @SerializedName(value = "cantidadDisponible", alternate = ["cantidad_disponible"])
    val cantidadDisponible: Double,
    val producto: Producto? = null
)

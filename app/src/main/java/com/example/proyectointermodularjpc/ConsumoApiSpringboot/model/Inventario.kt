package com.example.proyectointermodularjpc.ConsumoApiSpringboot.model

data class Inventario(
    val id: Long?,
    val cantidadDisponible: Double,
    val producto: Producto?
)
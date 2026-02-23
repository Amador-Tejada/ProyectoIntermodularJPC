package com.example.proyectointermodularjpc.ConsumoApiSpringboot.model


data class Producto(
    val id: Long?,
    val nombre: String,
    val descripcion: String?,
    val unidadMedida: String
)
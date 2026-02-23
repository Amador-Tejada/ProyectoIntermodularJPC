package com.example.proyectointermodularjpc.ConsumoApiSpringboot.model


data class Cliente(
    val id: Long?,
    val nombre: String,
    val telefono: String,
    val direccion: String?,
    val correoElectronico: String
)
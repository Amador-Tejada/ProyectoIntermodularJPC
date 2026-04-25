package com.example.proyectointermodularjpc.ConsumoApiSpringboot.model

import com.google.gson.annotations.SerializedName

data class Cliente(
    val id: Long? = null,
    val nombre: String,
    val telefono: String,
    val direccion: String,
    @SerializedName(value = "correoElectronico", alternate = ["correo_electronico"])
    val correoElectronico: String
)

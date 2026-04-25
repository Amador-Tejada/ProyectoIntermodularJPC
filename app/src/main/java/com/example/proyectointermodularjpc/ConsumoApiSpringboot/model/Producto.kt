package com.example.proyectointermodularjpc.ConsumoApiSpringboot.model

import com.google.gson.annotations.SerializedName

data class Producto(
    val id: Long? = null,
    val nombre: String,
    val descripcion: String?,
    @SerializedName(value = "unidadMedida", alternate = ["unidad_medida"])
    val unidadMedida: String
)

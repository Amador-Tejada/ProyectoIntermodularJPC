package com.example.proyectointermodularjpc.ConsumoApiSpringboot.model

import com.google.gson.annotations.SerializedName

data class Trabajo(
    val id: Long? = null,
    val titulo: String,
    val descripcion: String?,
    @SerializedName(value = "fecha_programada", alternate = ["fechaProgramada"])
    val fechaProgramada: String,
    val estado: EstadoTrabajo,
    val prioridad: PrioridadTrabajo,
    val cliente: Cliente?,
    val trabajador: Trabajador? = null,
    val productos: List<Producto>? = emptyList()
)

data class CrearTrabajoRequest(
    val titulo: String,
    val descripcion: String?,
    @SerializedName(value = "fecha_programada", alternate = ["fechaProgramada"])
    val fechaProgramada: String,
    val estado: EstadoTrabajo,
    val prioridad: PrioridadTrabajo,
    val cliente: Cliente?,
    val trabajador: Trabajador?,
    val productos: List<Producto> = emptyList()
)

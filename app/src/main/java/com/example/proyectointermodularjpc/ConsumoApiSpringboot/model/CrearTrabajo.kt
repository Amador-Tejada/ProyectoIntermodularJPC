package com.example.proyectointermodularjpc.ConsumoApiSpringboot.model

data class CrearTrabajoRequest(
    val titulo: String,
    val descripcion: String?,
    val fechaProgramada: String,
    val estado: EstadoTrabajo,
    val prioridad: PrioridadTrabajo,
    val cliente: Cliente,
    val trabajador: Trabajador
)
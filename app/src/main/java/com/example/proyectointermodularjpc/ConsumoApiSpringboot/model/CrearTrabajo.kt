package com.example.proyectointermodularjpc.ConsumoApiSpringboot.model

/**
 * DTO que coincide con TrabajoDTO del backend.
 * El controller espera: clienteId (Long, @NotNull) y trabajadorId (Long?, opcional).
 */
data class CrearTrabajoRequest(
    val titulo: String,
    val descripcion: String?,
    val fechaProgramada: String,
    val estado: EstadoTrabajo,
    val prioridad: PrioridadTrabajo,
    val clienteId: Long,
    val trabajadorId: Long? = null,
)
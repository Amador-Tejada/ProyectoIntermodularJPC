package com.example.proyectointermodularjpc.ConsumoApiSpringboot.model

/**
 * Modelo de Trabajo para el consumo de la API.
 *
 * NOTA: En el backend, fechaProgramada es LocalDate, por lo que se envía/recibe como String con formato yyyy-MM-dd.
 */
data class Trabajo(
    val id: Long? = null,
    val titulo: String,
    val descripcion: String?,
    /** yyyy-MM-dd */
    val fechaProgramada: String,
    val estado: EstadoTrabajo,
    val prioridad: PrioridadTrabajo,
    val cliente: Cliente,
    /** Puede no enviarse al crear el trabajo. */
    val trabajador: Trabajador? = null,
)
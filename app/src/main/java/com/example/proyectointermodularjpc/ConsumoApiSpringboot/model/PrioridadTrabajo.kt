package com.example.proyectointermodularjpc.ConsumoApiSpringboot.model

/**
 * Debe coincidir con el enum del backend.
 * @Enumerated(EnumType.STRING) guarda el .name() → BAJA, MEDIA, ALTA, URGENTE.
 */
enum class PrioridadTrabajo {
    BAJA,
    MEDIA,
    ALTA,
    URGENTE
}
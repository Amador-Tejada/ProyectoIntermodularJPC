package com.example.proyectointermodularjpc.ConsumoApiSpringboot.model

/**
 * Debe coincidir con el enum del backend.
 * @Enumerated(EnumType.STRING) guarda el .name() → PENDIENTE, EN_PROCESO, etc.
 */
enum class EstadoTrabajo {
    PENDIENTE,
    EN_PROCESO,
    COMPLETADO,
    CANCELADO
}
package com.example.proyectointermodularjpc.DatosDB


data class Trabajo(
    var id: String = "",
    var titulo: String = "",
    var descripcion: String = "",
    var fechaProgramada: Long = 0L,
    var prioridad: String = "",
    var clienteId: String = ""
)

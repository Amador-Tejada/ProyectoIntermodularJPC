package com.example.proyectointermodularjpc.ConsumoApiSpringboot.remote

import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Cliente
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Inventario
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Producto
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Trabajador
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Trabajo
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.CrearTrabajoRequest
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // CLIENTES

    @GET("api/clientes")
    fun getClientes(): Call<List<Cliente>>

    @GET("api/clientes/{id}")
    fun getCliente(@Path("id") id: Long): Call<Cliente>

    @POST("api/clientes")
    fun crearCliente(@Body cliente: Cliente): Call<Cliente>

    @PUT("api/clientes/{id}")
    fun actualizarCliente(
        @Path("id") id: Long,
        @Body cliente: Cliente
    ): Call<Cliente>

    @DELETE("api/clientes/{id}")
    fun eliminarCliente(@Path("id") id: Long): Call<Void>


    // TRABAJADORES

    @GET("api/trabajadores")
    fun getTrabajadores(): Call<List<Trabajador>>

    @POST("api/trabajadores")
    fun crearTrabajador(@Body trabajador: Trabajador): Call<Trabajador>

    @DELETE("api/trabajadores/{id}")
    fun eliminarTrabajador(@Path("id") id: Long): Call<Void>


    // PRODUCTOS

    @GET("api/productos")
    fun getProductos(): Call<List<Producto>>

    @GET("api/productos/{id}")
    fun getProducto(@Path("id") id: Long): Call<Producto>

    @POST("api/productos")
    fun crearProducto(@Body producto: Producto): Call<Producto>

    @PUT("api/productos/{id}")
    fun actualizarProducto(
        @Path("id") id: Long,
        @Body producto: Producto
    ): Call<Producto>

    @DELETE("api/productos/{id}")
    fun eliminarProducto(@Path("id") id: Long): Call<Void>


    // TRABAJOS

    @GET("api/trabajos")
    fun getTrabajos(): Call<List<Trabajo>>

    @GET("api/trabajos/{id}")
    fun getTrabajo(@Path("id") id: Long): Call<Trabajo>

    @POST("api/trabajos")
    fun crearTrabajo(@Body trabajo: CrearTrabajoRequest): Call<Trabajo>

    @PUT("api/trabajos/{id}")
    fun actualizarTrabajo(
        @Path("id") id: Long,
        @Body trabajo: Trabajo
    ): Call<Trabajo>

    @DELETE("api/trabajos/{id}")
    fun eliminarTrabajo(@Path("id") id: Long): Call<Void>


    // INVENTARIO

    @GET("api/inventarios")
    fun getInventario(): Call<List<Inventario>>

    @PUT("api/inventarios/{id}")
    fun actualizarInventario(
        @Path("id") id: Long,
        @Body inventario: Inventario
    ): Call<Inventario>
}
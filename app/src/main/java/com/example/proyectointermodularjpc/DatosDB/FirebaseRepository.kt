package com.example.proyectointermodularjpc.DatosDB

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction

/**
 * Repositorio sencillo para Realtime Database que:
 * - Mantiene contadores atómicos para generar IDs numéricos incrementales.
 * - Crea entidades `Trabajo` y `Producto` usando esos IDs.
 * - Crea automáticamente la relación `TareaProducto` (tarea-producto) cuando se añade
 *   un producto a un trabajo.
 *
 * Notas:
 * - Este enfoque usa un nodo `counters/{entity}` en la base de datos para llevar
 *   el contador y utiliza `runTransaction` para incrementarlo de forma segura.
 * - Las funciones usan callbacks (`onComplete`) para notificar éxito/fracaso y
 *   devolver el id generado cuando procede.
 */
class FirebaseRepository {
    private val database = FirebaseDatabase.getInstance()
    private val rootRef = database.reference
    private val countersRef = rootRef.child("counters")

    /** Genera un ID incremental atómico para la entidad indicada. */
    private fun generateIncrementalId(entityName: String, callback: (success: Boolean, newId: String?) -> Unit) {
        val counterRef = countersRef.child(entityName)
        counterRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val current = when (val v = currentData.getValue(Long::class.java)) {
                    null -> 0L
                    is Long -> v
                    is Int -> (v as Int).toLong()
                    else -> 0L
                }
                currentData.value = current + 1L
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null || !committed) {
                    callback(false, null)
                    return
                }
                val newId = currentData?.value?.toString()
                callback(true, newId)
            }
        })
    }

    /** Añade un Trabajo con ID incremental. */
    fun addTrabajo(trabajo: Trabajo, onComplete: (success: Boolean, id: String?) -> Unit) {
        generateIncrementalId("trabajos") { ok, newId ->
            if (!ok || newId == null) {
                onComplete(false, null)
                return@generateIncrementalId
            }
            trabajo.id = newId
            rootRef.child("trabajos").child(newId).setValue(trabajo)
                .addOnCompleteListener { task ->
                    onComplete(task.isSuccessful, if (task.isSuccessful) newId else null)
                }
        }
    }

    /** Añade un Producto con ID incremental. */
    fun addProducto(producto: Producto, onComplete: (success: Boolean, id: String?) -> Unit) {
        generateIncrementalId("productos") { ok, newId ->
            if (!ok || newId == null) {
                onComplete(false, null)
                return@generateIncrementalId
            }
            producto.id = newId
            rootRef.child("productos").child(newId).setValue(producto)
                .addOnCompleteListener { task ->
                    onComplete(task.isSuccessful, if (task.isSuccessful) newId else null)
                }
        }
    }

    /** Crea la relación tarea-producto (tabla de unión) usando claves compuestas. */
    fun createRelacionTareaProducto(tareaId: String, productoId: String, cantidad: Int = 1, onComplete: (success: Boolean) -> Unit) {
        val key = "${tareaId}_$productoId"
        val relacion = TareaProducto(tareaId = tareaId, productoId = productoId, cantidad = cantidad)
        rootRef.child("tareas_productos").child(key).setValue(relacion)
            .addOnCompleteListener { task -> onComplete(task.isSuccessful) }
    }

    /** Añade un producto y automáticamente crea la relación con el trabajo indicado. */
    fun addProductoToTrabajo(trabajoId: String, producto: Producto, cantidad: Int = 1, onComplete: (success: Boolean, productoId: String?) -> Unit) {
        // Primero crear el producto
        addProducto(producto) { ok, productoId ->
            if (!ok || productoId == null) {
                onComplete(false, null)
                return@addProducto
            }
            // Luego crear la relación tarea-producto
            createRelacionTareaProducto(trabajoId, productoId, cantidad) { relOk ->
                onComplete(relOk, if (relOk) productoId else null)
            }
        }
    }

    /** Ejemplo: eliminar la relación tarea-producto si se necesita. */
    fun removeRelacionTareaProducto(tareaId: String, productoId: String, onComplete: (success: Boolean) -> Unit) {
        val key = "${tareaId}_$productoId"
        rootRef.child("tareas_productos").child(key).removeValue()
            .addOnCompleteListener { task -> onComplete(task.isSuccessful) }
    }
}


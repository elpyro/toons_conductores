package com.castellanoseloy.toonsconductor.proveedor

import android.util.Log
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.modelos.RecargasModelo
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.optic.uberclonedriverkotlin.providers.DriverProvider

class RecargasProvider {

    private val db = FirebaseDatabase.getInstance().reference.child("Recargas")

    fun create(recarga: RecargasModelo): Task<Void> {
        return db.child(recarga.id!!).setValue(recarga).addOnFailureListener {
            Log.d("REALTIME_DB", "ERROR: ${it.message}")
        }
    }

    fun deleteRecargaPorId(idRecarga: String): Task<Void> {
        val referenciaRecarga = db.child(idRecarga)
        return referenciaRecarga.removeValue()
    }


    fun buscarPorIdDriver(idDriver: String, callback: (List<RecargasModelo>) -> Unit) {
        // Crea una consulta que filtre por el campo "idDriver"
        val consulta = db.orderByChild("idDriver").equalTo(idDriver)

        consulta.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recargasList = mutableListOf<RecargasModelo>()

                for (recargaSnapshot in snapshot.children) {
                    val recarga = recargaSnapshot.getValue(RecargasModelo::class.java)
                    recarga?.let { recargasList.add(it) }
                }
                callback(recargasList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores si es necesario
                println("Error en la consulta: ${error.message}")
                callback(emptyList())
            }
        })
    }


}
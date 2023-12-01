package com.castellanoseloy.toonsconductor.proveedor

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.optic.uberclonedriverkotlin.models.History

class HistoriesCancelProvider {

    val db = Firebase.firestore.collection("HistoriesCancels")

    fun create(history: History): Task<Void> {
        val documentReference = db.document(history.id!!)

        return documentReference.set(history)
            .addOnSuccessListener {
                Log.d("FIRESTORE", "Documento creado con ID personalizado: $history.id!!")
            }
            .addOnFailureListener { exception ->
                Log.d("FIRESTORE", "ERROR: ${exception.message}")
            }
    }

    fun getHistoryCancelById(id: String): Task<DocumentSnapshot> {
        return db.document(id).get()
            .addOnFailureListener { exception ->
                // Manejar el error aquí
                Log.e("Viaje cancelado", "Error al obtener el documento con ID $id", exception)
            }
    }

    fun updateComentarioCancelado(id: String,  comment: String): Task<Void> {
        val data = hashMapOf(
            "commentDriver" to comment
        )
        return db.document(id).update(data as Map<String, Any>).addOnFailureListener { exception ->
            Log.d("FIRESTORE", "ERROR: ${exception.message}")
        }
    }

}
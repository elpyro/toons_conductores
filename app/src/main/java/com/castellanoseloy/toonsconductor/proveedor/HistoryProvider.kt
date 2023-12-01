package com.optic.uberclonedriverkotlin.providers

import android.util.Log
import com.castellanoseloy.toonsconductor.proveedor.Autenticacion
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.optic.uberclonedriverkotlin.models.History

class HistoryProvider {

    val db = Firebase.firestore.collection("Histories")
    val authProvider = Autenticacion()

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

    fun getHistoryById(id: String): Task<DocumentSnapshot> {
        return db.document(id).get()
    }


    fun getLastHistory(): Query { // CONSULTA COMPUESTA - INDICE
        return db.whereEqualTo("idDriver", authProvider.obtenerId()).orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
    }

    fun getHistories(): Query { // CONSULTA COMPUESTA - INDICE
        return db.whereEqualTo("idDriver", authProvider.obtenerId()).orderBy("timestamp", Query.Direction.DESCENDING)
    }

    fun getHistoriesClient(idCliente:String): Query { // CONSULTA COMPUESTA - INDICE
        return db.whereEqualTo("idClient", idCliente)
    }


    fun getBooking(): Query {
        return db.whereEqualTo("idDriver", authProvider.obtenerId())
    }

    fun updateCalificationToClient(id: String, calification: Float, comment: String): Task<Void> {
        val data = hashMapOf(
            "calificationToClient" to calification,
            "commentDriver" to comment
        )
        return db.document(id).update(data as Map<String, Any>).addOnFailureListener { exception ->
            Log.d("FIRESTORE", "ERROR: ${exception.message}")
        }
    }

}
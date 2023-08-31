package com.castellanoseloy.toonsconductor.proveedor

import com.castellanoseloy.toonsconductor.modelos.ConductorModelo

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ClienteProveedor {

    val bd =Firebase.firestore.collection("Conductores")

    fun crear(cliente: ConductorModelo):Task<Void>{
        return bd.document(cliente.id!!).set(cliente)
    }

    fun getClientById(id: String): Task<DocumentSnapshot> {
        return bd.document(id).get()
    }

}
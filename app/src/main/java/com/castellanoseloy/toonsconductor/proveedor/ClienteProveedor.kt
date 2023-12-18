package com.castellanoseloy.toonsconductor.proveedor

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ClienteProveedor {

    val bd =Firebase.firestore.collection("Clientes")
    fun getClientById(id: String): Task<DocumentSnapshot> {
        return bd.document(id).get()
    }

}
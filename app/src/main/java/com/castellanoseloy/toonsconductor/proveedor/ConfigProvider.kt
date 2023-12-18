package com.castellanoseloy.toonsconductor.proveedor

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ConfigProvider {
    val db = Firebase.firestore.collection("Config")

    fun informacionWhatsApp(): Task<DocumentSnapshot> {
        return db.document("numerosWhatsApp").get().addOnFailureListener { exception ->
            Log.d("FIREBASE", "Error: ${exception.message}")
        }
    }

}
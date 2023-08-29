package com.optic.uberclonedriverkotlin.providers

import android.net.Uri
import android.util.Log
import com.castellanoseloy.toonsconductor.modelos.ConductorModelo
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask

import java.io.File

class DriverProvider {

    companion object {
        var datosConductor: ConductorModelo ? =null
    }


    val db = Firebase.firestore.collection("Conductores")
    var storage = FirebaseStorage.getInstance().getReference().child("profile")

    fun create(driver: ConductorModelo): Task<Void> {
        return db.document(driver.id!!).set(driver)
    }

    fun uploadImage(id: String, file: File): StorageTask<UploadTask.TaskSnapshot> {
        var fromFile = Uri.fromFile(file)
        val ref = storage.child("$id.jpg")
        storage = ref
        val uploadTask = ref.putFile(fromFile)

        return uploadTask.addOnFailureListener {
            Log.d("STORAGE", "ERROR: ${it.message}")
        }
    }

//    fun createToken(idDriver: String) {
//        FirebaseMessaging.getInstance().token.addOnCompleteListener {
//            if (it.isSuccessful) {
//                val token = it.result // TOKEN DE NOTIFICACIONES
//                updateToken(idDriver, token)
//            }
//        }
//    }

    fun updateToken(idDriver: String, token: String): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["token"] = token
        return db.document(idDriver).update(map)
    }

    fun getImageUrl(): Task<Uri> {
        return storage.downloadUrl
    }

    fun getDriver(idDriver: String): Task<DocumentSnapshot> {
        return db.document(idDriver).get()
    }

    fun update(driver: ConductorModelo): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["nombre"] = driver?.nombre!!
        map["apellido"] = driver?.apellido!!
        map["telefono"] = driver?.telefono!!
        map["validacion"] = driver?.validacion!!
        map["imagen"] = driver?.imagen!!
        return db.document(driver?.id!!).update(map)
    }


}
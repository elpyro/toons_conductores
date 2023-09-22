package com.optic.uberclonedriverkotlin.providers

import android.net.Uri
import android.util.Log
import com.castellanoseloy.toonsconductor.modelos.ConductorModelo
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class DriverProvider {

    companion object {
        var datosConductor: ConductorModelo ? =null

    }


    private lateinit var storageLicencia: StorageReference
    private lateinit var storageDocumento: StorageReference
    private lateinit var storageFotoPerfil: StorageReference
    val db = Firebase.firestore.collection("Conductores")
    var storage = FirebaseStorage.getInstance().getReference().child("profile")

    fun create(driver: ConductorModelo): Task<Void> {
        return db.document(driver.id!!).set(driver)
    }

    fun createToken(idDriver: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                val token = it.result // TOKEN DE NOTIFICACIONES
                updateToken(idDriver, token)
            }
        }
    }

    fun updateToken(idDriver: String, token: String): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["token"] = token
        return db.document(idDriver).update(map)
    }

    fun uploadImage(id: String, nombreImagen: String, file: File): StorageTask<UploadTask.TaskSnapshot> {
        var fromFile = Uri.fromFile(file)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val ref = storage.child("$id/$nombreImagen-$timeStamp.jpg")

        if (nombreImagen.equals("fotoPerfil"))storageFotoPerfil = ref
        if (nombreImagen.equals("documento"))storageDocumento = ref
        if (nombreImagen.equals("licencia"))storageLicencia = ref

        val uploadTask = ref.putFile(fromFile)

        return uploadTask.addOnFailureListener {
            Log.d("STORAGE", "ERROR: ${it.message}")
        }
    }



    fun getStorageFotoPerfil(): Task<Uri> {
        return storageFotoPerfil?.downloadUrl!!
    }
    fun getStorageDocumento(): Task<Uri> {
        return storageDocumento?.downloadUrl!!
    }
    fun getStorageLicencia(): Task<Uri> {
        return storageLicencia?.downloadUrl!!
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
        return db.document(driver?.id!!).update(map)
    }
    fun updateCar(idCar:String, ): Task<Void>{
        val map: MutableMap<String, Any> = HashMap()
        map["vehiculo"] = idCar
        return db.document(datosConductor?.id!!).update(map)
    }

    fun updateFotoPerfil(driver: ConductorModelo): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["imagenPerfil"] = driver?.imagenPerfil!!
        return db.document(driver?.id!!).update(map)
    }

    fun updateDocumento(driver: ConductorModelo): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["imagenDocumento"] = driver?.imagenDocumento!!
        return db.document(driver?.id!!).update(map)
    }

    fun updateLicencia(driver: ConductorModelo): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["imagenLicencia"] = driver?.imagenLicencia!!
        return db.document(driver?.id!!).update(map)
    }


}
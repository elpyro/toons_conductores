package com.castellanoseloy.toonsconductor.proveedor

import android.net.Uri
import android.util.Log
import com.castellanoseloy.toonsconductor.modelos.CarModel

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class CarProvider {

    companion object {
        var datosCarroEnUso: CarModel? =null
    }
    val bd =Firebase.firestore.collection("Cars")
    private lateinit var storageFotoFrontal: StorageReference
    private lateinit var storageLateral: StorageReference
    private lateinit var storageDocumento: StorageReference
    private lateinit var storageDocumentoTrasero: StorageReference
    private var storage = FirebaseStorage.getInstance().getReference().child("Cars")


    fun getCarByDriverId(driverId: String): Task<QuerySnapshot> {
        // Realizar una consulta para encontrar carros con el campo "idDriver" igual a driverId
        return bd.whereEqualTo("idDriver", driverId).get()
    }

    fun getCarById(id: String): Task<DocumentSnapshot> {
        return bd.document(id).get()
    }


    fun create(car: CarModel): Task<Void> {
        val carData: MutableMap<String, Any> = HashMap()
        carData["id"] = car.id!!
        carData["estado"] = car.estado!!
        carData["idDriver"] = car.idDriver!!
        carData["bland"] = car.bland!!
        carData["tipo"] = car.tipo!!
        carData["placa"] = car.placa!!
        carData["color"] = car.color!!
        carData["pasajeros"] = car.pasajeros!!
        carData["malero"] = car.malero!!
        carData["tecnomecanica"] = car.tecnomecanica!!
        carData["SOAT"] = car.SOAT!!

        return bd.document(car.id!!).update(carData)
    }

    fun uploadImage(id: String, nombreImagen: String, file: File): StorageTask<UploadTask.TaskSnapshot> {
        var fromFile = Uri.fromFile(file)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val ref = storage.child("$id/$nombreImagen-$timeStamp.jpg")

        if (nombreImagen.equals("fotoFrontal")) storageFotoFrontal= ref
        if (nombreImagen.equals("fotoLateral"))storageLateral = ref
        if (nombreImagen.equals("documentoVehiculo_frontal"))storageDocumento = ref
        if (nombreImagen.equals("documentoVehiculo_trasero"))storageDocumentoTrasero = ref


        val uploadTask = ref.putFile(fromFile)

        return uploadTask.addOnFailureListener {
            Log.d("STORAGE", "ERROR: ${it.message}")
        }
    }

    fun getStorageImagenFrontal(): Task<Uri> {
        return storageFotoFrontal?.downloadUrl!!
    }
    fun getStorageImagenLateral(): Task<Uri> {
        return storageLateral?.downloadUrl!!
    }
    fun getStorageDocumentoFrontal(): Task<Uri> {
        return storageDocumento?.downloadUrl!!
    }
    fun getStorageDocumentoTrasero(): Task<Uri> {
        return storageDocumentoTrasero?.downloadUrl!!
    }
    fun updateImagenFrontal(car: CarModel): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["imagenFrontal"] = car?.imagenFrontal!!
        return bd.document(car?.id!!).update(map)
    }

    fun updateImagenLateral(car: CarModel): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["imagenLateral"] = car?.imagenLateral!!
        return bd.document(car?.id!!).update(map)
    }

    fun updateDocumento(car: CarModel): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["imagenDocumento"] = car?.imagenDocumento!!
        return bd.document(car?.id!!).update(map)
    }
    fun updateDocumentoTrasero(car: CarModel): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["imagenDocumentoTrasero"] = car?.imagenDocumentoTrasero!!
        return bd.document(car?.id!!).update(map)
    }

}
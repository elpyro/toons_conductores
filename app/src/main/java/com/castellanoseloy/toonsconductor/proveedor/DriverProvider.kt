package com.optic.uberclonedriverkotlin.providers

import android.net.Uri
import android.util.Log
import com.castellanoseloy.toonsconductor.modelos.ConductorModelo
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
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

    private lateinit var storageDocumento: StorageReference
    private lateinit var storageDocumentoTrasero: StorageReference
    private lateinit var storageFotoPerfil: StorageReference
    private lateinit var storageLicenciaDelantera: StorageReference
    private lateinit var storageLicenciaTrasera: StorageReference

    val db = Firebase.firestore.collection("Conductores")
    var storage = FirebaseStorage.getInstance().getReference().child("profile")


    fun crearOActualizar(driver:ConductorModelo):Task<Void>{
        val documentReference = db.document(driver.id!!)
        return documentReference.set(driver,  SetOptions.merge())
    }

    fun getConductorPorTelefono(telefono: String): Task<DocumentSnapshot> {
        Log.d("Firebase","Se esta buscando conductor por telefono $telefono")
        return db.whereEqualTo("telefono", telefono)
            .get()
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al buscar conductor por teléfono", e)
            }
            .continueWith { task ->
                val querySnapshot = task.result
                if (task.isSuccessful && !querySnapshot.isEmpty) {
                    // Devuelve el primer documento que coincida con el número de teléfono
                    Log.d("Firebase","Conductor consegui por telefono")
                    return@continueWith querySnapshot.documents[0]
                } else {
                    Log.e("Firebase", "Error al buscar conductor por teléfono")
                    return@continueWith null
                }
            }
    }

    fun createToken(idDriver: String): Task<String> {
        return FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                val token = it.result // TOKEN DE NOTIFICACIONES
                updateToken(idDriver, token)
            }else{
                Log.e("Firebase", "Token creado con exito")

            }
        }
    }

    fun updateToken(idDriver: String, token: String): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["token"] = token
        return db.document(idDriver).update(map).addOnCompleteListener{
            if(it.isSuccessful){
                Log.d("Firebase", "Token creado con exito")
            }else{
                Log.e("Firebase", "Error creando token")
            }
        }
    }

    fun uploadImage(id: String, nombreImagen: String, file: File): StorageTask<UploadTask.TaskSnapshot> {
        var fromFile = Uri.fromFile(file)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val ref = storage.child("$id/$nombreImagen-$timeStamp.jpg")

        if (nombreImagen.equals("fotoPerfil"))storageFotoPerfil = ref
        if (nombreImagen.equals("documento"))storageDocumento = ref
        if (nombreImagen.equals("documentoIdentidadTrasero"))storageDocumentoTrasero = ref
        if (nombreImagen.equals("licenciaFrontal"))storageLicenciaDelantera = ref
        if (nombreImagen.equals("licenciaTrasera"))storageLicenciaTrasera = ref

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
    fun getStorageDocumentoTrasero(): Task<Uri> {
        return storageDocumentoTrasero?.downloadUrl!!
    }
    fun getStorageLicenciaFrontal(): Task<Uri> {
        return storageLicenciaDelantera?.downloadUrl!!
    }
    fun getStorageLicenciaTrasera(): Task<Uri> {
        return storageLicenciaTrasera?.downloadUrl!!
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
    fun updateCar(idCar:String ): Task<Void>{
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

    fun updateDocumentoTrasero(driver: ConductorModelo): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["imagenDocumentoTrasero"] = driver?.imagenDocumentoTrasero!!
        return db.document(driver?.id!!).update(map)
    }

    fun updateLicenciaDelantera(driver: ConductorModelo): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["imagenLicenciaDelantera"] = driver?.imagenLicenciaDelantera!!
        return db.document(driver?.id!!).update(map)
    }

    fun updateLicenciaTrasera(driver: ConductorModelo): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["imagenLicenciaTrasera"] = driver?.imagenLicenciaTrasera!!
        return db.document(driver?.id!!).update(map)
    }

    fun descontarComisionesApp(descuento:Double): Task<Void> {
    // Calcular la nueva cantidad de saldo después de descontar la comisión

        var nuevoSaldo = datosConductor?.saldo?.minus(descuento!!)

        val map: MutableMap<String, Any> = HashMap()
        map["saldo"] = nuevoSaldo!!

        return db.document(datosConductor?.id!!).update(map).addOnCompleteListener{
            if(it.isSuccessful){
                datosConductor?.saldo=nuevoSaldo
                Log.d("Saldo","Se ha descontado saldo a la cuenta")
            }else{
                Log.e("Saldo","No se ha logrado realizar el cobro")
            }
        }
    }

    fun aumentarSaldo(recarga:Double): Task<Void> {
        // Calcular la nueva cantidad de saldo después de descontar la comisión

        var nuevoSaldo = datosConductor?.saldo?.plus(recarga!!)

        val map: MutableMap<String, Any> = HashMap()
        map["saldo"] = nuevoSaldo!!

        return db.document(datosConductor?.id!!).update(map).addOnCompleteListener{
            if(it.isSuccessful){
                datosConductor?.saldo=nuevoSaldo
                Log.d("Saldo","Se ha recargado saldo a la cuenta")
            }else{
                Log.e("Saldo","No se ha recargar realizar el saldo")
            }
        }
    }


}
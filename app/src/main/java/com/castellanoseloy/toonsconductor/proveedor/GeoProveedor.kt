package com.castellanoseloy.toonsconductor.proveedor

import android.content.Context
import android.util.Log
import com.castellanoseloy.toonsconductor.objetos.ErroresFirebase
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore
import java.util.Date

class GeoProveedor {

    val localizacion = FirebaseFirestore.getInstance().collection("Localizacion")
    val loacalizacionTrabajo = FirebaseFirestore.getInstance().collection("LocalizacionTrabajo")
    val geoLocalizacion = GeoFirestore(localizacion)
    val geoLocalizacionTrabajo = GeoFirestore(loacalizacionTrabajo)

    val db = FirebaseFirestore.getInstance()
    val localizacionTime = db.collection("Localizacion")
   fun guardarLocalizacion(idConductor:String,localizacion:LatLng, tiempoDisponible: Long){

       // Guardar el tiempo en la colección "Localizacion"
       val data = hashMapOf(
           "time" to tiempoDisponible,
           "idConductor" to idConductor,
           "ultima_conexion" to Date().time
       )
       localizacionTime.document(idConductor).set(data)

       geoLocalizacion.setLocation(idConductor,  GeoPoint(localizacion.latitude,localizacion.longitude))

   }

    fun guardarLocalizacionConFecha(idConductor: String, localizacion: LatLng) {
        val currentTimestamp = Timestamp.now()

        val localizacionData = hashMapOf(
            "latitude" to localizacion.latitude,
            "longitude" to localizacion.longitude,
            "timestamp" to currentTimestamp
        )

        val docRef = localizacionTime.document(idConductor)
        docRef
            .set(localizacionData)
            .addOnSuccessListener {
                Log.d("FIRESTORE", "Localización con fecha guardada exitosamente")
            }
            .addOnFailureListener { exception ->
                Log.d("FIRESTORE", "Error al guardar la localización con fecha: ${exception.message}")
            }
    }

    fun guardarLocalizacionTrabajando(idConductor:String,localizacion:LatLng){
        geoLocalizacionTrabajo.setLocation(idConductor, GeoPoint(localizacion.latitude,localizacion.longitude))
    }

    fun eliminarLocalizacion(idConductor: String){
        geoLocalizacion.getRefForDocumentID(idConductor).delete()
    }

    fun obtenerLocalizacion(idConductor: String,context: Context):Task<DocumentSnapshot>{
        return geoLocalizacion.getRefForDocumentID(idConductor).get().addOnFailureListener {
            ErroresFirebase.imprimirError(it,context)
        }
    }

    fun eliminarLocalizacionTrabajo(idConductor: String){
        geoLocalizacionTrabajo.getRefForDocumentID(idConductor).delete()
    }
}
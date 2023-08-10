package com.castellanoseloy.toonsconductor.proveedor

import android.content.Context
import com.castellanoseloy.toonsconductor.objetos.ErroresFirebase
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore

class GeoProveedor {

    val localizacion = FirebaseFirestore.getInstance().collection("Localizacion")
    val loacalizacionTrabajo = FirebaseFirestore.getInstance().collection("LocalizacionTrabajo")
    val geoLocalizacion = GeoFirestore(localizacion)
    val geoLocalizacionTrabajo = GeoFirestore(loacalizacionTrabajo)

   fun guardarLocalizacion(idConductor:String,localizacion:LatLng){
       geoLocalizacion.setLocation(idConductor, GeoPoint(localizacion.latitude,localizacion.longitude))
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
}
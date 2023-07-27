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

    val coleccion = FirebaseFirestore.getInstance().collection("localizacion")
    val geoFirestore = GeoFirestore(coleccion)

   fun guardarLocalizacion(idConductor:String,localizacion:LatLng){
       geoFirestore.setLocation(idConductor, GeoPoint(localizacion.latitude,localizacion.longitude))
   }

    fun eliminarLocalizacion(idConductor: String){
        geoFirestore.getRefForDocumentID(idConductor).delete()
    }

    fun obtenerLocalizacion(idConductor: String,context: Context):Task<DocumentSnapshot>{
        return geoFirestore.getRefForDocumentID(idConductor).get().addOnFailureListener {
            ErroresFirebase.imprimirError(it,context)
        }
    }
}
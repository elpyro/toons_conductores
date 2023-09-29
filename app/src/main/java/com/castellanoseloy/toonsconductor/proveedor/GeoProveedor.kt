package com.castellanoseloy.toonsconductor.proveedor

import android.content.Context
import android.util.Log
import com.castellanoseloy.toonsconductor.objetos.ErroresFirebase
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun guardarLocalizacion(idConductor: String, localizacion: LatLng, tiempoDisponible: Long) {
        // Obtén una referencia a la ubicación donde deseas guardar los datos en Realtime Database
        val ubicacionReference = databaseReference.child("Localizacion").child(idConductor)

        val pasajeros= CarProvider.datosCarroEnUso?.pasajeros
        // Crear un mapa con los datos que deseas guardar
        val data = hashMapOf(
            "idConductor" to idConductor,
            "ubicacion" to GeoPoint(localizacion.latitude, localizacion.longitude),
            "time" to tiempoDisponible,
            "pasajeros" to pasajeros,
            "ultima_conexion" to Date().time
        )

        // Realizar la escritura en Firebase Realtime Database
        ubicacionReference.updateChildren(data as Map<String, Any>) { databaseError, databaseReference ->
            if (databaseError == null) {
                Log.d("Localizacion", "Documento guardado con éxito en Realtime Database")
            } else {
                Log.e("Localizacion", "Error al guardar el documento en Realtime Database: ${databaseError.message}")
            }
        }
    }

    fun guardarLocalizacionTrabajando(idConductor: String, localizacion: LatLng) {

        val ubicacionTrabajoReference = databaseReference.child("LocalizacionTrabajo").child(idConductor)

        val ubicacionMap = HashMap<String, Any>()
        ubicacionMap["latitude"] = localizacion.latitude
        ubicacionMap["longitude"] = localizacion.longitude

        ubicacionTrabajoReference.updateChildren(ubicacionMap) { databaseError, _ ->
            if (databaseError == null) {
                Log.d("Localizacion", "Ubicación de trabajo actualizada con éxito en Realtime Database")
            } else {
                Log.e("Localizacion", "Error al actualizar la ubicación de trabajo en Realtime Database: ${databaseError.message}")
            }
        }
    }

    fun eliminarLocalizacionTrabajo(idConductor: String) {
        val ubicacionTrabajoReference = databaseReference.child("LocalizacionTrabajo").child(idConductor)

        ubicacionTrabajoReference.removeValue { databaseError, _ ->
            if (databaseError == null) {
                Log.d("Localizacion", "La ubicacion de trabajo eliminado con éxito en Realtime Database")
            } else {
                Log.e("Localizacion", "Error al eliminar el documento de trabajo en Realtime Database: ${databaseError.message}")
            }
        }
    }

    fun eliminarLocalizacion(idConductor: String) {
        val ubicacionReference = databaseReference.child("Localizacion").child(idConductor)

        ubicacionReference.removeValue { databaseError, _ ->
            if (databaseError == null) {
                Log.d("Localizacion", "Se elimino el conductor de la lista de disponibles")
            }else{
               Log.e("Localizacion", "Error al eliminar la ubicacion del conductor en Realtime Database: ${databaseError.message}")
            }
        }
    }
}
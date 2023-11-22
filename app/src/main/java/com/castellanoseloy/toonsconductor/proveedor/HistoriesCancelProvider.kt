package com.castellanoseloy.toonsconductor.proveedor

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.optic.uberclonedriverkotlin.models.History

class HistoriesCancelProvider {

    private val db = FirebaseDatabase.getInstance().reference.child("HistoriesCancels")


    fun create(history: History): Task<Void> {
        return db.child(history?.id!!).setValue(history).addOnFailureListener {
            Log.d("REALTIME_DB", "ERROR: ${it.message}")
        }
    }

    fun getBookingCancel(id:String): Query {
        return db.orderByChild("id").equalTo(id).limitToFirst(1)
    }

    fun updateStatus(idDriver: String, status: String): Task<Void> {
        return db.child(idDriver).child("status").setValue(status).addOnFailureListener { exception ->
            Log.d("REALTIME_DB", "ERROR: ${exception.message}")
        }
    }

    fun deleteBookingsForDriver(idDriver: String): Task<Void> {
        // Obtén la referencia a la ubicación de los datos que deseas eliminar
        val driverRef = db.child(idDriver)

        // Realiza la eliminación de los datos en la ubicación especificada
        return driverRef.removeValue().addOnFailureListener { exception ->
            Log.d("REALTIME_DB", "ERROR: ${exception.message}")
        }
    }

}
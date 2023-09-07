package com.castellanoseloy.toonsconductor.proveedor

import android.util.Log
import com.castellanoseloy.toonsconductor.modelos.Booking
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.optic.uberclonedriverkotlin.providers.DriverProvider

class BookingAceptedsProvider {

    val db = Firebase.firestore.collection("BookingsAccepteds")
    val authProvider = Autenticacion()


    fun create(booking: Booking): Task<Void> {
        return db.document(authProvider.obtenerId()).set(booking).addOnFailureListener {
            Log.d("FIRESTORE", "ERROR: ${it.message}")
        }
    }

    fun getBookingAccepteds(): Query {
        return db.whereEqualTo("idDriver",DriverProvider.datosConductor?.id)
    }

    fun updateStatus(idDriver: String, status: String): Task<Void> {
        return db.document(idDriver).update("status", status).addOnFailureListener { exception ->
            Log.d("FIRESTORE", "ERROR: ${exception.message}")
        }
    }

    fun updateStatusViajeConductor(idDriver: String, status: String): Task<Void> {
        return db.document(idDriver).update("status", status).addOnFailureListener { exception ->
            Log.d("FIRESTORE", "ERROR: ${exception.message}")
        }
    }




    fun deleteBookingsForDriver(idDriver: String): Task<Void> {

        val query = db.whereEqualTo("idDriver", idDriver)

        return query.get().continueWithTask { querySnapshotTask ->
            val deleteTasks = mutableListOf<Task<Void>>()

            for (document in querySnapshotTask.result!!) {
                val deleteTask = document.reference.delete()
                deleteTasks.add(deleteTask)
            }

            return@continueWithTask Tasks.whenAll(deleteTasks)
        }.addOnFailureListener { exception ->
            Log.d("FIRESTORE", "ERROR: ${exception.message}")
        }
    }

}
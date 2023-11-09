package com.castellanoseloy.toonsconductor.proveedor

import android.util.Log
import com.castellanoseloy.toonsconductor.api.NequiVerificacionPago
import com.castellanoseloy.toonsconductor.modelos.RecargasModelo
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.optic.uberclonedriverkotlin.providers.DriverProvider

class RecargasProvider {

    private val db = FirebaseDatabase.getInstance().reference.child("Recargas")
    val recargasPorVerificar = mutableListOf<String>()
    fun create(recarga: RecargasModelo): Task<Void> {
        return db.child(recarga.id!!).setValue(recarga).addOnFailureListener {
            Log.d("REALTIME_DB", "ERROR: ${it.message}")
        }
    }


    fun cambiarEstado(id: String, status: String): Task<Void> {
        Log.d("Pruebas Nequi", "cambio de estado a $status del $id")
        return db.child(id).child("estado").setValue(status).addOnFailureListener { exception ->
            Log.d("REALTIME_DB", "ERROR: ${exception.message}")
        }
    }

    fun deleteRecargaPorId(idRecarga: String): Task<Void> {
        val referenciaRecarga = db.child(idRecarga)
        return referenciaRecarga.removeValue()
    }





    fun buscarRecagasPorIdDriver(idDriver: String, callback: (List<RecargasModelo>) -> Unit) {
        // Crea una consulta que filtre por el campo "idDriver"
        val consulta = db.orderByChild("idDriver").equalTo(idDriver)
        Log.d("Pruebas Nequi", "buscando recargas del conductor: $idDriver")

        consulta.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recargasList = mutableListOf<RecargasModelo>()

                for (recargaSnapshot in snapshot.children) {
                    val recarga = recargaSnapshot.getValue(RecargasModelo::class.java)
                    recarga?.let { recargasList.add(it) }

                    if (recarga?.estado.equals("Pendiente") && recarga?.codigoVerificacion != null) {
                        // Verificar si esta recarga ya se procesó previamente
                        if (!recargasPorVerificar.contains(recarga.codigoVerificacion)) {
                            Log.d("Pruebas Nequi", "Verificando recarga nequi ${recarga.codigoVerificacion}")
                            recargasPorVerificar.add(recarga.codigoVerificacion)
                            verifcarRecargaNequi(recarga.codigoVerificacion!!, recarga.valor!!)
                        }
                    }
                }
                callback(recargasList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores si es necesario

                callback(emptyList())
            }
        })

    }

    private fun verifcarRecargaNequi(codigoVerificacion:String, recarga:String) {
        var nequiVerificacionPago= NequiVerificacionPago()
        nequiVerificacionPago.verificarEstadoDePago(codigoVerificacion!!){ estado->
            if(estado.isNotEmpty()) actualizarRecargaFirebase(estado, codigoVerificacion, recarga!!)
        }
    }
    fun actualizarRecargaFirebase(statusCode: String, codigoVerificacion: String, recarga: String) {
        Log.d("Nequi", "el codigo del estado es $statusCode")
        when (statusCode) {

            "33" -> {
                    //pago pendiente no hacer nada
            }
            "34" -> {
                    anctualizarEstado("Cancelado",codigoVerificacion)

            }
            "35" -> {

                    anctualizarEstado("Realizado",codigoVerificacion)

            }
            "69" -> {
                // Caducada
                    anctualizarEstado("Caducado",codigoVerificacion)

            }
            "71" -> {

                    anctualizarEstado("Fallido",codigoVerificacion)
            }
            else -> {
                // Otro estado (manejar según sea necesario)
                    anctualizarEstado("Desconocido",codigoVerificacion)
            }
        }
    }

    fun anctualizarEstado(estado:String,codigoVerificacion:String){
        val recargasProvider=RecargasProvider()
        recargasProvider.buscarPorCodigoVerificacion(codigoVerificacion!!){recarga->
            recargasProvider.cambiarEstado(recarga.id!!,estado)
                .addOnCompleteListener{
                    if(it.isSuccessful){
                        if(!recarga.estado.equals("Realizado") && estado == "Realizado"){
                            val driverProvider=DriverProvider()
                            driverProvider.aumentarSaldo(recarga.valor!!.toDouble())
                        }

                    }
                }
        }
    }

    fun buscarPorCodigoVerificacion(codigo: String, callback: (RecargasModelo) -> Unit) {

        val consulta = db.orderByChild("codigoVerificacion").equalTo(codigo).limitToFirst(1)
        Log.d("Pruebas Nequi","codigo a buscar: $codigo")
        consulta.keepSynced(true)
        consulta.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    var recarga = childSnapshot.getValue(RecargasModelo::class.java)
                    callback(recarga!!)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


}
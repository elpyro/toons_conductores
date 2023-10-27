package com.castellanoseloy.toonsconductor.proveedor

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class Autenticacion {

    val firebaseAutenticacion=FirebaseAuth.getInstance()

    fun login( email:String , clave:String ): Task<AuthResult>{
        return firebaseAutenticacion.signInWithEmailAndPassword(email,clave)
    }
    fun registro( email:String , clave:String ): Task<AuthResult>{
        return firebaseAutenticacion.createUserWithEmailAndPassword(email,clave)
    }
    fun obtenerId():String{
        return firebaseAutenticacion.currentUser?.uid ?: ""
    }
    fun sesionExistente():Boolean{
        var existente=false

        if(firebaseAutenticacion.currentUser!=null){
            existente=true
        }

        return existente
    }

    fun cambiarClave(newPassword: String): Task<Void>? {
        val usuario = firebaseAutenticacion.currentUser
        return usuario?.updatePassword(newPassword)
    }


    fun logout() {
        firebaseAutenticacion.signOut()
    }



}
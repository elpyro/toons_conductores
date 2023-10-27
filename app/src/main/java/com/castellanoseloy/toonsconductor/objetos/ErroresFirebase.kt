package com.castellanoseloy.toonsconductor.objetos

import android.content.Context
import android.util.Log
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException

object ErroresFirebase {

     fun imprimirError(exception: Exception?, context: Context) {
        // Maneja diferentes tipos de excepciones
        when (exception) {
            is FirebaseAuthUserCollisionException -> {
                // El usuario ya está registrado con el correo proporcionado
                Utilidades.mostrarToast("El correo ya está en uso", context)
                Log.e("RegistroUsuarioActivity", "El correo ya está en uso: ${exception.message}", exception)
            }
            is FirebaseAuthWeakPasswordException -> {
                // La contraseña es demasiado débil
                Utilidades.mostrarToast("Contraseña débil, debe contener al menos 6 caracteres", context)
                Log.e("RegistroUsuarioActivity", "Contraseña débil, debe contener al menos 6 caracteres: ${exception.message}", exception)
            }
            is FirebaseNetworkException -> {
                Utilidades.mostrarToast("Problemas en la conexion", context)
                Log.e("Firebase Error", "Problemas de conexion ${exception.message}", exception)
            }

            is FirebaseAuthInvalidCredentialsException -> {
                // Las credenciales proporcionadas son inválidas
                Utilidades.mostrarToast("Credenciales inválidas", context)
                Log.e("Firebase Error", "Error de credenciales inválidas: ${exception.message}", exception)
            }
            is FirebaseAuthInvalidUserException -> {
                // El usuario no existe o ha sido deshabilitado
                Utilidades.mostrarToast("Usuario no válido o deshabilitado", context)
                Log.e("Firebase Error", "Error de usuario no válido o deshabilitado: ${exception.message}", exception)
            }

            is FirebaseNetworkException -> {
                // No hay conexión a Internet o la conexión es lenta
                Utilidades.mostrarToast("Problemas en la conexión", context)
                Log.e("Firebase Error", "Error de conexión a Internet: ${exception.message}", exception)
            }
            is FirebaseFirestoreException -> {
                // Error relacionado con Firestore
                if (exception.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    Utilidades.mostrarToast("Firestore no está disponible", context)
                    Log.e("Firebase Error", "Error de Firestore no disponible: ${exception.message}", exception)
                } else {
                    Utilidades.mostrarToast("Error en Firestore: ${exception.message}", context)
                    Log.e("Firebase Error", "Error de Firestore: ${exception.message}", exception)
                }
            }

            else -> {
                // Otros errores de autenticación
                Utilidades.mostrarToast("Error: ${exception?.message}", context)
                Log.e("Firebase Error", "Error de Firebase: ${exception?.message}", exception)
            }
        }
    }
}
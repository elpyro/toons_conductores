package com.castellanoseloy.toonsconductor.activitys.MainActivity

import androidx.lifecycle.ViewModel
import com.castellanoseloy.toonsconductor.databinding.ActivityMainBinding


class MainActivityViewModel : ViewModel() {

    fun validarDatos(binding: ActivityMainBinding) : Boolean{
        var datosValidos=true

        val correo=binding.editTextCorreo.text.toString()
        val contrasena=binding.editTextContrasena.text.toString()
        if(correo.isEmpty()){
            binding.editTextCorreo.setError("Ingrese el correo")
            datosValidos=false
        }
        if(contrasena.isEmpty()){
            binding.editTextContrasena.setError("Ingrese la contraseña")
            datosValidos=false
        }
        return datosValidos
    }


}
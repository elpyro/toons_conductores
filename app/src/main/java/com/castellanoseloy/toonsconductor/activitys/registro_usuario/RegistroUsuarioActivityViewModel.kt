package com.castellanoseloy.toonsconductor.activitys.registro_usuario

import androidx.lifecycle.ViewModel
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonscliente.objetos.Utilidades.eliminarEspacios
import com.castellanoseloy.toonsconductor.databinding.ActivityRegistroUsuarioBinding


class MyActivityViewModel : ViewModel() {


    fun ValidarDatos(binding: ActivityRegistroUsuarioBinding):Boolean {
        var datosValidos=true

//        val nombre=binding.editTextName.text.toString()
//        val apellido=binding.editTextApellido.text.toString()
//        val telefono=binding.editTexTelefono.text.toString().eliminarEspacios()
//        val correo=binding.editTexCorreo.text.toString()
        val contrasena=binding.editTextContrasena.text.toString()
        val contrasena2=binding.editTextVerificarContrasena.text.toString()


//        if (nombre != null && nombre.length <= 3) {
//            binding?.editTextName?.setError("Obligatorio")
//            datosValidos=false
//        }
//
//        if (apellido != null && apellido.length <= 3) {
//            binding?.editTextApellido?.setError("Obligatorio")
//            datosValidos=false
//        }
//
//        if (telefono != null && telefono.length != 10) {
//            binding?.editTexTelefono?.setError("Verificar")
//            datosValidos=false
//        }

//        if (correo != null && correo.length <= 3) {
//            binding?.editTexCorreo?.setError("Obligatorio")
//            datosValidos=false
//        }else{
//            if(!Utilidades.isValidEmail(correo)) {
//                datosValidos=false
//                binding?.editTexCorreo?.setError("Verificar")
//            }
//        }

        if (contrasena != null && contrasena.length <= 5) {
            binding?.editTextContrasena?.setError("Debe tenerner al menos 6 carapteres")
            datosValidos=false
        }else{
            if(contrasena!=contrasena2){
                binding?.editTextContrasena?.setError("Contraseñas no coinciden")
                binding?.editTextVerificarContrasena?.setError("Contraseñas no coinciden")
                datosValidos=false
            }
        }

        return datosValidos
    }

}

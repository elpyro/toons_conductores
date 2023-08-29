package com.castellanoseloy.toonsconductor.activitys.registro_usuario

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.activitys.mapActivity.MapaActivity
import com.castellanoseloy.toonsconductor.databinding.ActivityRegistroUsuarioBinding
import com.castellanoseloy.toonsconductor.modelos.ConductorModelo
import com.castellanoseloy.toonsconductor.objetos.ErroresFirebase
import com.castellanoseloy.toonsconductor.proveedor.Autenticacion
import com.castellanoseloy.toonsconductor.proveedor.ClienteProveedor
import com.optic.uberclonedriverkotlin.providers.DriverProvider

class RegistroUsuarioActivity : AppCompatActivity() {
    private var viewModel: MyActivityViewModel? = null
    private val clienteProvider= ClienteProveedor()
    private lateinit var binding: ActivityRegistroUsuarioBinding
    private val autenticacion=  Autenticacion()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityRegistroUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //COLOCAR LA BARRA SUPERIOR TRANSAPENTE
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        viewModel = ViewModelProvider(this).get(MyActivityViewModel::class.java)

        listeners()
    }

    private fun listeners() {

        binding.buttonRegistrar.setOnClickListener{
            registrar()
        }


    }

    private fun registrar() {

        binding.buttonRegistrar.isEnabled=false
        val datosValidos=viewModel!!.ValidarDatos(binding)

        if(datosValidos) {
            val correo=binding.editTexCorreo.text.toString()
            val clave=binding.editTextContrasena.text.toString()
            autenticacion.registro(correo,clave).addOnCompleteListener {it->
                if(it.isSuccessful){
                    val conductor= ConductorModelo(
                        id= autenticacion.obtenerId(),
                        nombre="",
                        apellido="",
                        telefono="",
                        correo=binding.editTexCorreo.text.toString(),
                        validacion = "Valida tu usuario"
                    )

                    clienteProvider.crear(conductor).addOnCompleteListener {
                        if(it.isSuccessful){
                            DriverProvider.datosConductor=conductor
                            Utilidades.mostrarToast("Registro Exitoso",this@RegistroUsuarioActivity)
                            abriMapa()
                        }else{
                            binding.buttonRegistrar.isEnabled=true
                            ErroresFirebase.imprimirError(it.exception,this@RegistroUsuarioActivity)
                        }
                    }
                }else{
                    ErroresFirebase.imprimirError(it.exception,this@RegistroUsuarioActivity)
                }
            }
        }else{
            binding.buttonRegistrar.isEnabled=true
        }
    }

    private fun abriMapa(){
        //abrir actividad y eleminar todas las anteriores para dejala de principal
        val intent = Intent(this, MapaActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

}
package com.castellanoseloy.toonsconductor.activitys.MainActivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.activitys.MapActivity.MapaActivity
import com.castellanoseloy.toonsconductor.activitys.registro_usuario.RegistroUsuarioActivity
import com.castellanoseloy.toonsconductor.databinding.ActivityMainBinding
import com.castellanoseloy.toonsconductor.objetos.ErroresFirebase
import com.castellanoseloy.toonsconductor.proveedor.Autenticacion
import com.castellanoseloy.toonsconductor.proveedor.ClienteProveedor


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var viewModel: MainActivityViewModel? = null
    private val clienteProvider= ClienteProveedor()

    private val autenticacion=  Autenticacion()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //COLOCAR LA BARRA SUPERIOR TRANSAPENTE
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        listeners()
    }

    private fun listeners() {
        binding.butoonRegistrarse.setOnClickListener{
            registrar()
        }
        binding.buttonIniciarSesion.setOnClickListener{
            iniciarSesion()
        }
    }

    private fun iniciarSesion() {
        val datosValidos=viewModel!!.validarDatos(binding)

        if (datosValidos){
            autenticacion.login(binding.editTextCorreo.text.toString(),binding.editTextContrasena.text.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        abriMapa()
                    }else{
                        ErroresFirebase.imprimirError(it.exception,this@MainActivity)
                    }
                }
        }
    }

    private fun registrar() {
        val intent = Intent(this@MainActivity, RegistroUsuarioActivity::class.java)
        startActivity(intent)
    }
    private fun abriMapa(){
        //abrir actividad y eleminar todas las anteriores para dejala de principal
        val intent = Intent(this@MainActivity, MapaActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onStart() {
        if(autenticacion.sesionExistente()) abriMapa()
        super.onStart()
    }
}
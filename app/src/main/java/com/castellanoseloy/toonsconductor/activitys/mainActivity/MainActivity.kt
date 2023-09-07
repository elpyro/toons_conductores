package com.castellanoseloy.toonsconductor.activitys.mainActivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.castellanoseloy.toonsconductor.activitys.mapActivity.MapaActivity
import com.castellanoseloy.toonsconductor.activitys.mapTripActivity.MapTripActivity
import com.castellanoseloy.toonsconductor.activitys.profileActivity.ProfileActivity
import com.castellanoseloy.toonsconductor.activitys.registro_usuario.RegistroUsuarioActivity
import com.castellanoseloy.toonsconductor.databinding.ActivityMainBinding
import com.castellanoseloy.toonsconductor.modelos.CarModel
import com.castellanoseloy.toonsconductor.modelos.ConductorModelo
import com.castellanoseloy.toonsconductor.objetos.ErroresFirebase
import com.castellanoseloy.toonsconductor.proveedor.Autenticacion
import com.castellanoseloy.toonsconductor.proveedor.BookingAceptedsProvider
import com.castellanoseloy.toonsconductor.proveedor.CarProvider
import com.optic.uberclonedriverkotlin.providers.DriverProvider


class MainActivity : AppCompatActivity() {

    private val bookingAceptedsProvider = BookingAceptedsProvider()
    private lateinit var binding: ActivityMainBinding
    private var viewModel: MainActivityViewModel? = null



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
                        obtenerDatosConductor()
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

    private fun obtenerDatosConductor(){
        val idConductor=autenticacion.obtenerId()

        var driverProvideer=DriverProvider()
        driverProvideer.getDriver(idConductor!!).addOnSuccessListener { document ->
            if (document.exists()) {
                val driver = document.toObject(ConductorModelo::class.java)
                DriverProvider.datosConductor = driver

                Log.d("Firestore", "Obteniendo datos del conductor ${driver?.validacion}")

                verificarViajesPendientes()
                obtenerDatosCarro()
            }else{
                Log.d("Firestore", "No se encontraron datos del conductor")
            }
        }
    }

    private fun obtenerDatosCarro(){

        var carProvider=CarProvider()
        carProvider.getCarById(DriverProvider.datosConductor?.vehiculo!!).addOnSuccessListener { document ->
            if (document.exists()) {
                val car = document.toObject(CarModel::class.java)
                CarProvider.datosCarroEnUso = car
                Log.d("Firestore", "Obteniendo datos del carro en uso")
            }else{
                Log.d("Firestore", "No se encontraron datos del carro en uso")
            }
        }
    }
    private fun verificarViajesPendientes() {

        bookingAceptedsProvider.getBookingAccepteds().get().addOnSuccessListener { query ->
            if (query != null) {
                if (query.size() > 0) {
                    //viaje pendiente encontrado
                    crearAlertaDeViajePendiente()
                }else{
                    abrirMapaInicio()
                }
            }
        }
    }

    private fun crearAlertaDeViajePendiente() {


            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Viaje Pendiente")
            alertDialogBuilder.setMessage("Tienes un viaje pendiente ¿Deseas ir a el viaje?.")

            // Botón Aceptar
            alertDialogBuilder.setPositiveButton("Ir a viaje") { dialog, which ->
                abrirViajePendiente()
            }

            // Botón Cancelar
            alertDialogBuilder.setNegativeButton("Finalizarlo") { dialog, which ->
                bookingAceptedsProvider.deleteBookingsForDriver(autenticacion.obtenerId())
                abrirMapaInicio()
            }

            val alertDialog: AlertDialog = alertDialogBuilder.create()
            alertDialog.show()

    }

    private fun abrirMapaInicio() {
        //abrir actividad y eleminar todas las anteriores para dejala de principal
        val intent = Intent(this@MainActivity, MapaActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun abrirViajePendiente() {
        //abrir actividad y eleminar todas las anteriores para dejala de principal
        val intent = Intent(this@MainActivity, MapTripActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onStart() {
        if(autenticacion.sesionExistente()) obtenerDatosConductor()
        super.onStart()
    }
}



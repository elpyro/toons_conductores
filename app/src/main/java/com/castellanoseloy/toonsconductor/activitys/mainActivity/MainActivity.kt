@file:Suppress("DEPRECATION")

package com.castellanoseloy.toonsconductor.activitys.mainActivity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.activitys.codigoMensajeVerificacion.CodigoMensajeVerificacion
import com.castellanoseloy.toonsconductor.activitys.mapActivity.MapaActivity
import com.castellanoseloy.toonsconductor.activitys.mapTripActivity.MapTripActivity
import com.castellanoseloy.toonsconductor.databinding.ActivityMainBinding
import com.castellanoseloy.toonsconductor.modelos.CarModel
import com.castellanoseloy.toonsconductor.modelos.ConductorModelo
import com.castellanoseloy.toonsconductor.objetos.ErroresFirebase
import com.castellanoseloy.toonsconductor.proveedor.Autenticacion
import com.castellanoseloy.toonsconductor.proveedor.BookingAceptedsProvider
import com.castellanoseloy.toonsconductor.proveedor.CarProvider
import com.castellanoseloy.toonsconductor.proveedor.TransaccionesProvider
import com.castellanoseloy.toonsconductor.services.DatosPersitidos
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.optic.uberclonedriverkotlin.providers.DriverProvider


class MainActivity : AppCompatActivity() {

    private lateinit var progressDialog: ProgressDialog
    private val bookingAceptedsProvider = BookingAceptedsProvider()
    private lateinit var binding: ActivityMainBinding
    private var viewModel: MainActivityViewModel? = null

    private val autenticacion=  Autenticacion()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        // Iniciar el servicio
        val intent = Intent(this, DatosPersitidos::class.java)
        startService(intent)

        listeners()
    }


    private fun listeners() {
        binding.butoonRegistrarse.setOnClickListener{
            registrar()
        }
        binding.buttonOlvideContrasena.setOnClickListener{
            registrar()
        }
        binding.buttonIniciarSesion.setOnClickListener{
            registrar()
        }
        binding.buttonMostrarContrasena.setOnClickListener {
            mostrarContrasena()
        }

    }

    private fun mostrarContrasena() {
        if (binding.editTextContrasena.transformationMethod == PasswordTransformationMethod.getInstance()) {
            // Si la contraseña está oculta, muéstrala
            binding.editTextContrasena.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.buttonMostrarContrasena.text = "Ocultar"
        } else {
            // Si la contraseña está visible, ocúltala
            binding.editTextContrasena.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.buttonMostrarContrasena.text = "Mostrar"
        }
    }


    private fun registrar() {
        val intent = Intent(this@MainActivity, CodigoMensajeVerificacion::class.java)
        startActivity(intent)
    }

     fun inicializarDatosConductor(){
        val driverProvideer=DriverProvider()
        driverProvideer.createToken(autenticacion.obtenerId()).addOnCompleteListener {
            if(!it.isSuccessful){
                Utilidades.mostrarToast("Error de autenticacion",this)
                solicitarCredenciales()
            }
             }
        driverProvideer.getDriver(autenticacion.obtenerId()).addOnSuccessListener { document ->
            Log.d("Firestore", "Validando conductor ${autenticacion.obtenerId()}")
            if (document.exists()) {

                Utilidades.esperarUnSegundo()
                Utilidades.esperarUnSegundo()

                val driver = document.toObject(ConductorModelo::class.java)
                DatosPersitidos.datosConductor = driver


                Log.d("Firestore", "Obteniendo datos del conductor ${driver?.validacion}")
                verificarViajesPendientes()
                obtenerDatosCarro()

                val transaccionesProvider= TransaccionesProvider()
                transaccionesProvider.buscarTransaccionesPorIdDriver(DatosPersitidos.datosConductor?.id!!, true){
                    Log.d("Pruebas nequi","Las recargas se estan verificando")
                }

            }else{
                    Log.d("Firestore", "No se encontraron datos del conductor")
                    Utilidades.mostrarToast("No se encontraron datos del conductor",this)
                    solicitarCredenciales()
                }
            if (progressDialog.isShowing)  progressDialog.dismiss()
        }


    }

    private fun obtenerDatosCarro(){

        val carProvider=CarProvider()
        DatosPersitidos.datosConductor?.vehiculo?.let {
            carProvider.getCarById(it).addOnSuccessListener { document ->
                if (document.exists()) {
                    val car = document.toObject(CarModel::class.java)
                    if (car?.estado?.contains("Validado")!!) {
                        DatosPersitidos.datosCarroEnUso = car
                        DatosPersitidos.datosConductor!!.vehiculo=car.id
                    }else{
                        DatosPersitidos.datosConductor!!.vehiculo=""
                        DatosPersitidos.datosCarroEnUso = null
                    }

                    Log.d("Firestore", "Obteniendo datos del carro en uso")
                }else{
                    Log.d("Firestore", "No se encontraron datos del carro en uso")
                }
            }
        }
    }
    private fun verificarViajesPendientes() {

        val bookingQuery = bookingAceptedsProvider.getBookingAccepteds()

        bookingQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    progressDialog.dismiss()
                    crearAlertaDeViajePendiente()
                }else{
                    abrirMapaInicio()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Este método se llama si ocurre un error al recuperar los datos
                Log.d("REALTIME DATABASE", "ERROR: ${error.message}")
                ErroresFirebase.imprimirError(error.toException(), this@MainActivity)
            }
        })


    }

    private fun crearAlertaDeViajePendiente() {


            val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setIcon(R.drawable.ic_ixi_logo)
            alertDialogBuilder.setTitle("Viaje Pendiente")
            alertDialogBuilder.setMessage("Tienes un viaje pendiente ¿Deseas ir a el viaje?.")

            // Botón Aceptar
            alertDialogBuilder.setPositiveButton("Ir a viaje") { _, _ ->
                abrirViajePendiente()
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

    override fun onResume() {
        super.onResume()
        if(autenticacion.sesionExistente()) {
            binding.cardViewCredenciales.visibility= View.GONE
            binding.imageViewLogo.visibility= View.VISIBLE
            inicializarDatosConductor()
        }else{
            solicitarCredenciales()
        }
    }

    private fun solicitarCredenciales() {
        binding.cardViewCredenciales.visibility= View.VISIBLE
        binding.imageViewLogo.visibility= View.GONE
    }
}



package com.castellanoseloy.toonsconductor.activitys.registro_usuario

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.activitys.mainActivity.MainActivity
import com.castellanoseloy.toonsconductor.activitys.mapActivity.MapaActivity
import com.castellanoseloy.toonsconductor.databinding.ActivityRegistroUsuarioBinding
import com.castellanoseloy.toonsconductor.modelos.ConductorModelo
import com.castellanoseloy.toonsconductor.objetos.ErroresFirebase
import com.castellanoseloy.toonsconductor.proveedor.Autenticacion
import com.castellanoseloy.toonsconductor.utils.Preferencias
import com.optic.uberclonedriverkotlin.providers.DriverProvider

class RegistroUsuarioActivity : AppCompatActivity() {
    private var driver: ConductorModelo? = null
    private lateinit var progressDialog: ProgressDialog
    private var telefono: String? = null
    private var viewModel: MyActivityViewModel? = null
    private val driverProvider=DriverProvider()
    private lateinit var binding: ActivityRegistroUsuarioBinding
    private val autenticacion=  Autenticacion()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityRegistroUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MyActivityViewModel::class.java)
        telefono = intent.getStringExtra("telefono")

        progressDialog = ProgressDialog(this)

        // Verifica si se recibió el extra y actúa en consecuencia
        if (telefono != null) {
            binding.texviewTelefono.text=telefono
            buscarPorTelefono(telefono!!)
        }
        listeners()
    }
    private fun listeners() {

        binding.buttonRegistrar.setOnClickListener{
            registrar()
        }
        binding.buttonMostrarContrasena.setOnClickListener {
            mostrarContrasena()
        }

    }

    private fun buscarPorTelefono(telefono: String) {
        mostrarDialogProgreso()
        Log.d("Firebase","Telenofo recibido $telefono")
        driverProvider.getConductorPorTelefono(telefono)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val document = task.result
                    if (document != null && document.exists()) {
                        // Cliente encontrado, document contiene los datos del cliente
                        driver = document.toObject(ConductorModelo::class.java)
                        val nombre= driver?.nombre?: "" + driver?.apellido?: ""
                        binding.textViewActualizaContrenaInf.text=nombre+", actualiza tu contraseña"
                    }
                }
                progressDialog.dismiss()
            }
    }

    private fun mostrarContrasena() {
        //edittext contraseña
        if (binding.editTextContrasena.transformationMethod == PasswordTransformationMethod.getInstance()) {
            // Si la contraseña está oculta, muéstrala
            binding.editTextContrasena.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.buttonMostrarContrasena.text = "Ocultar"
        } else {
            // Si la contraseña está visible, ocúltala
            binding.editTextContrasena.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.buttonMostrarContrasena.text = "Mostrar"
        }
        //edittext verificar contraseña
        if (binding.editTextVerificarContrasena.transformationMethod == PasswordTransformationMethod.getInstance()) {
            // Si la contraseña está oculta, muéstrala
            binding.editTextVerificarContrasena.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.buttonMostrarContrasena.text = "Ocultar"
        } else {
            // Si la contraseña está visible, ocúltala
            binding.editTextVerificarContrasena.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.buttonMostrarContrasena.text = "Mostrar"
        }
    }

    fun mostrarDialogProgreso(){

        progressDialog.setTitle("Guardando...")
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Espere un momento...")
        progressDialog.show()
    }


    private fun registrar() {
        val datosValidos=viewModel!!.ValidarDatos(binding)

        if(datosValidos) {

            mostrarDialogProgreso()

            if (driver != null) {
                actualizarClave()
            } else {
                val clave = binding.editTextContrasena.text.toString()
                val correoRegistro = telefono + "@ixi-conductor.com"
                registrarIncioSesion(correoRegistro, clave)
            }
        }
    }

    private fun registrarIncioSesion(correoRegistro:String, clave:String) {
        autenticacion.registro(correoRegistro!!, clave).addOnCompleteListener { it ->
            if (it.isSuccessful) {

                val saldoObsequio=5000.0

                val driver = ConductorModelo(
                    id = autenticacion.obtenerId(),
                    clave=clave,
                    telefono = telefono,
                    saldo=saldoObsequio,
                    correo = correoRegistro,
                    validacion = "Por registrar"
                )
                actualizarCrearCliente(driver)

                val preferencias=Preferencias(this)
                preferencias.guardarPreferencia("nequi", telefono!!)

            } else {
                registrarError(it.exception!!)
            }
        }
    }

    private fun actualizarCrearCliente(driver: ConductorModelo?) {

        driverProvider.crearOActualizar(driver!!).addOnCompleteListener {
            if (it.isSuccessful) {
                Utilidades.esperarUnSegundo()
                Utilidades.esperarUnSegundo()

                finish()
               // (this@RegistroUsuarioActivity as? MainActivity)?.inicializarDatosConductor()
            } else {
                registrarError(it.exception!!)
            }
        }
    }

    private fun actualizarClave() {
        val correo=driver?.telefono+"@ixi-conductor.com"
        val claveActual =driver?.clave?: ""
        val claveNueva=binding.editTextContrasena.text.toString().trim()

        Log.d("Firebase", "Autenticando $correo clave: $claveActual ")
        autenticacion.login(correo, claveActual!!)
            .addOnCompleteListener {
                if (it.isSuccessful){

                    Log.d("Firebase", "Cambiando clave a: $claveNueva ")
                    autenticacion.cambiarClave(claveNueva)
                        ?.addOnSuccessListener {
                            Log.d("Firebase", "Clave cambiada ")

                            driver?.clave=claveNueva

                            actualizarCrearCliente(driver)
                        }
                        ?.addOnFailureListener { e ->
                            registrarError(e)
                        }
                }else{
                    registrarError(it.exception!!)
                }
            }
    }

    fun registrarError(e:Exception){
        progressDialog.dismiss()
        Log.e("Firebase Error", "ERROR EN FIREBASE $e")
        ErroresFirebase.imprimirError(e, this@RegistroUsuarioActivity)
    }

    private fun abriMapa(){

        progressDialog.dismiss()

        val intent = Intent(this, MapaActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

}
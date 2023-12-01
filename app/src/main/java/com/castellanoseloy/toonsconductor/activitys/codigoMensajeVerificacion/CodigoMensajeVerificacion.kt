package com.castellanoseloy.toonsconductor.activitys.codigoMensajeVerificacion

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.databinding.ActivityCodigoMensajeVerificacionBinding
import com.castellanoseloy.toonsconductor.modelos.ConductorModelo
import com.castellanoseloy.toonsconductor.objetos.ErroresFirebase
import com.castellanoseloy.toonsconductor.proveedor.Autenticacion
import com.castellanoseloy.toonsconductor.utils.Preferencias
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.optic.uberclonedriverkotlin.providers.DriverProvider
import java.util.concurrent.TimeUnit

class CodigoMensajeVerificacion : AppCompatActivity() {

    private var driver: ConductorModelo? = null
    private lateinit var progressDialog: ProgressDialog
    private lateinit var credential: PhoneAuthCredential
    private lateinit var telefono: String
    private lateinit var idVerificacion: String
    private lateinit var binding: ActivityCodigoMensajeVerificacionBinding

    private val driverProvider = DriverProvider()
    private val autenticacion = Autenticacion()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCodigoMensajeVerificacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listeners()
    }

    private val phoneAuthCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            // Implementa tus devoluciones de llamada aquí
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                progressDialog.dismiss()
                Log.d("verificacionMensajes", "¡Verificacion automatica exitosa!!!!!!!!!! XD")

                buscarPorTelefono()
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                progressDialog.dismiss()
                Log.d("verificacionMensajes", "verificacion fallo:  ${p0.message}")
                Utilidades.mostrarToast(
                    "No se pudo enviar el codigo: ${p0.message}",
                    this@CodigoMensajeVerificacion
                )
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                idVerificacion = verificationId

                progressDialog.dismiss()
                Log.d("verificacionMensajes", "¡El codigo se envio a $verificationId")

                binding.linearLayoutIngresaNumero.visibility = View.GONE
                binding.linearLayoutIngresaCodigo.visibility = View.VISIBLE
                binding.textviewCodigoEnviadoInfo.text =
                    "Hemos enviado un mensaje con un  codigo de verificación a tu número de télefono $telefono"
            }
        }


    fun startPhoneNumberVerification(
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        Log.d("verificacionMensajes", "Enviando a $phoneNumber")
        val options = PhoneAuthOptions.newBuilder(autenticacion.firebaseAutenticacion)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(120L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Completar la autenticación con el código de verificación


    private fun buscarPorTelefono() {

        Log.d("Firebase", "Telefono recibido $telefono")
        driverProvider.getConductorPorTelefono(telefono)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val document = task.result
                    if (document != null && document.exists()) {
                        finish()
                    } else {
                        CrearNuevoConductor()
                    }
                }

            }
    }

    private fun CrearNuevoConductor() {

        val saldoObsequio = 10000.0

        val driver = ConductorModelo(
            id = autenticacion.obtenerId(),
            telefono = telefono,
            saldo = saldoObsequio,
            validacion = "Por registrar"
        )

        val preferencias = Preferencias(this)
        preferencias.guardarPreferencia("nequi", telefono!!)

        actualizarCrearCliente(driver)


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

    fun registrarError(e: Exception) {
        progressDialog.dismiss()
        Log.e("Firebase Error", "ERROR EN FIREBASE $e")
        ErroresFirebase.imprimirError(e, this@CodigoMensajeVerificacion)
    }


    fun mostrarDialogProgreso(titulo: String) {
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle(titulo)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Espere un momento...")
        progressDialog.show()
    }

    private fun listeners() {
        binding.buttonVerificarTelefono.setOnClickListener {
            enviarCodigo()
        }
        binding.buttonVerificarCodigo.setOnClickListener {
            verificarCodigo()
        }
    }

    private fun enviarCodigo() {
        telefono = binding.editTextTelefono.text.toString()
        val longitud = telefono.length
        if (longitud != 10) {
            binding.editTextTelefono.setError("El telefono debe tener 10 digitos")
            return
        }
        mostrarDialogProgreso("Solicitando codigo")
        startPhoneNumberVerification("+57$telefono", phoneAuthCallbacks)
    }

    private fun verificarCodigo() {
        val codigo = binding.editTextCodigo.text.toString().trim()
        val longitud = codigo.length
        if (longitud != 6) {
            binding.editTextCodigo.setError("El codigo debe tener 6 digitos")
            return
        }
        mostrarDialogProgreso("Verificando codigo")
        Log.d("verificacionMensajes", "Verificando credencial $codigo para el $idVerificacion")
        credential = PhoneAuthProvider.getCredential(idVerificacion, codigo)

        autenticacion.signInWithPhoneAuthCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    buscarPorTelefono()
                } else {
                    progressDialog.dismiss()
                    Utilidades.mostrarToast(
                        "Codigo rechazado: ${it.exception?.message}",
                        this@CodigoMensajeVerificacion
                    )
                    Log.d("verificacionMensajes", "Usuario rechazado ${it.exception?.message}")
                }
            }
    }
}
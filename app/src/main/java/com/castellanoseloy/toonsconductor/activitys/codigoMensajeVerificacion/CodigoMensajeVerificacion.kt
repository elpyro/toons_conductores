package com.castellanoseloy.toonsconductor.activitys.codigoMensajeVerificacion

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.activitys.registro_usuario.RegistroUsuarioActivity
import com.castellanoseloy.toonsconductor.databinding.ActivityCodigoMensajeVerificacionBinding
import com.castellanoseloy.toonsconductor.modelos.ConductorModelo
import com.castellanoseloy.toonsconductor.objetos.ErroresFirebase
import com.castellanoseloy.toonsconductor.proveedor.Autenticacion
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.optic.uberclonedriverkotlin.providers.DriverProvider
import java.util.concurrent.TimeUnit

class CodigoMensajeVerificacion : AppCompatActivity() {

    private lateinit var progressDialog: ProgressDialog
    private lateinit var credential: PhoneAuthCredential
    private lateinit var telefono: String
    private lateinit var idVerificacion: String
    private lateinit var binding: ActivityCodigoMensajeVerificacionBinding
    private val auth = FirebaseAuth.getInstance()

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

                val intent = Intent(this@CodigoMensajeVerificacion, RegistroUsuarioActivity::class.java)
                intent.putExtra("telefono",telefono)
                startActivity(intent)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                progressDialog.dismiss()
                Log.d("verificacionMensajes", "verificacion fallo:  ${p0.message}")
                Utilidades.mostrarToast("No se pudo enviar el codigo: ${p0.message}", this@CodigoMensajeVerificacion)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                idVerificacion=verificationId

                progressDialog.dismiss()
                Log.d("verificacionMensajes", "¡El codigo se envio a $verificationId")

                binding.linearLayoutIngresaNumero.visibility=View.GONE
                binding.linearLayoutIngresaCodigo.visibility=View.VISIBLE
                binding.textviewCodigoEnviadoInfo.text="Hemos enviado un mensaje con un  codigo de verificación a tu número de télefono $telefono"
            }
        }


    fun startPhoneNumberVerification(
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        Log.d("verificacionMensajes", "Enviando a $phoneNumber")
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(120L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Completar la autenticación con el código de verificación
    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("verificacionMensajes", "Usuario aceptado")
                    val intent = Intent(this@CodigoMensajeVerificacion, RegistroUsuarioActivity::class.java)
                    intent.putExtra("telefono",telefono)
                    startActivity(intent)
                } else {
                    Utilidades.mostrarToast("Codigo rechazado: ${task.exception?.message}", this@CodigoMensajeVerificacion)
                    Log.d("verificacionMensajes", "Usuario rechazado ${task.exception?.message}")
                }
            }
    }


    fun mostrarDialogProgreso(){
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Solicitando codigo...")
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Espere un momento...")
        progressDialog.show()
    }

    private fun listeners() {
        binding.buttonVerificarTelefono.setOnClickListener{
            enviarCodigo()
        }
        binding.buttonVerificarCodigo.setOnClickListener{
            verificarCodigo()
        }
    }

    private fun enviarCodigo() {
        telefono = binding.editTextTelefono.text.toString()
        val longitud = telefono.length
        if (longitud!=10){
            binding.editTextTelefono.setError("El telefono debe tener 10 digitos")
            return
        }
        mostrarDialogProgreso()
        startPhoneNumberVerification("+57$telefono", phoneAuthCallbacks)
    }

    private fun verificarCodigo() {
        val codigo=binding.editTextCodigo.text.toString().trim()
        val longitud = codigo.length
        if (longitud!=6){
            binding.editTextCodigo.setError("El codigo debe tener 6 digitos")
            return
        }
        Log.d("verificacionMensajes", "Verificando credencial $codigo para el $idVerificacion")
        credential = PhoneAuthProvider.getCredential(idVerificacion, codigo)


        signInWithPhoneAuthCredential(credential)
    }
}
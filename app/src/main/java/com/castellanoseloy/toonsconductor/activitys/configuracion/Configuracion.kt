package com.castellanoseloy.toonsconductor.activitys.configuracion


import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.activitys.mainActivity.MainActivity
import com.castellanoseloy.toonsconductor.databinding.ActivityConfiguracionBinding
import com.castellanoseloy.toonsconductor.proveedor.Autenticacion
import com.castellanoseloy.toonsconductor.utils.Preferencias
import com.castellanoseloy.toonsconductor.proveedor.ConfigProvider
import java.io.File
import java.util.concurrent.TimeUnit


class Configuracion : AppCompatActivity() {
    private lateinit var binding:ActivityConfiguracionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityConfiguracionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Configuración"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(Color.WHITE)

        listeners()
    }

    private fun listeners() {
        // Cargar preferencias

        val preferencias=Preferencias(this)
        val preferenciasApp= preferencias.cargarPreferencias()

        //establecer preferencias
        binding.switchSonidos.isChecked = preferenciasApp.sonidos
        binding.radioGroupNavegador.check(preferenciasApp.navegadorSeleccionado)
        binding.editTextTelefono.setText(preferenciasApp.nequi)
        val ahora = System.currentTimeMillis()
        if (ahora - preferenciasApp.ultimaModificacionNequi >= TimeUnit.DAYS.toMillis(2)) {
            binding.buttonCambiarNequi.visibility= View.VISIBLE
        }


        // Escuchar cambios en el Switch y guardar preferencias
        binding.switchSonidos.setOnCheckedChangeListener { _, isChecked ->
            // Guardar preferencia
            preferencias.guardarPreferencia("sonidos",isChecked )
        }

        // Escuchar cambios en el RadioGroup y guardar preferencias
        binding.radioGroupNavegador.setOnCheckedChangeListener { _, checkedId ->
            // Guardar preferencia
            preferencias.guardarPreferencia("navegador",checkedId )
        }
        binding.buttonCambiarNequi.setOnClickListener{
            verificarTelefono()
        }
        binding.linearLayoutLogout.setOnClickListener { goToMain() }

        binding.linearLayoutAyuda.setOnClickListener { enviasWhatsapp() }
        binding.buttonGrupowhatsApp.setOnClickListener { grupoWhatsApp() }
    }

    private fun goToMain() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setIcon(R.drawable.ic_ixi_logo)
        alertDialogBuilder.setTitle("Cerrar Sesion")
        alertDialogBuilder.setMessage("Está seguro de cerrar la sesion?.")

        // Botón Aceptar
        alertDialogBuilder.setPositiveButton("Si") { _, _ ->
            val authProvider = Autenticacion()

            val i = Intent(this, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
            authProvider.logout()
        }
        // Botón Cancelar
        alertDialogBuilder.setNegativeButton("No") { _, _ ->
        }
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()

    }
    private fun verificarTelefono() {
        val telefono = binding.editTextTelefono.text.toString()
        val longitud = telefono.length
        if (longitud!=10){
            binding.editTextTelefono.error = "El telefono debe tener 10 digitos"
            return
        }
        mostrarConfirmacion(telefono)
    }

    private fun enviasWhatsapp() {
        val configuracion= ConfigProvider()
        configuracion.informacionWhatsApp()
            .addOnSuccessListener { documentSnapshot ->
                // Verifica si el documento existe y contiene el campo "ayudaNumero"
                if (documentSnapshot.exists() && documentSnapshot.contains("ayudaNumero")) {
                    val numeroWhatsApp = documentSnapshot.getString("ayudaNumero")
                    numeroWhatsApp?.let { Utilidades.enviarWhatsapp(numeroWhatsApp, "Hola, ",this) }
                //    numeroWhatsApp?.let { compartirWhatsapp(it) }
                } else {
                    // El documento no existe o no contiene el campo "grupoWhatsApp"
                    Log.d("FIREBASE", "El documento no existe o no contiene 'ayudaNumero'")
                }
            }

    }

    private fun grupoWhatsApp() {
        val configuracion= ConfigProvider()
        configuracion.informacionWhatsApp()
            .addOnSuccessListener { documentSnapshot ->
                // Verifica si el documento existe y contiene el campo "grupoUrl"
                if (documentSnapshot.exists() && documentSnapshot.contains("grupoUrl")) {
                    // Obtén el valor del campo "grupoUrl"
                    val grupoWhatsApp = documentSnapshot.getString("grupoUrl")
                    grupoWhatsApp?.let { Utilidades.abrirEnlaceWhatsApp(it, this) }
                } else {
                    // El documento no existe o no contiene el campo "grupoWhatsApp"
                    Log.d("FIREBASE", "El documento no existe o no contiene 'grupoUrl'")
                }
            }
    }



    private fun mostrarConfirmacion(telefono: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setIcon(R.drawable.ic_ixi_logo)
        alertDialogBuilder.setTitle("Cambio de Nequi")
        alertDialogBuilder.setMessage("Esta seguro de cambiar su Nequi a $telefono \nSolo podra cambiarlo cada 2 días")

        // Botón Aceptar
        alertDialogBuilder.setPositiveButton("Cambiar") { _, _ ->
            val preferencias=Preferencias(this)
            preferencias.guardarPreferencia("nequi",telefono.trim())
        }

        // Botón Cancelar
        alertDialogBuilder.setNegativeButton("Cancelar") { _, _ ->

        }

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}
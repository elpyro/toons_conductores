package com.castellanoseloy.toonsconductor.activitys.profileActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.castellanoseloy.toonsconductor.R
import android.app.Activity
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.databinding.ActivityProfileBinding
import com.castellanoseloy.toonsconductor.modelos.ConductorModelo
import com.castellanoseloy.toonsconductor.proveedor.Autenticacion
import com.github.dhaval2404.imagepicker.ImagePicker

import com.google.protobuf.Empty

import com.optic.uberclonedriverkotlin.providers.DriverProvider
import java.io.File


class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    val driverProvider = DriverProvider()
    val authProvider = Autenticacion()

    private var imageFile: File? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getDriver()
        binding.imageViewBack.setOnClickListener { finish() }
        binding.btnUpdate.setOnClickListener { updateInfo() }
        binding.circleImageProfile.setOnClickListener { selectImage() }
    }

    private val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK) {
            val fileUri = data?.data
            imageFile = File(fileUri?.path)
            binding.circleImageProfile.setImageURI(fileUri)
        }
        else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        }
        else {
            Toast.makeText(this, "Tarea cancelada", Toast.LENGTH_LONG).show()
        }

    }
    private fun selectImage() {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }

    private fun updateInfo() {
        val name = binding.textFieldName.text.toString()
        val lastname = binding.textFieldLastname.text.toString()
        val phone = binding.textFieldPhone.text.toString()

        val driver = ConductorModelo(
            id = DriverProvider.datosConductor?.id,
            nombre = name,
            apellido = lastname,
            telefono = phone,
            imagen = DriverProvider.datosConductor?.imagen,
            validacion = "Verificando Datos"
        )

        if (imageFile != null) {
            driverProvider.uploadImage(DriverProvider.datosConductor?.id!!, imageFile!!).addOnSuccessListener { taskSnapshot ->
                driverProvider.getImageUrl().addOnSuccessListener { url ->
                    val imageUrl = url.toString()
                    driver.imagen = imageUrl
                    driverProvider.update(driver).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this@ProfileActivity, "Datos actualizados correctamente", Toast.LENGTH_LONG).show()
                        }
                        else {
                            Toast.makeText(this@ProfileActivity, "No se pudo actualizar la informacion", Toast.LENGTH_LONG).show()
                        }
                    }
                    Log.d("STORAGE", "$imageUrl")
                }
            }
        }   else {
            driverProvider.update(driver).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Datos actualizados correctamente", Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(this@ProfileActivity, "No se pudo actualizar la informacion", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun getDriver() {
        driverProvider.getDriver(DriverProvider.datosConductor?.id!!).addOnSuccessListener { document ->
            if (document.exists()) {
                val driver = document.toObject(ConductorModelo::class.java)
                binding.textViewEmail.text = driver?.correo
                binding.textFieldName.setText(driver?.nombre)
                binding.textFieldLastname.setText(driver?.apellido)
                binding.textFieldPhone.setText(driver?.telefono)

                if(driver?.validacion.equals("Valida tu usuario"))  binding.imageEstado.setImageResource(R.drawable.ic_valida_datos)
                if(driver?.validacion.equals("Verificando Datos"))  binding.imageEstado.setImageResource(R.drawable.ic_pendiente_validar)
                if(driver?.validacion.equals("Validado"))  binding.imageEstado.setImageResource(R.drawable.ic_validado)
                if(driver?.validacion.equals("Bloqueado"))  binding.imageEstado.setImageResource(R.drawable.ic_prohibido)

                binding.imageEstado.setOnClickListener{
                    if (driver?.validacion.equals("Valida tu usuario")) Utilidades.mostrarToast("Valida tu usuario",this)
                    if(driver?.validacion.equals("Verificando Datos")) Utilidades.mostrarToast("Estamos verificando tus datos",this)
                    if(driver?.validacion.equals("Validado"))  Utilidades.mostrarToast("Usuario Validado",this)
                    if(driver?.validacion.equals("Bloqueado")) Utilidades.mostrarToast("Usuario Bloqueado",this)
                }

                if (driver?.imagen != null) {
                    if (driver.imagen != "") {
                        Glide.with(this).load(driver.imagen).into(binding.circleImageProfile)
                    }
                }
            }
        }
    }

}
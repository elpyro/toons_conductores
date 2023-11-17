package com.castellanoseloy.toonsconductor.activitys.profileActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.castellanoseloy.toonsconductor.R
import android.app.Activity
import android.app.ProgressDialog
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.databinding.ActivityProfileBinding
import com.castellanoseloy.toonsconductor.modelos.ConductorModelo
import com.github.dhaval2404.imagepicker.ImagePicker

import com.optic.uberclonedriverkotlin.providers.DriverProvider
import java.io.File


class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    val driverProvider = DriverProvider()

    private var fotoPerfil: File? = null
    private var documento: File? = null
    private var documentoTrasero: File? =null
    private var licenciaFrontal: File? =null
    private var licenciaTrasera: File? =null

    private var imagenSeleccionada: String? = null

    private var viewModel: ProfileActivityViewModel ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = DriverProvider.datosConductor?.validacion
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(Color.WHITE)

        viewModel= ProfileActivityViewModel()
        getDriver()

        observador()
        listeners()
    }

    private fun observador() {
        viewModel?.elementosActualizar?.observe(this, Observer { it ->
                if( it==0){
                    progressDialog!!.dismiss()
                    Utilidades.mostrarToast("Datos Actualizados",this)
                    finish()
                }
        })
    }

    private fun listeners() {

        binding.btnUpdate.setOnClickListener { updateInfo() }
        binding.circleImageProfile.setOnClickListener { selectImage() }
        binding.imageViewDocumento.setOnClickListener{ selectDocument()}
        binding.imageViewDocumentoTrasero.setOnClickListener{selectDocumentoTrasero()}
        binding.imageViewLicenciaFrontal.setOnClickListener{selectLicenceFrontal()}
        binding.imageViewLicenciaTrasera.setOnClickListener{selectLicenceTrasera()}
    }

    private val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK) {
            val fileUri = data?.data

            if(imagenSeleccionada.equals("fotoPerfil")){
                fotoPerfil = File(fileUri?.path)
                binding.circleImageProfile?.setImageURI(fileUri)
            }
            if(imagenSeleccionada.equals("documentoIdentidad")){
                documento = File(fileUri?.path)
                binding.imageViewDocumento?.setImageURI(fileUri)
            }
            if(imagenSeleccionada.equals("documentoIdentidadTrasero")) {
                documentoTrasero = File(fileUri?.path)
                binding.imageViewDocumentoTrasero?.setImageURI(fileUri)
            }
            if(imagenSeleccionada.equals("licenciaFrontal")){
                licenciaFrontal = File(fileUri?.path)
                binding.imageViewLicenciaFrontal?.setImageURI(fileUri)
            }
            if(imagenSeleccionada.equals("licenciaTrasera")) {
                licenciaTrasera = File(fileUri?.path)
                binding.imageViewLicenciaTrasera?.setImageURI(fileUri)
            }

        }
        else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        }
        else {
            Toast.makeText(this, "Tarea cancelada", Toast.LENGTH_LONG).show()
        }

    }
    private fun selectImage() {
        imagenSeleccionada= "fotoPerfil"
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }

    private fun selectDocument() {
        imagenSeleccionada= "documentoIdentidad"
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }
    private fun selectDocumentoTrasero() {
        imagenSeleccionada= "documentoIdentidadTrasero"
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }

    private fun selectLicenceFrontal() {
        imagenSeleccionada= "licenciaFrontal"
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }

    private fun selectLicenceTrasera() {
        imagenSeleccionada= "licenciaTrasera"
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }

    val progressDialog: ProgressDialog by lazy {
        ProgressDialog(this).apply {
            setMessage("Guardando...")
            setCancelable(true)
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
        }
    }


    private fun updateInfo() {

        progressDialog!!.show()

        binding.btnUpdate.isEnabled=false

        //cuantos elementos va se van a guardar
        if(fotoPerfil!=null) viewModel?.incrementarElementos()
        if(documento!=null)viewModel?.incrementarElementos()
        if(documentoTrasero!=null)viewModel?.incrementarElementos()
        if(licenciaFrontal!=null)viewModel?.incrementarElementos()
        if(licenciaTrasera!=null)viewModel?.incrementarElementos()



        val name = binding.textFieldName.text.toString()
        val lastname = binding.textFieldLastname.text.toString()
        val phone = binding.textFieldPhone.text.toString()

        val driver = ConductorModelo(
            id = DriverProvider.datosConductor?.id,
            nombre = name,
            apellido = lastname,
            telefono = phone,
            imagenPerfil = DriverProvider.datosConductor?.imagenPerfil,
            imagenDocumento = DriverProvider.datosConductor?.imagenDocumento,
            imagenDocumentoTrasero = DriverProvider.datosConductor?.imagenDocumentoTrasero,
            validacion = "Verificando Datos"
        )

        driverProvider.update(driver).addOnCompleteListener {
            viewModel?.decrementarElementos()
            if (!it.isSuccessful) {
                Toast.makeText(
                    this@ProfileActivity,
                    "No se pudo actualizar la informacion",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        if (fotoPerfil != null) {
            driverProvider.uploadImage(
                DriverProvider.datosConductor?.id!!,
                "fotoPerfil",
                fotoPerfil!!
            ).addOnSuccessListener { taskSnapshot ->
                driverProvider.getStorageFotoPerfil().addOnSuccessListener { url ->
                    val imageUrl = url.toString()
                    Log.d("Firestore", "la ruta de la imagen de perfil es: ${imageUrl}")
                    driver.imagenPerfil = imageUrl
                    driverProvider.updateFotoPerfil(driver).addOnCompleteListener {
                        viewModel?.decrementarElementos()
                        if (!it.isSuccessful) {
                            Toast.makeText(
                                this@ProfileActivity,
                                "No se pudo guardar la foto de perfil",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    Log.d("STORAGE", "$imageUrl")
                }
            }
        }

        if (documento != null) {
            driverProvider.uploadImage(
                DriverProvider.datosConductor?.id!!,
                "documento",
                documento!!
            ).addOnSuccessListener { taskSnapshot ->
                driverProvider.getStorageDocumento().addOnSuccessListener { url ->
                    val imageUrl = url.toString()
                    Log.d("Firestore", "la ruta de la imagen documento es: ${imageUrl}")
                    driver.imagenDocumento = imageUrl
                    driverProvider.updateDocumento(driver).addOnCompleteListener {
                        viewModel?.decrementarElementos()
                        if (!it.isSuccessful) {
                            Toast.makeText(
                                this@ProfileActivity,
                                "No se pudo guardar el documento de identidad",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    Log.d("STORAGE", "$imageUrl")
                }
            }
        }
        if (documentoTrasero != null) {
            driverProvider.uploadImage(DriverProvider.datosConductor?.id!!, "documentoIdentidadTrasero", documentoTrasero!!)
                .addOnSuccessListener { taskSnapshot ->
                    driverProvider.getStorageDocumentoTrasero().addOnSuccessListener { url ->
                        val imageUrl = url.toString()
                        Log.d("Firestore", "la ruta de la imagen la licencia es: ${imageUrl}")
                        driver.imagenDocumentoTrasero = imageUrl
                        driverProvider.updateDocumentoTrasero(driver).addOnCompleteListener {
                            viewModel?.decrementarElementos()
                            if (!it.isSuccessful) {
                                Toast.makeText(
                                    this@ProfileActivity,
                                    "No se pudo guardar la licencia de conducir",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            Log.d("STORAGE", "$imageUrl")
                        }
                    }
                }
            }

        if (licenciaFrontal != null) {
            driverProvider.uploadImage(DriverProvider.datosConductor?.id!!, "licenciaFrontal", licenciaFrontal!!)
                .addOnSuccessListener { taskSnapshot ->
                    driverProvider.getStorageLicenciaFrontal().addOnSuccessListener { url ->
                        val imageUrl = url.toString()
                        Log.d("Firestore", "la ruta de la imagen la licencia es: ${imageUrl}")
                        driver.imagenLicenciaDelantera = imageUrl
                        driverProvider.updateLicenciaDelantera(driver).addOnCompleteListener {
                            viewModel?.decrementarElementos()
                            if (!it.isSuccessful) {
                                Toast.makeText(
                                    this@ProfileActivity,
                                    "No se pudo guardar la licencia de conducir",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            Log.d("STORAGE", "$imageUrl")
                        }
                    }
                }
        }

        if (licenciaTrasera != null) {
            driverProvider.uploadImage(DriverProvider.datosConductor?.id!!, "licenciaTrasera", licenciaTrasera!!)
                .addOnSuccessListener { taskSnapshot ->
                    driverProvider.getStorageLicenciaTrasera().addOnSuccessListener { url ->
                        val imageUrl = url.toString()
                        Log.d("Firestore", "la ruta de la imagen la licencia es: ${imageUrl}")
                        driver.imagenLicenciaTrasera = imageUrl
                        driverProvider.updateLicenciaTrasera(driver).addOnCompleteListener {
                            viewModel?.decrementarElementos()
                            if (!it.isSuccessful) {
                                Toast.makeText(
                                    this@ProfileActivity,
                                    "No se pudo guardar la licencia de conducir",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            Log.d("STORAGE", "$imageUrl")
                        }
                    }
                }
        }
        }


    private fun getDriver() {
            driverProvider.getDriver(DriverProvider.datosConductor?.id!!)
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val driver = document.toObject(ConductorModelo::class.java)
                        binding.textFieldPhone.text = driver?.telefono
                        binding.textFieldName.setText(driver?.nombre)
                        binding.textFieldLastname.setText(driver?.apellido)

                        if (driver?.validacion.equals("Valida tu usuario")) binding.imageEstado.setImageResource(
                            R.drawable.ic_valida_datos
                        )
                        if (driver?.validacion.equals("Verificando Datos")) binding.imageEstado.setImageResource(
                            R.drawable.ic_pendiente_validar
                        )
                        if (driver?.validacion.equals("Validado")) binding.imageEstado.setImageResource(
                            R.drawable.ic_validado
                        )
                        if (driver?.validacion.equals("Bloqueado")) binding.imageEstado.setImageResource(
                            R.drawable.ic_prohibido
                        )

                        binding.imageEstado.setOnClickListener {
                            if (driver?.validacion.equals("Valida tu usuario")) Utilidades.mostrarToast(
                                "Valida tu usuario",
                                this
                            )
                            if (driver?.validacion.equals("Verificando Datos")) Utilidades.mostrarToast(
                                "Estamos verificando tus datos",
                                this
                            )
                            if (driver?.validacion.equals("Validado")) Utilidades.mostrarToast(
                                "Usuario Validado",
                                this
                            )
                            if (driver?.validacion.equals("Bloqueado")) Utilidades.mostrarToast(
                                "Usuario Bloqueado",
                                this
                            )
                        }

                        if (driver?.imagenPerfil != null) {
                            if (driver.imagenPerfil != "") {
                                Glide.with(this).load(driver.imagenPerfil)
                                    .into(binding.circleImageProfile)
                            }
                        }

                        if (driver?.imagenDocumento != null) {
                            if (driver.imagenDocumento != "") {
                                Glide.with(this).load(driver.imagenDocumento)
                                    .into(binding.imageViewDocumento)
                            }
                        }

                        if (driver?.imagenDocumentoTrasero != null) {
                            if (driver.imagenDocumentoTrasero != "") {
                                Glide.with(this).load(driver.imagenDocumentoTrasero)
                                    .into(binding.imageViewDocumentoTrasero)
                            }
                        }

                        if (driver?.imagenLicenciaDelantera != null) {
                            if (driver.imagenLicenciaDelantera != "") {
                                Glide.with(this).load(driver.imagenLicenciaDelantera)
                                    .into(binding.imageViewLicenciaFrontal)
                            }
                        }

                        if (driver?.imagenLicenciaTrasera != null) {
                            if (driver.imagenLicenciaTrasera != "") {
                                Glide.with(this).load(driver.imagenLicenciaTrasera)
                                    .into(binding.imageViewLicenciaTrasera)
                            }
                        }
                        
                    }
                }
        }

}
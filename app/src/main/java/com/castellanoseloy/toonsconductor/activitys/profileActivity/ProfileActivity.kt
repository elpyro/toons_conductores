package com.castellanoseloy.toonsconductor.activitys.profileActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.castellanoseloy.toonsconductor.R
import android.app.Activity
import android.app.ProgressDialog
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.databinding.ActivityProfileBinding
import com.castellanoseloy.toonsconductor.modelos.ConductorModelo
import com.castellanoseloy.toonsconductor.services.DatosPersitidos
import com.github.dhaval2404.imagepicker.ImagePicker

import com.optic.uberclonedriverkotlin.providers.DriverProvider
import java.io.File


class ProfileActivity : AppCompatActivity() {

    private var driver: ConductorModelo? = null
    private lateinit var binding: ActivityProfileBinding
    val driverProvider = DriverProvider()

    private var fotoPerfil: File? = null
    private var documento: File? = null
    private var documentoTrasero: File? = null
    private var licenciaFrontal: File? = null
    private var licenciaTrasera: File? = null
    private var fotoValidacion: File? = null

    private var imagenSeleccionada: String? = null

    private var viewModel: ProfileActivityViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Mí perfil"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(Color.WHITE)

        viewModel = ProfileActivityViewModel()
        getDriver()

        observador()
        listeners()
    }

    private fun observador() {
        viewModel?.mensaje?.observe(this) { mensaje ->
            Utilidades.mostrarToast(mensaje, this)
        }
        viewModel?.elementosActualizar?.observe(this, Observer { it ->
            if (it == 0) {
                progressDialog!!.dismiss()
                Utilidades.mostrarToast("Datos Actualizados", this)
                finish()
            }
        })
    }

    private fun listeners() {

        binding.btnUpdate.setOnClickListener { verificacionDatos() }
        binding.circleImageProfile.setOnClickListener { selectImage() }
        binding.imageViewDocumento.setOnClickListener { selectDocument() }
        binding.imageViewDocumentoTrasero.setOnClickListener { selectDocumentoTrasero() }
        binding.imageViewLicenciaFrontal.setOnClickListener { selectLicenceFrontal() }
        binding.imageViewLicenciaTrasera.setOnClickListener { selectLicenceTrasera() }
        binding.circleImageValidacion.setOnClickListener { selectFotoValidacion() }
    }

    private fun verificacionDatos() {

        var editarDatos = false
        if (driver?.numeroDocumento != null) editarDatos = true
        val datosValidos = viewModel?.validarDatos(
            binding,
            fotoPerfil,
            documento,
            documentoTrasero,
            licenciaFrontal,
            licenciaTrasera,
            fotoValidacion,
            editarDatos
        )

        if (datosValidos == true) {
            driver?.let {
                viewModel?.verificarDocumento(
                    it,
                    binding.textFieldDocumento.text.toString(),
                    binding.spinner.selectedItem.toString()
                ) { existe ->
                    if (existe) {
                        // El número de documento ya existe en otro registro

                        if(binding.circleImageValidacion.isVisible){
                            updateInfo()
                        }else{
                            binding.circleImageValidacion.visibility = View.VISIBLE
                            alertCuentaExistente()
                        }

                    } else {
                        alertaConfirmacionDatos()
                    }
                }
            }
        }
    }

    private fun alertCuentaExistente() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Validar Datos")
        alertDialogBuilder.setMessage("Detectamos que ya tienes una cuenta asociada con estas credenciales. \nPara realizar un cambio de cuenta al nuevo número ${driver?.telefono}, por favor sigue estos pasos:\n\n1. Tómate una foto de tu documento de identidad.\n2. Actualiza tus datos para validar la solicitud de cambio de cuenta.")

        // Botón Aceptar
        alertDialogBuilder.setPositiveButton("Aceptar") { dialog, which ->

        }
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun alertaConfirmacionDatos() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Validar Datos")
        alertDialogBuilder.setMessage("¿Desea enviar a verificar sus datos?, una vez enviado sus datos no podran ser cambiados hasta ser verificados")

        // Botón Aceptar
        alertDialogBuilder.setPositiveButton("Si") { dialog, which ->
            updateInfo()
        }
        // Botón Cancelar
        alertDialogBuilder.setNegativeButton("No") { dialog, which ->
        }
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private val startImageForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK) {
                val fileUri = data?.data

                if (imagenSeleccionada.equals("fotoPerfil")) {
                    fotoPerfil = File(fileUri?.path)
                    binding.circleImageProfile?.setImageURI(fileUri)
                }
                if (imagenSeleccionada.equals("documentoIdentidad")) {
                    documento = File(fileUri?.path)
                    binding.imageViewDocumento?.setImageURI(fileUri)
                }
                if (imagenSeleccionada.equals("documentoIdentidadTrasero")) {
                    documentoTrasero = File(fileUri?.path)
                    binding.imageViewDocumentoTrasero?.setImageURI(fileUri)
                }
                if (imagenSeleccionada.equals("licenciaFrontal")) {
                    licenciaFrontal = File(fileUri?.path)
                    binding.imageViewLicenciaFrontal?.setImageURI(fileUri)
                }
                if (imagenSeleccionada.equals("licenciaTrasera")) {
                    licenciaTrasera = File(fileUri?.path)
                    binding.imageViewLicenciaTrasera?.setImageURI(fileUri)
                }
                if (imagenSeleccionada.equals("fotoValidacion")) {
                    fotoValidacion = File(fileUri?.path)
                    binding.circleImageValidacion?.setImageURI(fileUri)
                }

            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Imagen cancelada", Toast.LENGTH_LONG).show()
            }

        }

    private fun selectImage() {
        imagenSeleccionada = "fotoPerfil"
        ImagePicker.with(this)
            .crop()
            .cameraOnly()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }

    private fun selectDocument() {
        imagenSeleccionada = "documentoIdentidad"
        ImagePicker.with(this)
            .crop(14.0F, 9.0F)
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }

    private fun selectDocumentoTrasero() {
        imagenSeleccionada = "documentoIdentidadTrasero"
        ImagePicker.with(this)
            .crop(14.0F, 9.0F)
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }

    private fun selectLicenceFrontal() {
        imagenSeleccionada = "licenciaFrontal"
        ImagePicker.with(this)
            .crop(14.0F, 9.0F)
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }

    private fun selectLicenceTrasera() {
        imagenSeleccionada = "licenciaTrasera"
        ImagePicker.with(this)
            .crop(14.0F, 9.0F)
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }
    private fun selectFotoValidacion() {
        imagenSeleccionada = "fotoValidacion"
        ImagePicker.with(this)
            .crop()
            .cameraOnly()
            .compress(1024)
            .maxResultSize(1080, 1080)
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

        binding.btnUpdate.isEnabled = false

        //cuantos elementos va se van a guardar
        if (fotoPerfil != null) viewModel?.incrementarElementos()
        if (documento != null) viewModel?.incrementarElementos()
        if (documentoTrasero != null) viewModel?.incrementarElementos()
        if (licenciaFrontal != null) viewModel?.incrementarElementos()
        if (licenciaTrasera != null) viewModel?.incrementarElementos()
        if (fotoValidacion != null) viewModel?.incrementarElementos()

        val name = binding.textFieldName.text.toString()
        val lastname = binding.textFieldLastname.text.toString()
        val numeroDocumento = binding.textFieldDocumento.text.toString()
        val tipoDocumento = binding.spinner.selectedItem.toString()
        val validacion:String= obtenerTipoValidacion()
        val driver = ConductorModelo(
            id = DatosPersitidos.datosConductor?.id,
            nombre = name,
            apellido = lastname,
            tipoDocumento = tipoDocumento,
            numeroDocumento = numeroDocumento,
            imagenPerfil = DatosPersitidos.datosConductor?.imagenPerfil,
            imagenDocumento = DatosPersitidos.datosConductor?.imagenDocumento,
            imagenDocumentoTrasero = DatosPersitidos.datosConductor?.imagenDocumentoTrasero,
            validacion = validacion
        )

        driverProvider.update(driver).addOnCompleteListener {
            viewModel?.decrementarElementos()
            if (!it.isSuccessful) {
                Toast.makeText(
                    this@ProfileActivity,
                    "No se pudo actualizar la información",
                    Toast.LENGTH_LONG
                ).show()
            }else{
                DatosPersitidos.datosConductor=driver
            }
        }

        if (fotoPerfil != null) {
            driverProvider.uploadImage(
                DatosPersitidos.datosConductor?.id!!,
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
                DatosPersitidos.datosConductor?.id!!,
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
            driverProvider.uploadImage(
                DatosPersitidos.datosConductor?.id!!,
                "documentoIdentidadTrasero",
                documentoTrasero!!
            )
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
            driverProvider.uploadImage(
                DatosPersitidos.datosConductor?.id!!,
                "licenciaFrontal",
                licenciaFrontal!!
            )
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
            driverProvider.uploadImage(
                DatosPersitidos.datosConductor?.id!!,
                "licenciaTrasera",
                licenciaTrasera!!
            )
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
        if (fotoValidacion != null) {
            driverProvider.uploadImage(
                DatosPersitidos.datosConductor?.id!!,
                "fotoValidacion",
                fotoValidacion!!
            ).addOnSuccessListener { taskSnapshot ->
                driverProvider.getStorageFotoValidacion().addOnSuccessListener { url ->
                    val imageUrl = url.toString()
                    Log.d("Firestore", "la ruta de la imagen de perfil es: ${imageUrl}")
                    driver.imagenFotoValidacion = imageUrl
                    driverProvider.updateFotoValidacion(driver).addOnCompleteListener {
                        viewModel?.decrementarElementos()
                        if (!it.isSuccessful) {
                            Toast.makeText(
                                this@ProfileActivity,
                                "No se pudo guardar la foto de validación",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    Log.d("STORAGE", "$imageUrl")
                }
            }
        }

    }

    private fun obtenerTipoValidacion(): String {
        return if(binding.circleImageValidacion.isVisible){
            "Recuperacion de Telefono"
        }else{
            "Verificando Datos"
        }
    }


    private fun getDriver() {
        driverProvider.getDriver(DatosPersitidos.datosConductor?.id!!)
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    driver = document.toObject(ConductorModelo::class.java)
                    binding.textFieldPhone.text = driver?.telefono
                    binding.textFieldName.setText(driver?.nombre)
                    binding.textFieldLastname.setText(driver?.apellido)
                    binding.textFieldDocumento.setText(driver?.numeroDocumento)
                    //estblecer el valor del spinner
                    val opcionesArray =
                        applicationContext.resources.getStringArray(R.array.tipoDocumento)
                    val indiceValorPredeterminado = opcionesArray.indexOf(driver?.tipoDocumento)
                    if (indiceValorPredeterminado != -1) {
                        // Establecer el valor predeterminado en el Spinner
                        binding?.spinner?.setSelection(indiceValorPredeterminado)
                    }

                    if (driver?.validacion?.contains("Valida tu usuario")!!){
                        binding.imageEstado.setImageResource(
                            R.drawable.ic_valida_datos
                        )
                        binding.textViewEstado.text = driver?.validacion
                    }
                    if (driver?.validacion?.contains("Verificando Datos")!!){
                        binding.imageEstado.setImageResource(
                            R.drawable.ic_pendiente_validar
                        )
                        binding.textViewEstado.text = driver?.validacion
                        binding.btnUpdate.visibility= View.GONE
                    }
                    if (driver?.validacion?.contains("Validado")!!){
                        binding.imageEstado.setImageResource(
                            R.drawable.ic_validado
                        )
                        binding.textViewEstado.text = driver?.validacion
                        binding.btnUpdate.visibility= View.GONE
                    }
                    if (driver?.validacion?.contains("Bloqueado")!!){
                        binding.imageEstado.setImageResource(
                            R.drawable.ic_prohibido
                        )
                        binding.textViewEstado.text = driver?.validacion
                        binding.btnUpdate.visibility= View.GONE
                    }


                    if (driver?.imagenPerfil != null) {
                        if (driver!!.imagenPerfil != "") {
                            Glide.with(this).load(driver!!.imagenPerfil)
                                .into(binding.circleImageProfile)
                        }
                    }

                    if (driver?.imagenDocumento != null) {
                        if (driver!!.imagenDocumento != "") {
                            Glide.with(this).load(driver!!.imagenDocumento)
                                .into(binding.imageViewDocumento)
                        }
                    }

                    if (driver?.imagenDocumentoTrasero != null) {
                        if (driver!!.imagenDocumentoTrasero != "") {
                            Glide.with(this).load(driver!!.imagenDocumentoTrasero)
                                .into(binding.imageViewDocumentoTrasero)
                        }
                    }

                    if (driver?.imagenLicenciaDelantera != null) {
                        if (driver!!.imagenLicenciaDelantera != "") {
                            Glide.with(this).load(driver!!.imagenLicenciaDelantera)
                                .into(binding.imageViewLicenciaFrontal)
                        }
                    }

                    if (driver?.imagenLicenciaTrasera != null) {
                        if (driver!!.imagenLicenciaTrasera != "") {
                            Glide.with(this).load(driver!!.imagenLicenciaTrasera)
                                .into(binding.imageViewLicenciaTrasera)
                        }
                    }
                    if (driver?.imagenFotoValidacion != null) {
                        if (driver!!.imagenFotoValidacion != "") {
                            binding.circleImageValidacion.visibility=View.VISIBLE
                            Glide.with(this).load(driver!!.imagenFotoValidacion)
                                .into(binding.circleImageValidacion)
                        }
                    }

                }
            }
    }

}
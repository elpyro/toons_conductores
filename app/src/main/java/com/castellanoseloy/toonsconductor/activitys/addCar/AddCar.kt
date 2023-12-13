package com.castellanoseloy.toonsconductor.activitys.addCar

import android.app.Activity
import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.databinding.ActivityAddCarBinding
import com.castellanoseloy.toonsconductor.modelos.CarModel
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.github.dhaval2404.imagepicker.ImagePicker
import java.io.File
import java.util.UUID


class AddCar : AppCompatActivity() {

    private lateinit var idCar: String
    private var carModel: CarModel? = null
    var colorSeleccionado = Color.BLACK

    private var imageDocumentoDelantero: File? = null
    private var imageDocumentoTrasero: File? = null
    private var fotoFrontal: File? = null
    private var fotoLateral: File? = null

    private lateinit var imagenSeleccionada: String
    private lateinit var binding: ActivityAddCarBinding
    private lateinit var viewModel: AddCarViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Agregar Vehiculo"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(Color.WHITE)

        viewModel = AddCarViewModel()


        if (intent.hasExtra("carModelo")) {
            carModel = intent.getSerializableExtra("carModelo") as CarModel
            idCar = carModel!!.id.toString()
            colorSeleccionado = carModel!!.color!!

            binding?.btnUpdate?.text = "Guardar"
            if (!carModel!!.estado?.contains("Valida tu vehiculo")!!) binding?.btnUpdate?.visibility =
                View.GONE

            viewModel.cargarDatos(carModel!!, binding, applicationContext)
        } else {
            idCar = UUID.randomUUID().toString()
        }

        binding.imageViewCarColor.setColorFilter(colorSeleccionado)//color negro por defecto para el carro

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
                Utilidades.mostrarToast("Datos Guardados", this)
                finish()
            }
        })
    }

    val progressDialog: ProgressDialog by lazy {
        ProgressDialog(this).apply {
            setMessage("Guardando...")
            setCancelable(false)
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
        }
    }


    private fun listeners() {
        binding.circleImageFotoFrontal.setOnClickListener { selectImage() }
        binding.circleImageFotoLateral.setOnClickListener { selectImageLateral() }
        binding.imageViewDocumentoVehiculoFontal.setOnClickListener { selectDocumentFrontal() }
        binding.imageViewDocumentoVehiculoTrasero.setOnClickListener { selectDocumentTrasero() }
        binding.imageViewCarColor.setOnClickListener { selectColor() }
        binding.btnUpdate.setOnClickListener { actualizarCarro() }

    }

    private fun actualizarCarro() {
        var editarDatos = false
        if (carModel != null) editarDatos = true

        val datosValidados = viewModel.validarDatos(
            binding,
            fotoFrontal,
            fotoLateral,
            imageDocumentoDelantero,
            imageDocumentoTrasero,
            editarDatos
        )

        if (datosValidados) {
            dialogAlert(editarDatos)
        }
    }


    private fun dialogAlert(editarDatos: Boolean) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Validar Datos")
        alertDialogBuilder.setMessage("¿Desea enviar a verificar sus datos?, una vez enviado sus datos no podran ser cambiados hasta ser verificados")

        // Botón Aceptar
        alertDialogBuilder.setPositiveButton("Si") { dialog, which ->
            prepararDialogProcess()
            viewModel.salveData(
                idCar,
                binding,
                fotoFrontal,
                fotoLateral,
                imageDocumentoDelantero,
                imageDocumentoTrasero,
                colorSeleccionado,
                editarDatos
            )
        }
        // Botón Cancelar
        alertDialogBuilder.setNegativeButton("No") { dialog, which ->
        }
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun prepararDialogProcess() {
        progressDialog!!.show()

        binding.btnUpdate.isEnabled = false

        if (fotoFrontal != null) viewModel?.incrementarElementos()
        if (fotoLateral != null) viewModel?.incrementarElementos()
        if (imageDocumentoDelantero != null) viewModel?.incrementarElementos()
        if (imageDocumentoTrasero != null) viewModel?.incrementarElementos()

    }

    private fun selectImage() {
        imagenSeleccionada = "fotoFrontal"
        ImagePicker.with(this)
            .crop()
            .cameraOnly()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }

    private fun selectImageLateral() {
        imagenSeleccionada = "fotoLateral"
        ImagePicker.with(this)
            .crop()
            .cameraOnly()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }

    private fun selectDocumentFrontal() {
        imagenSeleccionada = "documentoVehiculo_frontal"
        ImagePicker.with(this)
            .crop(14.0F, 9.0F)
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }

    private fun selectDocumentTrasero() {
        imagenSeleccionada = "documentoVehiculo_trasero"
        ImagePicker.with(this)
            .crop(14.0F, 9.0F)
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }

    private val startImageForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK) {
                val fileUri = data?.data

                if (imagenSeleccionada.equals("fotoFrontal")) {
                    fotoFrontal = File(fileUri?.path)
                    binding.circleImageFotoFrontal?.setImageURI(fileUri)
                }
                if (imagenSeleccionada.equals("fotoLateral")) {
                    fotoLateral = File(fileUri?.path)
                    binding.circleImageFotoLateral?.setImageURI(fileUri)
                }

                if (imagenSeleccionada.equals("documentoVehiculo_frontal")) {
                    imageDocumentoDelantero = File(fileUri?.path)
                    binding.imageViewDocumentoVehiculoFontal?.setImageURI(fileUri)
                }

                if (imagenSeleccionada.equals("documentoVehiculo_trasero")) {
                    imageDocumentoTrasero = File(fileUri?.path)
                    binding.imageViewDocumentoVehiculoTrasero?.setImageURI(fileUri)
                }

            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Tarea cancelada", Toast.LENGTH_LONG).show()
            }

        }

    private fun selectColor() {
        var currentBackgroundColor: Int = Color.BLUE
        ColorPickerDialogBuilder
            .with(this)
            .setTitle("Seleccione color de vehiculo")
            .initialColor(currentBackgroundColor)
            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
            .density(12)
            .lightnessSliderOnly()
            .setPositiveButton(
                "ok"
            ) { dialog, selectedColor, allColors -> changeColorCar(selectedColor) }
            .setNegativeButton(
                "cancel"
            ) { dialog, which -> }
            .build()
            .show()
    }

    private fun changeColorCar(selectedColor: Int) {
        colorSeleccionado = selectedColor
        binding.imageViewCarColor.setColorFilter(selectedColor)
    }

}
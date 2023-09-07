package com.castellanoseloy.toonsconductor.activitys.addCar

import android.app.Activity
import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.databinding.ActivityAddCarBinding
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.github.dhaval2404.imagepicker.ImagePicker
import java.io.File


class AddCar : AppCompatActivity() {

    var colorSeleccionado= Color.BLACK
    private var Imagedocumento: File? =null
    private var fotoFrontal: File? =null
    private var fotoLateral: File? =null
    private lateinit var imagenSeleccionada: String
    private lateinit var binding: ActivityAddCarBinding
    private lateinit var viewModel: AddCarViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAddCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Agregar Vehiculo"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(Color.WHITE)

        viewModel= AddCarViewModel()

        binding.imageViewCarColor.setColorFilter(colorSeleccionado)//color negro por defecto para el carro

        observador()
        listeners()
    }

    private fun observador() {
        viewModel?.mensaje?.observe(this) { mensaje->
            Utilidades.mostrarToast(mensaje, this)
        }
        viewModel?.elementosActualizar?.observe(this, Observer { it ->
            if( it==0){
                progressDialog!!.dismiss()
                Utilidades.mostrarToast("Vehiculo creado",this)
                finish()
            }
        })
    }

    val progressDialog: ProgressDialog by lazy {
        ProgressDialog(this).apply {
            setMessage("Guardando...")
            setCancelable(true)
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
        }
    }


    private fun listeners() {
        binding.circleImageFotoFrontal.setOnClickListener { selectImage() }
        binding.circleImageFotoLateral.setOnClickListener { selectImageLateral() }
        binding.imageViewDocumentSOAT.setOnClickListener { selectDocument() }
        binding.imageViewCarColor.setOnClickListener{selectColor()}
        binding.btnUpdate.setOnClickListener{

            val datosValidados= viewModel.validarDatos(binding,fotoFrontal,fotoLateral, Imagedocumento)
            if(datosValidados){
                prepararDialogProcess()
                viewModel.salveData(binding, fotoFrontal,fotoLateral,Imagedocumento,colorSeleccionado)
            }

        }
    }

    private fun prepararDialogProcess() {
        progressDialog!!.show()

        binding.btnUpdate.isEnabled=false

        if(fotoFrontal!=null) viewModel?.incrementarElementos()
        if(fotoLateral!=null)viewModel?.incrementarElementos()
        if(Imagedocumento!=null)viewModel?.incrementarElementos()
    }

    private fun selectImage() {
        imagenSeleccionada= "fotoFrontal"
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }
    private fun selectImageLateral() {
        imagenSeleccionada= "fotoLateral"
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }
    private fun selectDocument() {
        imagenSeleccionada= "documentoSOAT"
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }

    private val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK) {
            val fileUri = data?.data

            if(imagenSeleccionada.equals("fotoFrontal")){
                fotoFrontal = File(fileUri?.path)
                binding.circleImageFotoFrontal?.setImageURI(fileUri)
            }
            if(imagenSeleccionada.equals("fotoLateral")){
                fotoLateral = File(fileUri?.path)
                binding.circleImageFotoLateral?.setImageURI(fileUri)
            }

            if(imagenSeleccionada.equals("documentoSOAT")){
                Imagedocumento = File(fileUri?.path)
                binding.imageViewDocumentSOAT?.setImageURI(fileUri)
            }
//

        }
        else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        }
        else {
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
            .setPositiveButton(
                "ok"
            ) { dialog, selectedColor, allColors ->    changeColorCar(selectedColor) }
            .setNegativeButton(
                "cancel"
            ) { dialog, which -> }
            .build()
            .show()

    }

    private fun changeColorCar(selectedColor: Int) {
        colorSeleccionado=selectedColor
        binding.imageViewCarColor.setColorFilter(selectedColor)
    }

}
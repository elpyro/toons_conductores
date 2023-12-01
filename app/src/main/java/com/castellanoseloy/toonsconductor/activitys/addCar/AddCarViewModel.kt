package com.castellanoseloy.toonsconductor.activitys.addCar

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.databinding.ActivityAddCarBinding
import com.castellanoseloy.toonsconductor.modelos.CarModel
import com.castellanoseloy.toonsconductor.proveedor.CarProvider
import com.castellanoseloy.toonsconductor.services.DatosPersitidos
import java.io.File

class AddCarViewModel : ViewModel() {


    private val _elementosActualizar = MutableLiveData<Int>()
    val mensaje=MutableLiveData<String> ()

    val elementosActualizar: LiveData<Int>
        get() = _elementosActualizar

    init {
        _elementosActualizar.value = 1
    }

    fun incrementarElementos() {
        val nuevoValor = _elementosActualizar.value?.plus(1)
        _elementosActualizar.value = nuevoValor!!
    }
    fun decrementarElementos() {
        val nuevoValor = _elementosActualizar.value?.minus(1)
        _elementosActualizar.value = nuevoValor!!
    }

    fun salveData(
        idCar: String,
        binding: ActivityAddCarBinding,
        fotoFrontal: File?,
        fotoLateral: File?,
        Imagedocumento: File?,
        imageDocumentoTrasero: File?,
        colorSeleccionado: Int,
        editarDatos: Boolean
    ) {

        if(!editarDatos){
            val carProvinder= CarProvider()
            carProvinder.createCarId(idCar).addOnCompleteListener { //si el vehiculo es nuevo, primero creamos el id para luego editar
                if(it.isSuccessful){
                    guardarOnline(binding,idCar, colorSeleccionado)
                    guardarImagenes(idCar,fotoFrontal,fotoLateral,Imagedocumento,imageDocumentoTrasero)
                }
            }
        }else{
            guardarOnline(binding,idCar, colorSeleccionado)
            guardarImagenes(idCar,fotoFrontal,fotoLateral,Imagedocumento,imageDocumentoTrasero)
        }



    }

    private fun guardarImagenes(
        idCar: String,
        fotoFrontal: File?,
        fotoLateral: File?,
        imagenDocumento: File?,
        imageDocumentoTrasero: File?
    ) {
        val carProvider= CarProvider()
        if (fotoFrontal != null) {
            carProvider.uploadImage(
                idCar,
                "fotoFrontal",
                fotoFrontal!!
            ).addOnSuccessListener { taskSnapshot ->
                carProvider.getStorageImagenFrontal().addOnSuccessListener { url ->
                    val imageUrl = url.toString()
                    Log.d("Firestore", "la ruta de la imagen de perfil es: ${imageUrl}")
                    val car=CarModel(id=idCar, imagenFrontal =imageUrl )
                    carProvider.updateImagenFrontal(car).addOnCompleteListener {
                        decrementarElementos()
                        if (!it.isSuccessful) {
                           mensaje.value="No se pudo guardar la foto frontal"
                            Log.e("Firebase", "ERROR GUARDANDO EN LA NUBE: ${it.exception}")
                        }
                    }
                    Log.d("STORAGE", "$imageUrl")
                }
            }
        }

        if (fotoLateral != null) {
            carProvider.uploadImage(
                idCar,
                "fotoLateral",
                fotoLateral!!
            ).addOnSuccessListener { taskSnapshot ->
                carProvider.getStorageImagenLateral().addOnSuccessListener { url ->
                    val imageUrl = url.toString()

                    Log.d("Firestore", "la ruta de la imagen documento es: ${imageUrl}")

                    val car=CarModel(id=idCar, imagenLateral =imageUrl )
                    carProvider.updateImagenLateral(car).addOnCompleteListener {
                        decrementarElementos()
                        if (!it.isSuccessful) {
                            mensaje.value="No se pudo guardar la imagen lateral"
                            Log.e("Firebase", "ERROR GUARDANDO EN LA NUBE: ${it.exception}")
                        }
                    }
                    Log.d("STORAGE", "$imageUrl")
                }
            }
        }
        if ( imagenDocumento!= null) {
            carProvider.uploadImage(idCar, "documentoVehiculo_frontal", imagenDocumento!!)
                .addOnSuccessListener { taskSnapshot ->
                    carProvider.getStorageDocumentoFrontal().addOnSuccessListener { url ->
                        val imageUrl = url.toString()
                        Log.d("Firestore", "la ruta de la imagen la licencia es: ${imageUrl}")
                        val car=CarModel(id=idCar, imagenDocumento =imageUrl )
                        carProvider.updateDocumento(car).addOnCompleteListener {
                            decrementarElementos()
                            if (!it.isSuccessful) {
                              mensaje.value="No se pudo guardar el documento"
                                Log.e("Firebase", "ERROR GUARDANDO EN LA NUBE: ${it.exception}")
                            }
                            Log.d("STORAGE", "$imageUrl")
                        }
                    }
                }
        }

        if ( imageDocumentoTrasero!= null) {
            carProvider.uploadImage(idCar, "documentoVehiculo_trasero", imageDocumentoTrasero!!)
                .addOnSuccessListener { taskSnapshot ->
                    carProvider.getStorageDocumentoTrasero().addOnSuccessListener { url ->
                        val imageUrl = url.toString()
                        Log.d("Firestore", "la ruta de la imagen la licencia es: ${imageUrl}")
                        val car=CarModel(id=idCar, imagenDocumentoTrasero =imageUrl )
                        carProvider.updateDocumentoTrasero(car).addOnCompleteListener {
                            decrementarElementos()
                            if (!it.isSuccessful) {
                                mensaje.value="No se pudo guardar el documento"
                                Log.e("Firebase", "ERROR GUARDANDO EN LA NUBE: ${it.exception}")
                            }
                            Log.d("STORAGE", "$imageUrl")
                        }
                    }
                }
        }

    }

    private fun guardarOnline(
        binding: ActivityAddCarBinding,
        idCar: String,
        colorSeleccionado: Int
    ) {

        val placa = binding.textFieldPlaca.text.toString()
        val marca = binding.textFielMarca.text.toString()
        val tipo = binding.spinner.selectedItem.toString()
        val pasajeros = binding.textFielAsisentos.text.toString().toInt()
        val idConductor= DatosPersitidos.datosConductor?.id
        val malero=binding.switchMaletero.isChecked
        val tecnomecanica=binding.checkBoxTecnomecanica.isChecked
        val SOAT=binding.checkBoxSoat.isChecked

        val car = CarModel(
            id= idCar,
            estado = "Validando",
            idDriver=idConductor,
            bland = marca,
            tipo =tipo,
            placa = placa,
            color=colorSeleccionado,
            pasajeros= pasajeros,
            malero= malero,
            tecnomecanica= tecnomecanica,
            SOAT=SOAT
        )

        val carProvider=CarProvider()
        carProvider.updateCar(car).addOnCompleteListener {
            if(it.isSuccessful){
                decrementarElementos()
            }else{
                decrementarElementos()
                mensaje.value="Error guardando datos"
                Log.e("Firebase", "ERROR GUARDANDO EN LA NUBE: ${it.exception}")
            }
        }

    }

    fun validarDatos(
        binding: ActivityAddCarBinding,
        fotoFrontal: File?,
        fotoLateral: File?,
        Imagedocumento: File?,
        imageDocumentoTrasero: File?,
        editarDatos: Boolean
    ): Boolean {

        var datosValidos=true

        if(!editarDatos){
            if(fotoFrontal==null) {
                mensaje.value= "Se requiere una foto frontal del vehiculo"
                datosValidos=false
            }
            if(fotoLateral==null) {
                mensaje.value= "Se requiere una foto lateral del vehiculo"
                datosValidos=false
            }
            if(Imagedocumento==null) {
                mensaje.value= "Se requiere imagen frontal del documento del vehiculo"
                datosValidos=false
            }
            if(imageDocumentoTrasero==null) {
                mensaje.value= "Se requiere imagen trasera del documento del vehiculo"
                datosValidos=false
            }

        }
        if(!binding.checkBoxTecnomecanica.isChecked){
            mensaje.value="Debe tener la revisión tecnomecánica vigente para registrar el vehiculo"
            datosValidos=false
        }

        if(!binding.checkBoxSoat.isChecked){
            mensaje.value="Debe tener el SOAT vigente para registrar el vehiculo"
            datosValidos=false
        }
        if(binding.textFieldPlaca.text.toString().isEmpty()){
            datosValidos=false
            binding.textFieldPlaca.setError("Obligatorio")
        }
        if(binding.textFielMarca.text.toString().isEmpty()){
            datosValidos=false
            binding.textFielMarca.setError("Obligatorio")
        }
        if(binding.textFielAsisentos.text.toString().isEmpty()){
            datosValidos=false
            binding.textFielAsisentos.setError("Obligatorio")
        }

        return datosValidos

    }

    fun cargarDatos(carModel: CarModel, binding: ActivityAddCarBinding, applicationContext: Context) {


        if (carModel?.imagenFrontal != null) {
                try {
                    Glide.with(applicationContext).load(carModel?.imagenFrontal).into(binding.circleImageFotoFrontal!!)
                }catch (e: Exception){  }
        }
        if (carModel?.imagenLateral != null) {
            try {
                Glide.with(applicationContext).load(carModel?.imagenLateral).into(binding.circleImageFotoLateral!!)
            }catch (e: Exception){  }
        }
        if (carModel?.imagenDocumento != null) {
            try {
                Glide.with(applicationContext).load(carModel?.imagenDocumento).into(binding.imageViewDocumentoVehiculoFontal!!)
            }catch (e: Exception){  }
        }
        if (carModel?.imagenDocumentoTrasero != null) {
            try {
                Glide.with(applicationContext).load(carModel?.imagenDocumentoTrasero).into(binding.imageViewDocumentoVehiculoTrasero!!)
            }catch (e: Exception){  }
        }

        binding.textFieldPlaca.setText(carModel.placa)
        binding.textFielMarca.setText(carModel.bland)
        binding.textFielAsisentos.setText(carModel.pasajeros!!.toString())
        binding.switchMaletero.setChecked(carModel.malero!!)
        binding.checkBoxTecnomecanica.isChecked=carModel.tecnomecanica!!
        binding.checkBoxSoat.isChecked=carModel.SOAT!!

        //estblecer el valor del spinner
        val opcionesArray = applicationContext.resources.getStringArray(R.array.tipoVehiculos)
        val indiceValorPredeterminado = opcionesArray.indexOf(carModel.tipo)
        if (indiceValorPredeterminado != -1) {
            // Establecer el valor predeterminado en el Spinner
            binding?.spinner?.setSelection(indiceValorPredeterminado)
        }
        binding.imageViewCarColor.setColorFilter(carModel.color!!)

    }


}
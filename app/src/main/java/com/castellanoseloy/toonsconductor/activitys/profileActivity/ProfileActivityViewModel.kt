package com.castellanoseloy.toonsconductor.activitys.profileActivity

import android.util.Log
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.castellanoseloy.toonsconductor.databinding.ActivityProfileBinding
import com.castellanoseloy.toonsconductor.modelos.ConductorModelo
import com.optic.uberclonedriverkotlin.providers.DriverProvider
import java.io.File

class ProfileActivityViewModel : ViewModel() {

    private val _elementosActualizar = MutableLiveData<Int>()
    val mensaje = MutableLiveData<String>()
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


    fun validarDatos(
        binding: ActivityProfileBinding,
        fotoPerfil: File?,
        documentoDelantero: File?,
        documentoTrasero: File?,
        licenciaFrontal: File?,
        licenciaTrasera: File?,
        fotoValidacion: File?,
        editarDatos: Boolean
    ): Boolean {

        var datosValidos = true

        if (!editarDatos) {
            if (fotoPerfil == null) {
                mensaje.value = "Se requiere una foto de perfil"
                datosValidos = false
            }
            if (documentoDelantero == null) {
                mensaje.value = "Se requiere una foto delantera del documento de identidad"
                datosValidos = false
            }
            if (documentoTrasero == null) {
                mensaje.value = "Se requiere una foto trasera del documento de identidad"
                datosValidos = false
            }
            if (licenciaFrontal == null) {
                mensaje.value = "Se requiere foto de la licencia de conduci frontal"
                datosValidos = false
            }
            if (licenciaTrasera == null) {
                mensaje.value = "Se requiere foto de la licencia de conducir trasera"
                datosValidos = false
            }
            if(binding.circleImageValidacion.isVisible){
                if (fotoValidacion == null) {
                    mensaje.value = "Se requiere foto de validacion de identidad"
                    datosValidos = false
                }
            }
        }
        if (binding.textFieldName.text.toString().isEmpty()) {
            datosValidos = false
            binding.textFieldName.setError("Obligatorio")
        }
        if (binding.textFieldLastname.text.toString().isEmpty()) {
            datosValidos = false
            binding.textFieldLastname.setError("Obligatorio")
        }
        if (binding.textFieldDocumento.text.toString().isEmpty()) {
            datosValidos = false
            binding.textFieldDocumento.setError("Obligatorio")
        }

        return datosValidos

    }

    fun verificarDocumento(
        driverActual: ConductorModelo,
        nuevoNumeroDocumento: String,
        tipoDocumento: String,
        callback: (Boolean) -> Unit
    ) {
        val driverProvider = DriverProvider()
        driverProvider.getConductoresPorDocumento(nuevoNumeroDocumento)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    if (querySnapshot != null) {
                        val conductores =
                            querySnapshot.documents.mapNotNull { it.toObject(ConductorModelo::class.java) }
                        // 'conductores' ahora contiene una lista de objetos ConductorModelo que coinciden con el número de documento
                        for (conductor in conductores) {
                            if (driverActual.id.toString() != conductor.id.toString() && tipoDocumento == conductor.tipoDocumento ){
                                // Realiza las acciones que necesites con cada objeto ConductorModelo
                                Log.d("Firebase", "Conductor encontrado: ${conductor.id.toString()}")
                                callback(true)
                                return@addOnCompleteListener
                            }

                        }
                    }
                    callback(false)
                } else {
                    Log.e("Firebase", "Error al obtener el resultado de la consulta")
                }
            }
    }


}
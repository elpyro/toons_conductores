package com.castellanoseloy.toonsconductor.activitys.profileActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileActivityViewModel : ViewModel() {

     private val _elementosActualizar = MutableLiveData<Int>()
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
}
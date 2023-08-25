package com.castellanoseloy.toonsconductor.activitys.mapActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapaActivityViewModel :ViewModel(){

    private val _currentTimerValue = MutableLiveData<Long>()
    val currentTimerValue: LiveData<Long>
        get() = _currentTimerValue

    // Otras propiedades y funciones del ViewModel...

    // Función para actualizar el valor del temporizador
    fun updateCurrentTimerValue(value: Long) {
        _currentTimerValue.value = value
    }

}
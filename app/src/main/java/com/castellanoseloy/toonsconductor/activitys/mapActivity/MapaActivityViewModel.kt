package com.castellanoseloy.toonsconductor.activitys.mapActivity

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.castellanoseloy.toonsconductor.modelos.VersionModel
import com.castellanoseloy.toonsconductor.proveedor.VersionControlProvider



class MapaActivityViewModel :ViewModel(){

    private var _desActualizado=MutableLiveData<VersionModel>()
    val currentdesActualizado: LiveData<VersionModel>
        get() = _desActualizado

    private val _currentTimerValue = MutableLiveData<Long>()
    val currentTimerValue: LiveData<Long>
        get() = _currentTimerValue

    // Función para actualizar el valor del temporizador
    fun updateCurrentTimerValue(value: Long) {
        _currentTimerValue.value = value
    }

    fun obtenerInformacionDeVersion(contexto: Context): Pair<String, Int>? {
        return try {
            val pInfo: PackageInfo = contexto.packageManager.getPackageInfo(contexto.packageName, 0)
            val versionName = pInfo.versionName
            val versionCode = pInfo.versionCode
            Log.d("vesion","El nombre de la version es: $versionName y el vesion code: $versionCode")
            Pair(versionName, versionCode)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    fun verificarVesionActualizada(mapaActivity: MapaActivity) {
        val version=obtenerInformacionDeVersion(mapaActivity)
        val versionControl=VersionControlProvider()
        versionControl.getVersionActual().addOnSuccessListener { document ->
            if (document.exists()) {
                val versionActual=document.toObject(VersionModel::class.java)
                if(version?.second!! < versionActual?.versionCode!!) _desActualizado.value=versionActual!!
            }
        }
    }

}
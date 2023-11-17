package com.castellanoseloy.toonsconductor.utils


import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.R
import java.util.concurrent.TimeUnit

class Preferencias(private val context: Context) {

    private var navegadorSeleccionado: Int = R.id.NavegadorNinguno
    private var sonidos: Boolean = true
    private lateinit var nequi: String
    private var ultimaModificacionNequi: Long = 0L

    private val PREFS_NAME = "MyPrefsFile"
    private val PREF_SONIDOS = "sonidos"
    private val PREF_NAVEGADOR = "navegador"
    private val PREF_NEQUI = "nequi"
    private val PREF_ULTIMA_MODIFICACION_NEQUI = "ultimaModificacionNequi"

    data class PreferenciasModel(
        val navegadorSeleccionado: Int,
        val sonidos: Boolean,
        val nequi: String,
        val ultimaModificacionNequi:Long
    )

    fun cargarPreferencias(): PreferenciasModel {
        val settings = context.getSharedPreferences(PREFS_NAME, AppCompatActivity.MODE_PRIVATE)

        sonidos = settings.getBoolean(PREF_SONIDOS, true)
        navegadorSeleccionado = settings.getInt(PREF_NAVEGADOR, R.id.NavegadorNinguno)
        nequi = settings.getString(PREF_NEQUI, "").toString()
        ultimaModificacionNequi = settings.getLong(PREF_ULTIMA_MODIFICACION_NEQUI, 0L)

        return PreferenciasModel(navegadorSeleccionado, sonidos, nequi,ultimaModificacionNequi)
    }

    fun guardarPreferencia(preferencia: String, checkedValue: Any) {
        val settings = context.getSharedPreferences(PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = settings.edit()

        when (preferencia) {
            PREF_NEQUI -> {
                    val ahora = System.currentTimeMillis()
                    editor.putString(preferencia, checkedValue.toString())
                    editor.putLong(PREF_ULTIMA_MODIFICACION_NEQUI, ahora)
                    Utilidades.mostrarToast("Nequi cambiado",context)
            }
            else -> {
                when (checkedValue) {
                    is Int -> editor.putInt(preferencia, checkedValue)
                    is Boolean -> editor.putBoolean(preferencia, checkedValue)
                    else -> throw IllegalArgumentException("Tipo de dato no soportado: ${checkedValue.javaClass.simpleName}")
                }
            }
        }

        editor.apply()
    }
}
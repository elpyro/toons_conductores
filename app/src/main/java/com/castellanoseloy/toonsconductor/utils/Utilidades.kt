package com.castellanoseloy.toonsconductor.utils

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Utilidades {

    fun convertLocationToLatLng(location: Location): LatLng {
        val latitude = location.latitude
        val longitude = location.longitude
        return LatLng(latitude, longitude)
    }

    fun String.formatoMoneda(): String {
        val formatoMoneda = DecimalFormat("###,###") // Dos decimales
        formatoMoneda.decimalFormatSymbols = DecimalFormatSymbols(Locale("es", "CO"))
        val valorDouble = this.toDoubleOrNull() ?: return this // Retorna el string original si no se puede convertir a double

        if (valorDouble == 0.0) {
            return "0"
        }

        val formattedValue = formatoMoneda.format(valorDouble)

        // Verifica si el resultado tiene ",00" o ".00" al final de la cadena y si es así, lo reemplaza por "0"
        val resultWithoutDecimals = formattedValue.replace(Regex("[,.]00$"), "")

        // Verifica si el resultado tiene ".0" al final de la cadena y si es así, lo reemplaza por ""
        return resultWithoutDecimals.replace(".0$", "")+"$"
    }

}
package com.castellanoseloy.toonsconductor.utils

import android.location.Location
import com.google.android.gms.maps.model.LatLng

object Utilidades {

    fun convertLocationToLatLng(location: Location): LatLng {
        val latitude = location.latitude
        val longitude = location.longitude
        return LatLng(latitude, longitude)
    }

}
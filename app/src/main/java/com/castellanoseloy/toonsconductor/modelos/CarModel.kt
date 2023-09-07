package com.castellanoseloy.toonsconductor.modelos

import com.beust.klaxon.*

private val klaxon = Klaxon()

data class CarModel (
    val id: String? = null,
    val estado: String? = null,
    val idDriver: String? =null,
    val bland: String? = null,
    val tipo: String? = null,
    val placa: String? = null,
    val imagenFrontal: String? = null,
    val imagenLateral: String? = null,
    val imagenDocumento: String? = null,
    val color: Int? = null,
    val pasajeros: Int? = null,
    val malero:Boolean? =null

) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<CarModel>(json)
    }
}
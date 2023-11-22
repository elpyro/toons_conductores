package com.castellanoseloy.toonsconductor.modelos
import com.beust.klaxon.Klaxon

private val klaxon = Klaxon()

data class TarifasModelo (
    val totalViaje: Double? = null,
    val totalComisionConductor: Double? = null, //corresponde al total a cobrar el conductor despues que se descuantan todos los cargaro
    val totalComisionReferido: Double? = null,//corresponde al total a cobrar el referidor despues que se descuantan todos los cargaro
    val totalComisionApp: Double? = null,
    val impuestoNacional: Double? = null,
    val porcenajeComisionApp: Double? = null,
    val comisionApp: Double? = null,
    val porcenajeComisionReferido: Double? = null, //porcentaje de recargo del referido
    val tarifaComisionReferido: Double? = null,  //precio de recargo del referido

) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<BookingModel>(json)
    }
}
package com.castellanoseloy.toonsconductor.modelos
import com.beust.klaxon.*

private val klaxon = Klaxon()

data class ConductorModelo (
    val id: String? = null,
    val nombre: String? = null,
    val apellido: String? = null,
    val tipoDocumento: String? = null,
    val numeroDocumento: String? = null,
    val correo: String? = null,
    val telefono: String? = null,
    var imagenPerfil: String? = null,
    var imagenDocumento: String? = null,
    var imagenDocumentoTrasero: String? = null,
    var imagenLicenciaDelantera: String? = null,
    var imagenLicenciaTrasera: String? = null,
    var imagenFotoValidacion: String? = null,
    val validacion: String? =null,
    var vehiculo:String? = null,
    var saldo:Double?=null,
    var clave:String? =null
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<ConductorModelo>(json)
    }
}
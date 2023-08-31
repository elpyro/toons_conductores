package com.castellanoseloy.toonsconductor.modelos
import com.beust.klaxon.*

private val klaxon = Klaxon()

data class ConductorModelo (
    val id: String? = null,
    val nombre: String? = null,
    val apellido: String? = null,
    val correo: String? = null,
    val telefono: String? = null,
    var imagenPerfil: String? = null,
    var imagenDocumento: String? = null,
    var imagenLicencia: String? = null,
    val validacion: String? =null
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<ConductorModelo>(json)
    }
}
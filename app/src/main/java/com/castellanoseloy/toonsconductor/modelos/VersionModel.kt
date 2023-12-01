package com.castellanoseloy.toonsconductor.modelos

import com.beust.klaxon.Klaxon
import java.sql.Timestamp

private val klaxon = Klaxon()
data  class VersionModel (
    var versionName:String? = null,
    var obligatory:Double?=null,
    var versionCode:Int? =null,
    val fecha: Timestamp? =null,
) {
        public fun toJson() = klaxon.toJsonString(this)
        companion object {
            public fun fromJson(json: String) = klaxon.parse<ConductorModelo>(json)
        }
    }

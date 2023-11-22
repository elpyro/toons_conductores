package com.castellanoseloy.toonsconductor.activitys.utils

import android.content.Context
import android.media.MediaPlayer
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.utils.Preferencias


class Sonidos(private val context: Context) {
        val preferencias=Preferencias(context)
    fun crearTonoInformativo() {
        if (preferencias.cargarPreferencias().sonidos) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.musica_tono)
            mediaPlayer.start()
        }
    }

    fun crearTono() {
        if (preferencias.cargarPreferencias().sonidos) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.musica_informativo)
            mediaPlayer.start()
        }
    }

    fun crearTonoPuntoEncuentro() {
        if (preferencias.cargarPreferencias().sonidos) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.punto_de_encuentro)
            mediaPlayer.start()
        }
    }

    fun crearTonoBuenViaje() {
        if (preferencias.cargarPreferencias().sonidos) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.sonido_buen_viaje)
            mediaPlayer.start()
        }
    }
    fun crearTonoLLegandoAlDestino() {
        if (preferencias.cargarPreferencias().sonidos) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.sonido_llegando_al_destino)
            mediaPlayer.start()
        }
    }

    fun crearTonoCancelado() {
        if (preferencias.cargarPreferencias().sonidos) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.tono_alerta_cancelacion)
            mediaPlayer.start()
        }
    }
}
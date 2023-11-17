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
}
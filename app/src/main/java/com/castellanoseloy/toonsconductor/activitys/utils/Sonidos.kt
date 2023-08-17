package com.castellanoseloy.toonsconductor.activitys.utils

import android.content.Context
import android.media.MediaPlayer
import com.castellanoseloy.toonsconductor.R


class Sonidos {

    fun crearTonoInformativo(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.musica_tono)
        mediaPlayer.start()
    }

    fun crearTono(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.musica_informativo)
        mediaPlayer.start()
    }
}
package com.castellanoseloy.toonsconductor.activitys.viajeCancelado

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.activitys.utils.Sonidos

class ViajeCancelado : AppCompatActivity() {
    private var sonidos= Sonidos(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viaje_cancelado)

        sonidos.crearTonoCancelado()
    }

}
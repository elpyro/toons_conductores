package com.castellanoseloy.toonsconductor.activitys.configuracion

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.databinding.ActivityAddCarBinding
import com.castellanoseloy.toonsconductor.databinding.ActivityConfiguracionBinding

class Configuracion : AppCompatActivity() {
    private lateinit var binding:ActivityConfiguracionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityConfiguracionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Configuración"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(Color.WHITE)


    }
}
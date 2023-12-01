package com.castellanoseloy.toonsconductor.activitys.carList

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.castellanoseloy.toonsconductor.activitys.addCar.AddCar
import com.castellanoseloy.toonsconductor.databinding.ActivityCarListBinding
import com.castellanoseloy.toonsconductor.modelos.CarModel
import com.castellanoseloy.toonsconductor.proveedor.CarProvider
import com.castellanoseloy.toonsconductor.services.DatosPersitidos
import com.optic.uberclonedriverkotlin.providers.DriverProvider

class CarList : AppCompatActivity() ,  CarListAdapter.OnItemClickListener{

    private lateinit var binding: ActivityCarListBinding
    private val listaCarros: MutableList<CarModel> = mutableListOf()
    private lateinit var adaptador:CarListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCarListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val linearLayoutManager = LinearLayoutManager(this)
        binding.recyclerCarList.layoutManager = linearLayoutManager

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Mis vehiculos"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(Color.WHITE)


        listeners()
    }

    private fun obtenerCars() {
        if(DatosPersitidos.datosConductor?.id ==null){
            return
        }

        val carProvider=CarProvider()
        carProvider.getCarByDriverId(DatosPersitidos.datosConductor?.id!!)
            .addOnCompleteListener {listaCars->
                if (listaCars.isSuccessful) {
                    val carrosEncontrados = listaCars.result?.toObjects(CarModel::class.java)
                    if (!carrosEncontrados.isNullOrEmpty()) {
                        // Aquí puedes hacer algo con los carros encontrados
                        for (carro in carrosEncontrados) {
                            listaCarros.add(carro)
                        }
                            cargarRecyclerView()
                    } else {
                        // No se encontraron carros para el conductor con el ID especificado
                        Log.d("Carros", "Ningún carro encontrado para el conductor")
                    }
                } else {
                    // Manejar el error si la consulta falla
                    Log.e("Error", "Error al buscar carros: ${listaCars.exception?.message}")
                }
            }
    }

    private fun cargarRecyclerView() {
        val gridLayoutManager = GridLayoutManager(this, 1)
        binding!!.recyclerCarList.layoutManager = gridLayoutManager


        if (listaCarros.isNotEmpty()) {
            adaptador = CarListAdapter(listaCarros, this)
            binding.recyclerCarList.adapter = adaptador
        }

    }

    override fun onResume() {
        super.onResume()
        listaCarros.clear()
        obtenerCars()
    }

    private fun listeners() {
        binding.imageViewAddCard.setOnClickListener{  goToAddCar() }
    }

    private fun goToAddCar() {
        val i = Intent(this@CarList, AddCar::class.java)
        startActivity(i)
    }

    override fun onSwitchClick(car: CarModel) {
        val driverProvider=DriverProvider()
        driverProvider.updateCar(car.id!!)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    DatosPersitidos.datosConductor?.vehiculo = car.id
                    DatosPersitidos.datosCarroEnUso= car
                    adaptador.notifyDataSetChanged()
                }
            }
    }

    override fun onCarItemClick(car: CarModel) {
        //editar carro
        val i = Intent(this@CarList, AddCar::class.java)
        i.putExtra("carModelo", car)
        startActivity(i)
    }

}
package com.castellanoseloy.toonsconductor.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.activitys.carList.CarList
import com.castellanoseloy.toonsconductor.activitys.configuracion.Configuracion
import com.castellanoseloy.toonsconductor.activitys.historial.HistoriesActivity
import com.castellanoseloy.toonsconductor.activitys.mainActivity.MainActivity
import com.castellanoseloy.toonsconductor.activitys.profileActivity.ProfileActivity
import com.castellanoseloy.toonsconductor.activitys.saldo.Saldo
import com.castellanoseloy.toonsconductor.proveedor.Autenticacion
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.optic.uberclonedriverkotlin.providers.DriverProvider


class ModalBottomSheetMenu: BottomSheetDialogFragment() {
    companion object {
        const val TAG = "ModalBottomSheetMenu"
    }



    val authProvider = Autenticacion()

    private var linearLayoutConfinguracion: LinearLayout ? = null
    private var linearLayoutCar: LinearLayout ? = null
    private var textViewUsername: TextView? = null
    private var linearLayoutLogout: LinearLayout? = null
    private var linearLayoutProfile: LinearLayout? = null
    private var linearLayoutHistory: LinearLayout? = null
    private var linearLayoutRecagas: LinearLayout?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modal_bottom_sheet_menu, container, false)

        textViewUsername = view.findViewById(R.id.textViewUsername)
        linearLayoutLogout = view.findViewById(R.id.linearLayoutLogout)
        linearLayoutProfile = view.findViewById(R.id.linearLayoutProfile)
        linearLayoutHistory = view.findViewById(R.id.linearLayoutHistory)
        linearLayoutCar = view.findViewById(R.id.linearLayoutCar)
        linearLayoutConfinguracion = view.findViewById(R.id.linearLayoutConfiguracion)
        linearLayoutRecagas= view.findViewById(R.id.linearLayoutRecagas)

        getDriver()

        listeners()

        return view
    }

    private fun listeners() {
        linearLayoutLogout?.setOnClickListener { goToMain() }
        linearLayoutProfile?.setOnClickListener { goToProfile() }
        linearLayoutHistory?.setOnClickListener { goToHistories() }
        linearLayoutCar?.setOnClickListener{goToCarList()}
        linearLayoutConfinguracion?.setOnClickListener{ goToConfig()}
        linearLayoutRecagas?.setOnClickListener{ goToSaldo()}
    }

    private fun goToSaldo() {
        val i = Intent(activity, Saldo::class.java)
        startActivity(i)
    }

    private fun goToConfig() {
        val i = Intent(activity, Configuracion::class.java)
        startActivity(i)
    }

    private fun goToProfile() {
        val i = Intent(activity, ProfileActivity::class.java)
        startActivity(i)
    }

    private fun goToCarList() {
        val i = Intent(activity, CarList::class.java)
        startActivity(i)
    }

    private fun goToHistories() {
        val i = Intent(activity, HistoriesActivity::class.java)
        startActivity(i)
    }

    private fun goToMain() {
        val i = Intent(activity, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
        authProvider.logout()
    }

    private fun getDriver() {
        textViewUsername?.text = DriverProvider.datosConductor?.nombre +" "+DriverProvider.datosConductor?.apellido
        }
}
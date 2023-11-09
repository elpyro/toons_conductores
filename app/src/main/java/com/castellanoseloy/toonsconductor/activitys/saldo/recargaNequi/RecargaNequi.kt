package com.castellanoseloy.toonsconductor.activitys.saldo.recargaNequi

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonscliente.objetos.Utilidades.eliminarPuntosComasLetras
import com.castellanoseloy.toonscliente.objetos.Utilidades.escribirFormatoMoneda
import com.castellanoseloy.toonsconductor.api.NequiOAuth2Manager
import com.castellanoseloy.toonsconductor.databinding.ActivityRecargaNequiBinding
import com.castellanoseloy.toonsconductor.modelos.RecargasModelo
import com.castellanoseloy.toonsconductor.proveedor.RecargasProvider
import com.optic.uberclonedriverkotlin.providers.DriverProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.Date
import java.util.Random

class RecargaNequi : AppCompatActivity() {
    private lateinit var idTransaccion: String
    private lateinit var telefono: String
    private lateinit var codigoVerificacion: String
    private lateinit var recarga: String
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivityRecargaNequiBinding
    val recargasProvider=RecargasProvider()
    var nequiOAuth2Manager = NequiOAuth2Manager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecargaNequiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listeners()
//        binding?.editTextNumeroNequi?.setText(DriverProvider.datosConductor?.telefono?: "")
        binding?.editTextNumeroNequi?.setText("3720002150")
    }

    private fun listeners() {
        binding?.botonRecargar?.setOnClickListener { verificacion() }

        binding?.editTextNumeroNequi?.setOnClickListener {
            binding.editTextNumeroNequi.selectAll()
        }
        binding?.editTextRecarga?.setOnClickListener{
            binding?.editTextRecarga?.selectAll()
        }
        binding?.editTextRecarga?.escribirFormatoMoneda()

    }

        private fun verificacion() {
            recarga=binding.editTextRecarga.text.toString().trim().replace(" ", "").eliminarPuntosComasLetras()
            codigoVerificacion = Utilidades.generarMessageID() // Genera un MessageID aleatorio
            telefono=binding.editTextNumeroNequi.text.toString().trim().replace(" ", "")

            val longitud = telefono.length

            if (longitud!=10){
                binding.editTextNumeroNequi.setError("El telefono debe tener 10 digitos")
                return
            }
            if(recarga.toInt()<3000){
                binding.editTextRecarga.setError("La recarga debe ser superior a 3000$")
                return
            }

            mostrarDialogProgreso()
            creaPeticionNequi()

    }

    private fun crearRegistroFirebase(codigoVerificacion:String) {
        idTransaccion=Utilidades.generarIdUnico()
        val datosRecarga=RecargasModelo(
            id =idTransaccion ,
            idDriver = DriverProvider.datosConductor!!.id,
            banco ="Nequi",
            cuenta = telefono,
            codigoVerificacion =codigoVerificacion,
            valor = recarga,
            estado = "Pendiente",
            fecha = Date()
        )


        recargasProvider.create(datosRecarga)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("Pruebas Nequi", "Registro de firebase creado")
                    operacionExitosa(codigoVerificacion)
                }else{
                    progressDialog.dismiss()
                    Utilidades.mostrarToast("Ha ocurrido un error, intente nuevamente", this)
                    Log.e("Pruebas Nequi", "Error en firebase")
                }
            }
    }

    private fun creaPeticionNequi() {


        runOnUiThread {
            Log.d("Pruebas Nequi", "Creando Token")
            nequiOAuth2Manager.getAccessToken { accessToken ->
                if (accessToken != null) {
                    Log.d("Pruebas Nequi", "Token de autenticacion: $accessToken")
                    verificarEstadoDePago(accessToken)

                } else {
                    //eliminar el registro de firebase
                    if(idTransaccion.isNotEmpty())recargasProvider.deleteRecargaPorId(idTransaccion)
                    // Maneja el error de autenticación
                    progressDialog.dismiss()
                    Utilidades.mostrarToast(
                        "Error de autenticacion",
                        this@RecargaNequi
                    )
                    Log.e("Pruebas Nequi", "Error de autenticacion")
                }
            }
        }
    }



    fun verificarEstadoDePago(accessToken:String) {
        val url = "https://api.sandbox.nequi.com/payments/v2/-services-paymentservice-unregisteredpayment"

        val fecha=Utilidades.obtenerFechaActualEnFormatoISO8601()
        val clientId=nequiOAuth2Manager.clientId
        val apiKey = nequiOAuth2Manager.apiKey
        val referencia1=nequiOAuth2Manager.referencia1
        Log.d("Pruebas Nequi", "Id de la transaccion $codigoVerificacion")

        val jsonBody = """
        {
          "RequestMessage": {
            "RequestHeader": {
              "Channel": "PNP04-C001",
              "RequestDate": $fecha,
              "MessageID": "$codigoVerificacion",
              "ClientID": "$clientId",
              "Destination": {
                "ServiceName": "PaymentsService",
                "ServiceOperation": "unregisteredPayment",
                "ServiceRegion": "C001",
                "ServiceVersion": "1.2.0"
              }
            },
            "RequestBody": {
              "any": {
                "unregisteredPaymentRQ": {
                  "phoneNumber": "$telefono",
                  "code": "NIT_1",
                  "value": "$recarga",
                  "reference1": "reference1",
                  "reference2": "reference2",
                  "reference3": "reference3"
                }
              }
            }
          }
        }
    """.trimIndent()

        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, jsonBody)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", accessToken)
            .addHeader("x-api-key", apiKey)
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        Log.d("Pruebas Nequi", "Respuesta : ${request.body.toString()}")
        var statusCode = ""

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                runOnUiThread {
                    progressDialog.dismiss()
                }
                val responseBody = response.body?.string()
                val jsonResponse = JSONObject(responseBody)

                val responseMessage = jsonResponse.optJSONObject("ResponseMessage")
                val responseHeader = responseMessage?.optJSONObject("ResponseHeader")
                statusCode = responseHeader?.optJSONObject("Status")?.optString("StatusCode")!!

                Log.d("Pruebas Nequi", "Respuesta : $responseBody")

                if (statusCode == "0") {
                    // Éxito
                    Log.d("Pruebas Nequi", "Respuesta exitosa")

                    // Obtener el transactionId de la respuesta
                    val responseBody = jsonResponse.optJSONObject("ResponseMessage")?.optJSONObject("ResponseBody")
                    val transactionId = responseBody?.optJSONObject("any")?.optJSONObject("unregisteredPaymentRS")?.optString("transactionId")
                    Log.d("Pruebas Nequi", "transaccion nequi $transactionId")

                    runOnUiThread {
                        crearRegistroFirebase(transactionId!!)
                    }
                } else {
                    // Error
                    if(idTransaccion.isNotEmpty())recargasProvider.deleteRecargaPorId(idTransaccion)
                    Log.e("Pruebas Nequi", "Error en la operación: $statusCode - ${responseHeader?.optString("StatusDesc")}")
                    // Muestra un Toast con el mensaje de error
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error en la operación verifique los datos", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
                val errorCode = response.code // Código de estado HTTP
                val errorMessage = response.message // Mensaje de error
                Log.e("Pruebas Nequi", "Error en la solicitud HTTP: $errorCode - $errorMessage")

                // Obtener el cuerpo de la respuesta
                val errorBody = response.body?.string()
                Log.d("Pruebas Nequi", "Cuerpo de la respuesta de error: $errorBody")

                // Obtener otros encabezados de respuesta si es necesario
                val headers = response.headers
                Log.d("Pruebas Nequi", "Encabezados de la respuesta de error: $headers")
                runOnUiThread {
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext, "Error de conexion", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun operacionExitosa(transactionIdNequi: String?) {
        val i = Intent(this, InformacionNequi::class.java)
        i.putExtra("telefono",telefono)
        i.putExtra("codigoVerificacion",transactionIdNequi)
        i.putExtra("recarga",recarga)
        startActivity(i)
    }

    fun mostrarDialogProgreso(){
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Conectando con Nequi...")
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Espere un momento...")
        progressDialog.show()
    }
}
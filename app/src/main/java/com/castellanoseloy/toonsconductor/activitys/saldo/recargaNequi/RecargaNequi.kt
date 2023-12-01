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
import com.castellanoseloy.toonsconductor.api.NequiApiService
import com.castellanoseloy.toonsconductor.api.NequiOAuth2Manager
import com.castellanoseloy.toonsconductor.databinding.ActivityRecargaNequiBinding
import com.castellanoseloy.toonsconductor.modelos.TransaccionesModelo
import com.castellanoseloy.toonsconductor.proveedor.TransaccionesProvider
import com.castellanoseloy.toonsconductor.services.DatosPersitidos
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


import java.util.Date

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException

class RecargaNequi : AppCompatActivity() {
    private lateinit var idTransaccion: String
    private lateinit var telefono: String
    private lateinit var codigoVerificacion: String
    private lateinit var recarga: String
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivityRecargaNequiBinding
    val transaccionesProvider = TransaccionesProvider()
    var nequiOAuth2Manager = NequiOAuth2Manager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecargaNequiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listeners()
//        binding?.editTextNumeroNequi?.setText(DatosPersitidos.datosConductor?.telefono?: "")
        binding?.editTextNumeroNequi?.setText("3720002150")

    }


    private fun listeners() {
        binding?.botonRecargar?.setOnClickListener { verificacion() }

        binding?.editTextNumeroNequi?.setOnClickListener {
            binding.editTextNumeroNequi.selectAll()
        }
        binding?.editTextRecarga?.setOnClickListener {
            binding.textFieldHit.hint = ""
            binding?.editTextRecarga?.selectAll()
        }
        binding?.editTextRecarga?.escribirFormatoMoneda()

    }

    private fun verificacion() {
        recarga = binding.editTextRecarga.text.toString().trim().replace(" ", "")
            .eliminarPuntosComasLetras()
        telefono = binding.editTextNumeroNequi.text.toString().trim().replace(" ", "")

        codigoVerificacion = Utilidades.generarMessageID() // Genera un MessageID aleatorio

        val longitud = telefono.length

        if (longitud != 10) {
            binding.editTextNumeroNequi.setError("El telefono debe tener 10 digitos")
            return
        }
        if (recarga.isNotEmpty()) {
            if (recarga.toInt() < 3000) {
                binding.editTextRecarga.setError("La recarga debe ser superior a 3000$")
                return
            }
        } else {
            binding.editTextRecarga.setError("La recarga debe ser superior a 3000$")
            return
        }

        mostrarDialogProgreso()
        creaPeticionNequi()

    }

    private fun crearRegistroFirebase(codigoVerificacion: String) {
        idTransaccion = Utilidades.generarIdUnico()
        val datosRecarga = TransaccionesModelo(
            id = idTransaccion,
            idDriver = DatosPersitidos.datosConductor!!.id,
            banco = "Nequi",
            cuenta = telefono,
            codigoVerificacion = codigoVerificacion,
            valor = recarga,
            estado = "Pendiente",
            fecha = Date()
        )

        transaccionesProvider.create(datosRecarga)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("Pruebas Nequi", "Registro de firebase creado")
                    operacionExitosa(codigoVerificacion)
                } else {
                    progressDialog.dismiss()
                    Utilidades.mostrarToast("Ha ocurrido un error, intente nuevamente", this)
                    Log.e("Pruebas Nequi", "Error en firebase")
                }
            }
    }

    private fun creaPeticionNequi() {


        Log.d("Pruebas Nequi", "Creando Token")
        nequiOAuth2Manager.getAccessToken { accessToken ->
            if (accessToken != null) {
                Log.d("Pruebas Nequi", "Token de autenticacion: $accessToken")

//                val solicitudNequi=configurarSolicituPushNequi(accessToken)
//                ProcesarSolicitud(solicitudNequi)
//
                retrofit2(configurarSolicituPushNequi(),accessToken)

            } else {
                runOnUiThread {
                    progressDialog.dismiss()
                    Utilidades.mostrarToast(
                        "Error de autenticacion",
                        applicationContext
                    )
                    Log.e("Pruebas Nequi", "Error de autenticacion")
                }
            }

        }
    }

    private fun retrofit2(requestBody: RequestBody, token: String) {
        val timeoutInSeconds = 120L
        val client = OkHttpClient.Builder()
            .connectTimeout(timeoutInSeconds, TimeUnit.SECONDS)
            .writeTimeout(timeoutInSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutInSeconds, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.sandbox.nequi.com") // Reemplaza con la URL correcta
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val nequiApiService = retrofit.create(NequiApiService::class.java)
        val apiKey = nequiOAuth2Manager.apiKey
        val call = nequiApiService.realizarPago(token, apiKey, requestBody)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val responseBody = response.body()

                if (response.isSuccessful) {
                    if (responseBody != null) {
                        val responseBodyString = responseBody.string()
                        val jsonResponse = JSONObject(responseBodyString)

                        val responseMessage = jsonResponse.optJSONObject("ResponseMessage")
                        val responseHeader = responseMessage?.optJSONObject("ResponseHeader")
                        var statusCode = responseHeader?.optJSONObject("Status")?.optString("StatusCode")!!

                        Log.d("Pruebas Nequi", "Respuesta : $responseBodyString")

                        if (statusCode == "0") {
                            Log.d("Pruebas Nequi", "Respuesta exitosa o devuelta por error de usuario invalido")

                            val responseBody = jsonResponse.optJSONObject("ResponseMessage")
                                ?.optJSONObject("ResponseBody")
                            val transactionId = responseBody?.optJSONObject("any")
                                ?.optJSONObject("unregisteredPaymentRS")?.optString("transactionId")
                            Log.d("Pruebas Nequi", "transaccion nequi creada TransaccionId: $transactionId")

                            runOnUiThread {
                                crearRegistroFirebase(transactionId!!)  //transaccion exitosa, dejar registro
                            }
                        } else if (statusCode == "3-451" || statusCode == "11-9L" || statusCode == "11-37L" || statusCode == "2-CCSB000012" || statusCode == "2-CCSB000013") {
                            Log.e("Pruebas Nequi", "Respuesta exitosa, error usuario no registrado")
                            runOnUiThread {
                                progressDialog.dismiss()
                                Toast.makeText(
                                    applicationContext,
                                    "Numero de telefono errado",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            errorNequi()
                        }
                    } else {
                        errorNequi()
                    }
                } else {
                    errorNequi()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                errorNequi(t)

                if (t is SocketTimeoutException) {
                    Log.e("Pruebas Nequi", "Timeout de la solicitud: ${t.message}", t)
                    // Manejar el timeout aquí
                } else {
                    Log.e("Pruebas Nequi", "Error en la solicitud: ${t.message}", t)
                    // Otro tipo de error
                }
            }
        })
    }

    private fun errorNequi(t: Throwable? =null) {
        runOnUiThread {
            progressDialog.dismiss()
            Toast.makeText(
                applicationContext,
                "Error: Nequi no responde ${t?.message} ${t?.cause}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

//
//    private fun procesarSolicitud(
//        request: Request
//    ) {
//
//        // Configurar un tiempo de espera máximo de 120 segundos
//        val timeoutInSeconds = 120L
//        val client = OkHttpClient.Builder().callTimeout(timeoutInSeconds, TimeUnit.SECONDS).build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.e(
//                    "Pruebas Nequi",
//                    "Error en la solicitud HTTP: ${e.message}, causa: ${e.cause}"
//                )
//
//                runOnUiThread {
//                    progressDialog.dismiss()
//                    Toast.makeText(
//                        applicationContext,
//                        "Nequi no responde",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                val responseBody = response.body?.string()
//                val jsonResponse = JSONObject(responseBody)
//
//                val responseMessage = jsonResponse.optJSONObject("ResponseMessage")
//                val responseHeader = responseMessage?.optJSONObject("ResponseHeader")
//                var statusCode = responseHeader?.optJSONObject("Status")?.optString("StatusCode")!!
//
//                Log.d("Pruebas Nequi", "Respuesta : $responseBody")
//
//                if (statusCode == "0") {
//                    Log.d("Pruebas Nequi", "Respuesta exitosa o devuelta por error de usuario invalido")
//
//                    val responseBody = jsonResponse.optJSONObject("ResponseMessage")
//                        ?.optJSONObject("ResponseBody")
//                    val transactionId = responseBody?.optJSONObject("any")
//                        ?.optJSONObject("unregisteredPaymentRS")?.optString("transactionId")
//                    Log.d("Pruebas Nequi", "transaccion nequi creada TransaccionId: $transactionId")
//
//                    runOnUiThread {
//                        crearRegistroFirebase(transactionId!!)  //transaccion exitosa, dejar registro
//                    }
//                } else if (statusCode == "3-451" || statusCode == "11-9L" || statusCode == "11-37L" || statusCode == "2-CCSB000012" || statusCode == "2-CCSB000013") {
//                    Log.e("Pruebas Nequi", "Respuesta exitosa, error usuario no registrado")
//                    runOnUiThread {
//                        progressDialog.dismiss()
//                        Toast.makeText(
//                            applicationContext,
//                            "Numero de telefono errado",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//
//                } else {
//
//                    runOnUiThread {
//                        progressDialog.dismiss()
//                        Toast.makeText(
//                            applicationContext,
//                            "Error: Nequi no responde",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//
//                }
//            }
//        })
//    }


    fun configurarSolicituPushNequi(): RequestBody {
        val url =
            "https://api.sandbox.nequi.com/payments/v2/-services-paymentservice-unregisteredpayment"

        val fecha = Utilidades.obtenerFechaActualEnFormatoISO8601()
        val clientId = nequiOAuth2Manager.clientId
        val apiKey = nequiOAuth2Manager.apiKey
        val referencia1 = nequiOAuth2Manager.referencia1


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
                  "reference1": "$referencia1",
                  "reference2": "reference2",
                  "reference3": "reference3"
                }
              }
            }
          }
        }
    """.trimIndent()

        val mediaType = "application/json".toMediaTypeOrNull()

        Log.d("Pruebas Nequi", "jsonBody : $jsonBody")

        return RequestBody.create(mediaType, jsonBody)

    }

    private fun operacionExitosa(transactionIdNequi: String?) {
        progressDialog.dismiss()

        finish()
        val i = Intent(this, InformacionNequi::class.java)
        i.putExtra("telefono", telefono)
        i.putExtra("codigoVerificacion", transactionIdNequi)
        i.putExtra("recarga", recarga)
        startActivity(i)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    fun mostrarDialogProgreso() {
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Conectando con Nequi...")
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Espere un momento...")
        progressDialog.show()
    }
}
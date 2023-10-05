package com.castellanoseloy.toonsconductor.objetos
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.text.SimpleDateFormat
import java.util.Date

object BodyUtils {
    // Canal para pagos con PUSH o sin registro
    private const val CHANNEL = "PNP04-C001"

    fun getBodyUnregisteredPayment(
        phone: Long,
        value: Int,
        code: String,
        reference1: String,
        reference2: String,
        reference3: String
    ): JsonObject {
        // Fecha de la petición
        val d = Date()
        val date = SimpleDateFormat("yyyy-MM-dd:HH-mm-ss").format(d)
        val miliseconds = System.currentTimeMillis().toString()
        val input = """
            {
                "RequestMessage": {
                    "RequestHeader": {
                        "Channel": "$CHANNEL",
                        "RequestDate": "$date",
                        "MessageID": "${miliseconds.substring(miliseconds.length - 9)}",
                        "ClientID": "$phone",
                        "Destination": {
                            "ServiceName": "PaymentsService",
                            "ServiceOperation": "unregisteredPayment",
                            "ServiceRegion": "C001",
                            "ServiceVersion": "1.0.0"
                        }
                    },
                    "RequestBody": {
                        "any": {
                            "generateCodeQRRQ": {
                                "phoneNumber": "$phone",
                                "code": "$code",
                                "value": "$value",
                                "reference1": "$reference1",
                                "reference2": "$reference2",
                                "reference3": "$reference3"
                            }
                        }
                    }
                }
            }
        """.trimIndent()

        val jsonParser = JsonParser()
        return jsonParser.parse(input).asJsonObject
    }
}

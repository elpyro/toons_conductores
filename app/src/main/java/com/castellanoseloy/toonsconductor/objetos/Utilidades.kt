package com.castellanoseloy.toonscliente.objetos

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.*

object Utilidades {



    fun String.eliminarAcentosTildes(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        val pattern = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        return pattern.replace(normalized, "").lowercase(Locale.getDefault())
    }

    fun String.eliminarPuntosComasLetras(): String {
        return this.replace(Regex("[^\\d.]"), "")
            .replace(".", "")
            .replace(",", "")
    }

    fun esperarUnSegundo(){
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun ocultarTeclado(context: Context,view: View) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun separarNumerosDelString(string: String): Pair<String, String?> {
        // Expresión regular para detectar números al final del string
        val regex = "(\\d+)$"
        val matchResult = Regex(regex).find(string)
        if (matchResult != null) {
            // Si hay números al final del string, separarlos del resto del string
            val numeros = matchResult.value
            val resto = string.removeSuffix(numeros)
            return Pair(resto, numeros)
        } else {
            // Si no hay números al final del string, devolver el string completo y null
            return Pair(string.trim(), null)
        }
    }


    fun obtenerImageUriParaCompartir(bitmap: Bitmap,context: Context): Uri {
        //otorga el fileprovinder para compartir
        val idImagen= generarIdUnico()
        val imagePath = "${context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/$idImagen.png"
        val imageFile = File(imagePath)

        // Guardar el bitmap como archivo en el almacenamiento externo
        imageFile.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }

        return FileProvider.getUriForFile(context, "com.example.ToonsEcomerce.fileprovider", imageFile)
    }
    fun obtenerFechaActual(): String {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")) // Especifica la configuración regional adecuada
        val fechaActual = Date()
        return formatoFecha.format(fechaActual)
    }

    fun obtenerHoraActual(): String {
        val formatoHora = SimpleDateFormat("HH:mm:ss", Locale("es", "ES")) // Especifica la configuración regional adecuada
        val horaActual = Date()
        return formatoHora.format(horaActual)
    }

    // 16. Validar si una cadena de texto es un email válido
    fun isValidEmail(email: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    fun obtenerFechaUnix():Long{
        // Obtener la fecha actual sin la hora
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val fecha = calendar.timeInMillis
        return fecha
    }

    fun convertirFechaAUnix(fecha: String): Long {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaObjeto = formatoFecha.parse(fecha)

        val calendar = Calendar.getInstance()
        calendar.time = fechaObjeto
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }



    fun convertirUnixAFecha(fechaUnix: Long): String {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val fecha = Date(fechaUnix)
        return formatoFecha.format(fecha)
    }

    fun String.eliminarEspacios(): String {
        return this.replace("\\s+".toRegex(), "")
    }

    fun EditText.escribirFormatoMoneda() {
        val textWatcher = object : TextWatcher {
            var isProgrammaticChange = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isProgrammaticChange && s != null) {
                    removeTextChangedListener(this)
                    val precio = s.toString().eliminarPuntosComasLetras()
                    setText(precio.formatoMonenda())
                    setSelection(text.length)
                    addTextChangedListener(this)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        addTextChangedListener(textWatcher)
    }

    fun String.formatoMonenda(): String? {
        val formatoMoneda = DecimalFormat("###,###.###")
        formatoMoneda.decimalFormatSymbols = DecimalFormatSymbols(Locale("es", "CO"))
        formatoMoneda.maximumFractionDigits = 2
        val valorDouble = this.toDoubleOrNull()
            ?: return this // Retorna el string original si no se puede convertir a double
        return formatoMoneda.format(valorDouble)
    }

    fun mostrarToast( message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // 2. Crear un AlertDialog básico
    fun showAlertDialog(
        context: Context,
        title: String,
        message: String,
        positiveButton: String,
        positiveButtonClickListener: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton, positiveButtonClickListener)
            .setCancelable(false)
            .show()
    }

    // 4. Obtener el estado de la conexión a Internet
    fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    // 7. Generar un ID único
    fun generarIdUnico(): String {
        return UUID.randomUUID().toString()
    }

    // 10. Convertir un Bitmap a un ByteArray
    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 20, stream)
        return stream.toByteArray()
    }

    fun obtenerBitmapDesdeImageView(imageView: ImageView): Bitmap? {
        val drawable = imageView.drawable ?: return null

        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }


    // 11. Convertir un ByteArray a un Bitmap
    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    //guarda la imagen el en dispositivo en cache para obtener una imagen uri
    fun guardarImagenEnDispositivo(context: Context, bitmap: Bitmap): Uri? {

        // Crear un archivo temporal en el almacenamiento interno
        val file = File.createTempFile(
            "tempImagen",
            ".jpg",
            context.cacheDir
        )

        val byteArray = bitmapToByteArray(bitmap)

        // Escribir el archivo jpeg en el archivo temporal
        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.write(byteArray)
        fileOutputStream.flush()
        fileOutputStream.close()

        // Obtener la URI del archivo temporal
        return Uri.fromFile(file)
    }

    fun crearLog(mensaje:String){
        Log.i("Información:", mensaje)
    }

     fun copyToClipboard(textoSeleccionado: String, context: Context) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Codigo Referido", textoSeleccionado)
        clipboardManager.setPrimaryClip(clipData)
    }

     fun calcularPorcentaje(precioSugerido: String, precio: String): Double {
        var porcentajeGanancia = 0.0
        if (!precio.equals("") && !precioSugerido.equals("")) {
            val ganancia = (precioSugerido.toDouble() - precio.toDouble()) / precio.toDouble() * 100
            val decimalFormat = DecimalFormat("#.#") // Formato con un solo decimal
            porcentajeGanancia = decimalFormat.format(ganancia).toDouble()
        }
        return porcentajeGanancia
    }

    fun iniciarVideoUrl(url: String, context: Context): VideoView {
        val videoView = VideoView(context)

        if (!url.isNullOrEmpty()) {
            val videoUrl = url

            val videoUri = Uri.parse(videoUrl)
            videoView.setVideoURI(videoUri)

            val mediaController = MediaController(context)
            videoView.setMediaController(mediaController)

            videoView.start()
        }

        return videoView
    }

    fun vefificarTamanoVideo(videoUri: Uri, requireActivity: FragmentActivity): Boolean {
        var permitido=false

        val videoSize = obtenerTamanoArchivo(videoUri,requireActivity)
        val maxVideoSize = 30  * 1024 * 1024 // Tamaño máximo permitido de 20 MB

        permitido = videoSize <= maxVideoSize

        return permitido
    }
    private fun obtenerTamanoArchivo(uri: Uri, requireActivity: FragmentActivity): Long {
        val fileDescriptor = requireActivity.contentResolver.openFileDescriptor(uri, "r")
        val fileSize = fileDescriptor?.statSize ?: -1
        fileDescriptor?.close()
        return fileSize
    }





}
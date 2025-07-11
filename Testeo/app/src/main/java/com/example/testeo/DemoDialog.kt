package com.example.testeo

import android.content.Context
import androidx.appcompat.app.AlertDialog

object DemoDialog {

    fun mostrar(context: Context) {
        val opciones = arrayOf("Alerta sonido", "Alerta distancia", "Alerta humedad", "Cerrar")

        AlertDialog.Builder(context)
            .setTitle("Demo de alertas")
            .setItems(opciones) { dialog, which ->
                when (which) {
                    0 -> AlertaManager.reproducirAlertaSonido(context)
                    1 -> AlertaManager.reproducirAlertaDistancia(context)
                    2 -> AlertaManager.reproducirAlertaHumedad(context)
                    3 -> dialog.dismiss()
                }
            }
            .setCancelable(true)
            .show()
    }
}

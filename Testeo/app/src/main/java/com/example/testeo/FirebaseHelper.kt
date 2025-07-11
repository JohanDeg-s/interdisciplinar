package com.example.testeo

import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseHelper {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun guardarPuntoConDialogo(context: Context, location: Location?, onResult: (Boolean) -> Unit) {
        if (location == null) {
            Toast.makeText(context, "Ubicación no disponible.", Toast.LENGTH_LONG).show()
            onResult(false)
            return
        }

        val lat = location.latitude
        val lon = location.longitude
        val opciones = arrayOf("Escaleras", "Baños", "Otro")

        AlertDialog.Builder(context)
            .setTitle("Selecciona tipo de punto")
            .setItems(opciones) { _, which ->
                val tipo = opciones[which]
                guardarPunto(context, lat, lon, tipo, onResult)
            }
            .setCancelable(true)
            .show()
    }

    private fun guardarPunto(
        context: Context,
        lat: Double,
        lon: Double,
        tipo: String,
        onResult: (Boolean) -> Unit
    ) {
        val punto = mapOf(
            "lat" to lat,
            "lon" to lon,
            "label" to tipo
        )

        database.child("puntos")
            .child(tipo)
            .push()
            .setValue(punto)
            .addOnSuccessListener {
                Toast.makeText(context, "Punto guardado: $tipo", Toast.LENGTH_SHORT).show()
                onResult(true)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al guardar punto: ${e.message}", Toast.LENGTH_LONG).show()
                onResult(false)
            }
    }
}

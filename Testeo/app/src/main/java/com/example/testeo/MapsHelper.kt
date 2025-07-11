package com.example.testeo

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

object MapsHelper {

    /**
     * Intenta abrir Google Maps centrado en Arequipa;
     * si no está instalado, abre la versión web.
     */
    fun abrirGoogleMaps(context: Context) {
        try {
            val uri = Uri.parse("geo:0,0?q=Arequipa")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            if (isAppInstalled(context, "com.google.android.apps.maps")) {
                context.startActivity(mapIntent)
            } else {
                // Fallback a la versión web
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://maps.google.com")
                )
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al abrir Maps: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Abre Google Maps con una ubicación específica
     */
    fun abrirGoogleMapsConUbicacion(context: Context, latitud: Double, longitud: Double, nombre: String = "Ubicación") {
        try {
            val uri = Uri.parse("geo:$latitud,$longitud?q=$latitud,$longitud($nombre)")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            if (isAppInstalled(context, "com.google.android.apps.maps")) {
                context.startActivity(mapIntent)
            } else {
                // Fallback a la versión web
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?q=$latitud,$longitud")
                )
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al abrir Maps: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Comprueba si una app con el package name dado está instalada
     */
    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
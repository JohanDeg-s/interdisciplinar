package com.example.testeo

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient

object UbicacionHelper {

    fun solicitarPermisosUbicacion(
        activity: Activity,
        permisosLauncher: ActivityResultLauncher<Array<String>>
    ) {
        val fineGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted || !coarseGranted) {
            permisosLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    fun obtenerUltimaUbicacion(
        activity: Activity,
        fusedClient: FusedLocationProviderClient,
        onSuccess: (Location) -> Unit,
        onFailure: (Exception) -> Unit,
        onNoLocation: () -> Unit
    ) {
        val fineGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            Toast.makeText(activity, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show()
            return
        }

        fusedClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onSuccess(location)
                } else {
                    onNoLocation()
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
                Log.e("UbicacionHelper", "Error obteniendo ubicación", e)
            }
    }
}

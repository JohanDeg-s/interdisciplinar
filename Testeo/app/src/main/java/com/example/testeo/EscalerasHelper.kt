package com.example.testeo

import android.content.Context
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.database.*

object EscalerasHelper {

    private const val DISTANCIA_UMBRAL_METROS = 5.0
    private const val INTERVALO_VERIFICACION_MS = 3000L // Verificar cada 3 segundos
    private const val COOLDOWN_ALERTA_MS = 10000L // Cooldown de 10 segundos entre alertas

    private var isVerificacionActiva = false
    private var puntosEscaleras = mutableListOf<Point>()
    private var handler = Handler(Looper.getMainLooper())
    private var verificacionRunnable: Runnable? = null
    private var ultimaAlerta = 0L
    private var mainActivity: androidx.appcompat.app.AppCompatActivity? = null
    private var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient? = null

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun activarVerificacion(activity: androidx.appcompat.app.AppCompatActivity, onStatusChange: (Boolean) -> Unit) {
        if (isVerificacionActiva) {
            Log.d("EscalerasHelper", "Verificación ya está activa")
            return
        }

        mainActivity = activity
        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(activity)

        isVerificacionActiva = true
        onStatusChange(true)
        cargarPuntosEscaleras(activity)
        iniciarVerificacionPeriodica(activity)
        Log.d("EscalerasHelper", "Verificación de escaleras ACTIVADA")
    }

    fun desactivarVerificacion(onStatusChange: (Boolean) -> Unit) {
        if (!isVerificacionActiva) {
            Log.d("EscalerasHelper", "Verificación ya está desactivada")
            return
        }

        isVerificacionActiva = false
        onStatusChange(false)
        verificacionRunnable?.let { handler.removeCallbacks(it) }
        verificacionRunnable = null
        mainActivity = null
        fusedLocationClient = null
        Log.d("EscalerasHelper", "Verificación de escaleras DESACTIVADA")
    }

    fun isActiva(): Boolean = isVerificacionActiva

    private fun cargarPuntosEscaleras(context: Context) {
        database.child("puntos").child("Escaleras")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    puntosEscaleras.clear()

                    for (puntoSnapshot in snapshot.children) {
                        try {
                            val lat = puntoSnapshot.child("lat").getValue(Double::class.java) ?: 0.0
                            val lon = puntoSnapshot.child("lon").getValue(Double::class.java) ?: 0.0
                            val label = puntoSnapshot.child("label").getValue(String::class.java) ?: "Escaleras"

                            puntosEscaleras.add(Point(lat, lon, label))
                        } catch (e: Exception) {
                            Log.e("EscalerasHelper", "Error al parsear punto: ${e.message}")
                        }
                    }

                    Log.d("EscalerasHelper", "Cargados ${puntosEscaleras.size} puntos de escaleras")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("EscalerasHelper", "Error cargando puntos: ${error.message}")
                }
            })
    }

    private fun iniciarVerificacionPeriodica(context: Context) {
        verificacionRunnable = object : Runnable {
            override fun run() {
                if (isVerificacionActiva) {
                    verificarCercaniaEscaleras(context)
                    handler.postDelayed(this, INTERVALO_VERIFICACION_MS)
                }
            }
        }

        verificacionRunnable?.let { handler.post(it) }
    }

    private fun verificarCercaniaEscaleras(context: Context) {
        if (puntosEscaleras.isEmpty()) {
            Log.d("EscalerasHelper", "No hay puntos de escaleras cargados")
            return
        }

        val activity = mainActivity
        val locationClient = fusedLocationClient

        if (activity == null || locationClient == null) {
            Log.e("EscalerasHelper", "Activity o LocationClient no disponible")
            return
        }

        // Obtener ubicación actual
        UbicacionHelper.obtenerUltimaUbicacion(
            activity,
            locationClient,
            { ubicacionActual ->
                verificarDistanciaAPuntos(context, ubicacionActual)
            },
            { error ->
                Log.e("EscalerasHelper", "Error obteniendo ubicación: ${error.message}")
            },
            {
                Log.w("EscalerasHelper", "Ubicación no disponible")
            }
        )
    }

    private fun verificarDistanciaAPuntos(context: Context, ubicacionActual: Location) {
        val ubicacionPunto = Location("punto")

        for (punto in puntosEscaleras) {
            ubicacionPunto.latitude = punto.lat
            ubicacionPunto.longitude = punto.lon

            val distancia = ubicacionActual.distanceTo(ubicacionPunto)

            Log.d("EscalerasHelper", "Distancia a escalera: ${distancia.toInt()}m")

            if (distancia <= DISTANCIA_UMBRAL_METROS) {
                val tiempoActual = System.currentTimeMillis()

                // Verificar cooldown para evitar alertas repetitivas
                if (tiempoActual - ultimaAlerta > COOLDOWN_ALERTA_MS) {
                    Log.d("EscalerasHelper", "¡ALERTA! Escaleras cerca: ${distancia.toInt()}m")
                    AlertaManager.reproducirAlertaEscaleras(context)
                    ultimaAlerta = tiempoActual
                } else {
                    Log.d("EscalerasHelper", "Escaleras cerca pero en cooldown")
                }

                break // Solo alertar por la escalera más cercana
            }
        }
    }

    fun getPuntosEscaleras(): List<Point> = puntosEscaleras.toList()

    fun getEstadoVerificacion(): String {
        return if (isVerificacionActiva) {
            "Verificando escaleras (${puntosEscaleras.size} puntos)"
        } else {
            "Verificación desactivada"
        }
    }
}
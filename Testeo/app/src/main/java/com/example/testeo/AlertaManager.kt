package com.example.testeo

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast

object AlertaManager {

    private const val UMBRAL_HUMEDAD = 80.0
    private const val UMBRAL_CAMBIO_DISTANCIA = 5.0

    private const val HUMIDITY_ALERT_COOLDOWN_MS = 5000L
    private const val DISTANCE_ALERT_COOLDOWN_MS = 5000L
    private const val ESCALERAS_ALERT_COOLDOWN_MS = 10000L

    private var canPlayHumidityAlert = true
    private var canPlayDistanceAlert = true
    private var canPlayEscalerasAlert = true

    private var previousDistance: Double? = null

    private val handler = Handler(Looper.getMainLooper())

    fun reproducirAlertaSonido(context: Context) {
        try {
            val mp = MediaPlayer.create(context, R.raw.alerta_sonido)
            mp?.let {
                it.setOnCompletionListener { player -> player.release() }
                it.start()
            }
        } catch (e: Exception) {
            Log.e("AlertaManager", "Error alerta sonido", e)
            Toast.makeText(context, "Error alerta sonido", Toast.LENGTH_SHORT).show()
        }
    }

    fun reproducirAlertaDistancia(context: Context) {
        try {
            val mp = MediaPlayer.create(context, R.raw.alarma_distancia)
            mp?.let {
                it.setOnCompletionListener { player -> player.release() }
                it.start()
            }
        } catch (e: Exception) {
            Log.e("AlertaManager", "Error alerta distancia", e)
            Toast.makeText(context, "Error alerta distancia", Toast.LENGTH_SHORT).show()
        }
    }

    fun reproducirAlertaHumedad(context: Context) {
        try {
            val mp = MediaPlayer.create(context, R.raw.alarma_humedad)
            mp?.let {
                it.setOnCompletionListener { player -> player.release() }
                it.start()
            }
        } catch (e: Exception) {
            Log.e("AlertaManager", "Error alerta humedad", e)
            Toast.makeText(context, "Error alerta humedad", Toast.LENGTH_SHORT).show()
        }
    }

    // 🆕 Nueva función para alertas de escaleras
    fun reproducirAlertaEscaleras(context: Context) {
        if (!canPlayEscalerasAlert) {
            Log.d("AlertaManager", "Alerta escaleras en cooldown")
            return
        }

        try {
            val mp = MediaPlayer.create(context, R.raw.alerta_escalera)
            mp?.let {
                it.setOnCompletionListener { player -> player.release() }
                it.start()
                Log.d("AlertaManager", "Reproduciendo alerta de escaleras")

                // Activar cooldown
                canPlayEscalerasAlert = false
                handler.postDelayed({
                    canPlayEscalerasAlert = true
                    Log.d("AlertaManager", "Cooldown escaleras finalizado")
                }, ESCALERAS_ALERT_COOLDOWN_MS)
            }
        } catch (e: Exception) {
            Log.e("AlertaManager", "Error alerta escaleras", e)
            Toast.makeText(context, "Error alerta escaleras: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun verificarCambioDistancia(context: Context, currentDistance: Double) {
        val prev = previousDistance
        if (prev == null) {
            previousDistance = currentDistance
            return
        }

        val diff = kotlin.math.abs(currentDistance - prev)
        if (diff > UMBRAL_CAMBIO_DISTANCIA && canPlayDistanceAlert) {
            reproducirAlertaDistancia(context)
            canPlayDistanceAlert = false
            handler.postDelayed({ canPlayDistanceAlert = true }, DISTANCE_ALERT_COOLDOWN_MS)
            Log.d("AlertaManager", "Alerta distancia: cambio de $diff cm")
        }

        previousDistance = currentDistance
    }

    fun verificarHumedad(context: Context, humedad: Double) {
        if (humedad > UMBRAL_HUMEDAD && canPlayHumidityAlert) {
            reproducirAlertaHumedad(context)
            canPlayHumidityAlert = false
            handler.postDelayed({ canPlayHumidityAlert = true }, HUMIDITY_ALERT_COOLDOWN_MS)
            Log.d("AlertaManager", "Alerta humedad: $humedad%")
        }
    }

    // 🆕 Función para verificar si la alerta de escaleras está disponible
    fun canPlayEscalerasAlert(): Boolean = canPlayEscalerasAlert
}
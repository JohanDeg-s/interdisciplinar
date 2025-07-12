package com.example.testeo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.util.*

object VoiceCommandHelper {

    private lateinit var requestAudioPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var voiceRecognitionLauncher: ActivityResultLauncher<Intent>
    private lateinit var mainActivity: MainActivity

    fun initVoiceCommands(activity: MainActivity) {
        mainActivity = activity


        // Configurar launcher para permiso de audio
        requestAudioPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                iniciarReconocimientoVoz()
            } else {
                Toast.makeText(activity, "Permiso de micrófono denegado", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar launcher para reconocimiento de voz
        voiceRecognitionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!matches.isNullOrEmpty()) {
                    val textoReconocido = matches[0].lowercase(Locale.getDefault())
                    procesarComandoVoz(textoReconocido)
                }
            }
        }

        // Configurar botón de voz
        val btnVoice = activity.findViewById<Button>(R.id.btnVoice)
        btnVoice?.setOnClickListener {
            verificarPermisoYIniciarVoz()
        }
    }

    private fun verificarPermisoYIniciarVoz() {
        when {
            ContextCompat.checkSelfPermission(
                mainActivity,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                iniciarReconocimientoVoz()
            }
            else -> {
                requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun iniciarReconocimientoVoz() {
        if (!SpeechRecognizer.isRecognitionAvailable(mainActivity)) {
            Toast.makeText(mainActivity, "Reconocimiento de voz no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga un comando de voz...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            voiceRecognitionLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(mainActivity, "Error al iniciar reconocimiento de voz", Toast.LENGTH_SHORT).show()
        }
    }

    private fun procesarComandoVoz(textoReconocido: String) {
        Toast.makeText(mainActivity, "Comando: $textoReconocido", Toast.LENGTH_SHORT).show()

        when {
            // Comandos de alertas existentes
            textoReconocido.contains("probar alerta") ||
                    textoReconocido.contains("probar alertas") -> {
                AlertaManager.reproducirAlertaSonido(mainActivity)
            }

            textoReconocido.contains("alerta distancia") -> {
                AlertaManager.reproducirAlertaDistancia(mainActivity)
            }

            textoReconocido.contains("alerta humedad") -> {
                AlertaManager.reproducirAlertaHumedad(mainActivity)
            }


            else -> {
                Toast.makeText(mainActivity, "Comando no reconocido", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


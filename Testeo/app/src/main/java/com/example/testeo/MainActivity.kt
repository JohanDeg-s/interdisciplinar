package com.example.testeo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    private lateinit var txtStatus: TextView
    private lateinit var txtDistancia: TextView
    private lateinit var txtHumedad: TextView
    private lateinit var txtNivelSonido: TextView
    private lateinit var btnDemo: Button
    private lateinit var btnVoice: Button
    private lateinit var btnGuardarPunto: Button
    private lateinit var mapsButton: Button
    private lateinit var btnConectarBluetooth: Button
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var mainContent: ScrollView
    private lateinit var toolbar: Toolbar

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            obtenerUbicacion()
        } else {
            txtStatus.text = "Permiso de ubicación denegado."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inicializarVistas()
        setupToolbar()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        establecerListeners()
        setupFragmentResultListener()

        // Inicializar comandos de voz
        VoiceCommandHelper.initVoiceCommands(this)

        txtStatus.text = "Aplicación iniciada. Conecte dispositivo Bluetooth para recibir datos de sensores."
        UbicacionHelper.solicitarPermisosUbicacion(this, requestLocationPermissionLauncher)
    }

    private fun inicializarVistas() {
        txtStatus = findViewById(R.id.txtStatus)
        txtDistancia = findViewById(R.id.txtDistancia)
        txtHumedad = findViewById(R.id.txtHumedad)
        txtNivelSonido = findViewById(R.id.txtNivelSonido)
        btnDemo = findViewById(R.id.btnDemo)
        btnVoice = findViewById(R.id.btnVoice)
        btnGuardarPunto = findViewById(R.id.btnGuardarPunto)
        mapsButton = findViewById(R.id.mapsButton)
        btnConectarBluetooth = findViewById(R.id.btnConectarBluetooth)
        fragmentContainer = findViewById(R.id.fragment_container)
        mainContent = findViewById(R.id.main_content)
        toolbar = findViewById(R.id.toolbar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportFragmentManager.addOnBackStackChangedListener(this)
        toolbar.visibility = View.GONE
    }

    private fun establecerListeners() {
        btnDemo.setOnClickListener { DemoDialog.mostrar(this) }
        btnGuardarPunto.setOnClickListener { guardarPuntoActual() }
        mapsButton.setOnClickListener { MapsHelper.abrirGoogleMaps(this) }  // 👈 Usando el helper
        btnConectarBluetooth.setOnClickListener { mostrarDevicesFragment() }
        // btnVoice listener se configura automáticamente en VoiceCommandHelper
    }

    override fun onBackStackChanged() {
        val hayFragmentos = supportFragmentManager.backStackEntryCount > 0
        supportActionBar?.setDisplayHomeAsUpEnabled(hayFragmentos)
        toolbar.visibility = if (hayFragmentos) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            fragmentContainer.visibility = View.GONE
            mainContent.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }

    private fun obtenerUbicacion() {
        UbicacionHelper.obtenerUltimaUbicacion(
            this,
            fusedLocationClient,
            { location ->
                lastLocation = location
                txtStatus.text = "Ubicación: Lat ${location.latitude}, Lon ${location.longitude}"
            },
            { error ->
                txtStatus.text = "Error obteniendo ubicación: ${error.message}"
            },
            {
                txtStatus.text = "Ubicación no disponible."
            }
        )
    }

    private fun guardarPuntoActual() {
        UbicacionHelper.obtenerUltimaUbicacion(
            this,
            fusedLocationClient,
            { location ->
                FirebaseHelper.guardarPuntoConDialogo(this, location) { exitoso ->
                    txtStatus.text = if (exitoso) "Punto guardado exitosamente." else "Error guardando punto."
                }
            },
            { error ->
                txtStatus.text = "Error de ubicación: ${error.message}"
            },
            {
                txtStatus.text = "No se pudo obtener ubicación."
            }
        )
    }

    private fun mostrarDevicesFragment() {
        mainContent.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DevicesFragment(), "devices")
            .addToBackStack(null)
            .commit()
    }

    private fun setupFragmentResultListener() {
        supportFragmentManager.setFragmentResultListener(
            DevicesFragment.REQUEST_KEY_DEVICE_SELECTED,
            this
        ) { _, bundle ->
            val address = bundle.getString(DevicesFragment.BUNDLE_KEY_DEVICE_ADDRESS)
            val name = bundle.getString(DevicesFragment.BUNDLE_KEY_DEVICE_NAME)
            fragmentContainer.visibility = View.GONE
            mainContent.visibility = View.VISIBLE
            if (address != null) {
                txtStatus.text = "Dispositivo seleccionado: $name ($address)"
                // 👉 Aquí sigue tu lógica Bluetooth que no he tocado 💙
            } else {
                txtStatus.text = "No se seleccionó ningún dispositivo Bluetooth."
            }
        }
    }
}
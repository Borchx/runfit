package com.borja.android.runfit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.borja.android.runfit.databinding.FragmentRutasBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RutasFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private var _binding: FragmentRutasBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private lateinit var btnCalculate: Button

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val locationList: MutableList<LatLng> = mutableListOf()
    private var isCalculatingRoute = false
    private var startTime: Long = 0

    private lateinit var tvDistance: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvTime: TextView

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }

    private lateinit var locationUpdateServiceIntent: Intent


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRutasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        createLocationCallback() // Inicializar locationCallback
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRutasBinding.bind(view)
        btnCalculate = binding.btnCalculateRoute
        tvDistance = binding.tvDistance
        tvSpeed = binding.tvSpeed
        tvTime = binding.tvTime

        // Inicializar fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


        btnCalculate.setOnClickListener {
            if (isCalculatingRoute) {
                stopCalculatingRoute()
            } else {
                startCalculatingRoute()
            }
        }

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment

        // Solicitar la ubicación actual al iniciar la app
        requestLocation()

        // Configurar el mapa
        mapFragment.getMapAsync(this)

        // Inicializar el Intent para el servicio en segundo plano
        locationUpdateServiceIntent = Intent(requireActivity(), LocationUpdateService::class.java)
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        map.setOnMapClickListener(this)
        enableMyLocation()
        map.uiSettings.isMyLocationButtonEnabled = true
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (isCalculatingRoute) {
                    val lastLocation = locationResult.lastLocation
                    val currentLatLng = LatLng(lastLocation!!.latitude, lastLocation!!.longitude)
                    locationList.add(currentLatLng)
                    drawRoute()
                }
            }
        }
    }

    private fun startCalculatingRoute() {
        isCalculatingRoute = true
        startTime = System.currentTimeMillis()
        tvDistance.text = "Distancia: "
        tvSpeed.text = "Velocidad: "
        tvTime.text = "Tiempo: "
        locationList.clear()
        map.clear() //borrar ruta
        btnCalculate.text = "Parar"
        requestLocationUpdates()

        // Iniciar el servicio en segundo plano
        ContextCompat.startForegroundService(requireActivity(), locationUpdateServiceIntent)
    }

    private fun stopCalculatingRoute() {
        isCalculatingRoute = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        btnCalculate.text = "Empezar"
        if (locationList.isNotEmpty()) {
            val lastLocation = locationList.last()
            map.addMarker(MarkerOptions().position(lastLocation).title("Fin de ruta"))
        }
        val distance = calculateDistance()
        val timeInSeconds = (System.currentTimeMillis() - startTime) / 1000L // Convertir a Long
        val formattedTime = formatTime(timeInSeconds)
        val speed = distance / timeInSeconds.toDouble() * 3.6 // Convertir a Double para la división
        tvDistance.text = String.format("%.2f Metros", distance)
        tvSpeed.text = String.format("%.2f Km/h", speed)
        tvTime.text = "$formattedTime"

        // Detener el servicio en segundo plano
        requireActivity().stopService(locationUpdateServiceIntent)
    }

    private fun formatTime(timeInSeconds: Long): String {
        val hours = timeInSeconds / 3600
        val minutes = (timeInSeconds % 3600) / 60
        val seconds = timeInSeconds % 60

        return String.format("%02dh : %02dm : %02ds", hours, minutes, seconds)
    }

    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 5000 // Intervalo de actualización de ubicación en milisegundos
        locationRequest.fastestInterval =
            2000 // Intervalo más rápido de actualización de ubicación en milisegundos
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            // Si no se han otorgado los permisos, solicitarlos al usuario.
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            // Si no se han otorgado los permisos, solicitarlos al usuario.
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permiso de ubicación denegado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun drawRoute() {
        val polylineOptions = PolylineOptions()
        polylineOptions.addAll(locationList)
        requireActivity().runOnUiThread {
            map.addPolyline(polylineOptions)
        }
    }

    private fun calculateDistance(): Float {
        var distance = 0f
        for (i in 0 until locationList.size - 1) {
            val location1 = locationList[i]
            val location2 = locationList[i + 1]

            distance += getDistanceBetweenPoints(location1, location2)
        }
        return distance
    }

    private fun getDistanceBetweenPoints(point1: LatLng, point2: LatLng): Float {
        val location1 = Location("point1")
        location1.latitude = point1.latitude
        location1.longitude = point1.longitude

        val location2 = Location("point2")
        location2.latitude = point2.latitude
        location2.longitude = point2.longitude

        return location1.distanceTo(location2)
    }

    override fun onMapClick(latlng: LatLng) {
        if (isCalculatingRoute) {
            Toast.makeText(
                requireContext(),
                "Detén el cálculo de la ruta antes de mover el mapa",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { // Verificar si location no es nulo
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    if (::map.isInitialized) { // Verificar si map está inicializado
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                        locationList.add(currentLatLng)
                        map.addMarker(
                            MarkerOptions().position(currentLatLng).title("Inicio de ruta")
                        )
                    } else {
                        // Map no está inicializado, puedes manejar esto de acuerdo a tus necesidades.
                    }
                } ?: run {
                    Toast.makeText(
                        requireContext(),
                        "No se pudo obtener la ubicación actual",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            // Si no se han otorgado los permisos, solicitarlos al usuario.
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
}

private fun getRetrofit(): Retrofit {
    return Retrofit.Builder()
        .baseUrl("https://api.openrouteservice.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

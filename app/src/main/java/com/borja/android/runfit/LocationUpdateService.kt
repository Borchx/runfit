package com.borja.android.runfit

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class LocationUpdateService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private companion object {
        private const val NOTIFICATION_CHANNEL_ID = "com.borja.android.runfit.notification_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "RunFit Location Update"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationCallback()
        startLocationUpdates()

        // Mostrar la notificación de servicio en primer plano
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("RunFit")
            .setContentText("Registrando ubicación en segundo plano...")
            .setSmallIcon(R.drawable.ic_run_fit_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = "Notificaciones de RunFit"
        channel.enableLights(true)
        channel.lightColor = Color.GREEN

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                // Aquí obtienes la ubicación del usuario en tiempo real.
                // Puedes guardar esta ubicación en una lista, enviarla a un servidor, etc.
                val location = locationResult.lastLocation
                if (location != null) {
                    // Aquí puedes realizar acciones con la ubicación recibida.
                    // Por ejemplo, enviar la ubicación a un servidor, guardarla en una base de datos, etc.
                    Log.d("LocationUpdateService", "Latitud: ${location.latitude}, Longitud: ${location.longitude}")
                }
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 5000 // Intervalo de actualización de ubicación en milisegundos
        locationRequest.fastestInterval = 2000 // Intervalo más rápido de actualización de ubicación en milisegundos
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            // Manejar el caso en el que no se hayan otorgado los permisos.
            // Puedes detener el servicio o notificar al usuario, según lo que necesites.
            Log.e("LocationUpdateService", "No se han otorgado los permisos de ubicación.")
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Aquí es donde se ejecutará el trabajo en segundo plano.
        // Puedes registrar la ubicación del usuario aquí y realizar otras tareas necesarias.
        return START_STICKY // Esto indica que el servicio debe seguir ejecutándose después de que el sistema lo detenga.
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpia cualquier recurso o tarea cuando se detenga el servicio.
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
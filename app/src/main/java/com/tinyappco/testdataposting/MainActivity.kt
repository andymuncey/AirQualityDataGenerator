package com.tinyappco.testdataposting

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class MainActivity : AppCompatActivity(), NetworkHandler.NetworkErrorHandler {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestLocationAccess()
        networkHandler = NetworkHandler(deviceGUID())
    }

    private fun deviceGUID() : String {
        val sharedPref = this.getPreferences(
            Context.MODE_PRIVATE)
        val existingGuid = sharedPref.getString("Guid", null)
        if (existingGuid == null) {
            val guid = UUID.randomUUID().toString()
            with (sharedPref.edit()){
                putString("Guid",guid)
                apply()
            }
            return guid
        }
        else {
            return existingGuid
        }
    }


    private lateinit var networkHandler : NetworkHandler
    private var hasLocationPermission = false
    var loc: Location? = null
    private var locMan: LocationManager? = null

    private var running = false

    var locationFound = false

    private fun requestLocationAccess() {
        hasLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasLocationPermission) {
            ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 10
            )
        } else {
            hasLocationPermission = true
            registerForLocationUpdates()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 10) {
            if (grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"Location Permission Granted", Toast.LENGTH_SHORT).show()
                registerForLocationUpdates()
            }
        }

    }

    fun buttonClicked(v: View){
        if (running){
            running = false
            stop()
            Toast.makeText(this,"Stopping", Toast.LENGTH_SHORT).show()
        } else {
            if (loc != null) {
                running = true
                start()
                Toast.makeText(this, "Starting", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No location - cannot start", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val interval : Long = 5000 //how long to wait between posts to the API

    val handler = Handler(Looper.getMainLooper())

    private var locationPoster = LocationPoster()

    inner class LocationPoster : Runnable {
        override fun run() {
            createReading()
            handler.postDelayed(this, interval)
        }
    }

    private fun start(){
        locationPoster.run()
    }

    private fun stop(){
        handler.removeCallbacks(locationPoster)
    }

    private fun createReading(){
        val latitude = loc?.latitude ?: 0.0
        val longitude = loc?.longitude ?: 0.0
        val pm25 = findViewById<SeekBar>(R.id.seekBar).progress
        val reading = AirQualityReading(null,0,pm25,latitude,longitude, Date())
        networkHandler.post(reading)
    }

    override fun networkError(message: String) {
        runOnUiThread {
            Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun notifyLocationFound(){
        Toast.makeText(this, "Location found", Toast.LENGTH_SHORT).show()
    }

    private val locListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            loc = location
            if (!locationFound){
                locationFound = true
                runOnUiThread {
                    notifyLocationFound()
                }
            }

        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {/* Required */}
        override fun onProviderEnabled(provider: String) {/* Required */}
        override fun onProviderDisabled(provider: String) {/* Required */}
    }

    private fun registerForLocationUpdates() {
        locMan = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Location to be unpacked later
            val provider = LocationManager.GPS_PROVIDER
            val updateTime: Long = 5000 //5s between updates
            val updateDistance = 0.0F
            locMan?.requestLocationUpdates(provider, updateTime, updateDistance, locListener)
            loc = locMan?.getLastKnownLocation(provider)
        }
    }
}
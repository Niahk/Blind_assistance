package com.example.vision

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.vision.databinding.ActivityLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.*



class Location : AppCompatActivity(), View.OnClickListener, TextToSpeech.OnInitListener{

    var locate = ""
    private var tts: TextToSpeech? = null
    private lateinit var mainBinding: ActivityLocationBinding
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_location)
        setContentView(mainBinding.root)
        tts = TextToSpeech(this, this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mainBinding.btnLocation.setOnClickListener {
            getLocation()
        }
    }
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        mainBinding.apply {
                            tvLatitude.text = "Latitude\n${list[0].latitude}"
                            tvLongitude.text = "Longitude\n${list[0].longitude}"
                            tvCountryName.text = "Country Name\n${list[0].countryName}"
                            tvLocality.text = "Locality\n${list[0].locality}"
                            tvAddress.text = "Address\n${list[0].getAddressLine(0)}"
                            locate = list[0].getAddressLine(0);
                            val bst = "Your Address is " + locate + " "
                            tts?.speak(bst, TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    override fun onInit(p0: Int) {
        Log.d(TAG, "Initializing TTS")
        if (p0 == TextToSpeech.SUCCESS) {
            Log.d(TAG, "SUCCESS")
            tts!!.language = Locale.US
            tts?.speak(
                "Current Location Open Please tap on Screen",
                TextToSpeech.QUEUE_FLUSH, null, null
            )
        }
    }

    override fun onClick(view: View) {
        if (view.id == R.id.tv_address) {
            val bst = "Your Address is " + locate + " "
            tts?.speak(bst, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
}



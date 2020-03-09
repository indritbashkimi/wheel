package com.ibashkimi.wheel.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Point
import android.location.Criteria
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.compat.ui.PlaceAutocomplete
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.maps.android.SphericalUtil
import com.ibashkimi.wheel.core.checkPermission
import com.ibashkimi.wheel.core.requestPermission
import com.ibashkimi.wheel.location.databinding.ActivityPlacePickerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

// TODO improve this ugliness
class PlacePickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityPlacePickerBinding

    private var center: LatLng? = null
    private var address: CharSequence? = null
    private var radius: Int = 300
    private var margin: Int = 0

    private var lastJob: Job? = null

    private lateinit var mapStyle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mapStyle = intent.getStringExtra("map_style") ?: "normal"

        binding = ActivityPlacePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        margin = resources.getDimensionPixelOffset(R.dimen.place_picker_circle_margin)

        if (savedInstanceState == null) {
            val extras = intent.extras
            if (extras != null) {
                if (extras.containsKey("latitude") && extras.containsKey("longitude"))
                    center = LatLng(extras.getDouble("latitude"), extras.getDouble("longitude"))
                if (extras.containsKey("radius"))
                    radius = extras.getInt("radius")
            }
        } else {
            center = savedInstanceState.getParcelable("center")
            radius = savedInstanceState.getInt("radius")
        }

        binding.selectLocationCard.setOnClickListener { onSave() }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putParcelable("center", center)
        outState.putInt("radius", radius)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.place_picker, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onCancel()
                return true
            }
            R.id.action_place_search -> {
                try {
                    val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                        .build(this)
                    startActivityForResult(
                        intent,
                        PLACE_AUTOCOMPLETE_REQUEST_CODE
                    )
                } catch (e: GooglePlayServicesRepairableException) {
                    e.printStackTrace()
                    throw e
                } catch (e: GooglePlayServicesNotAvailableException) {
                    e.printStackTrace()
                    throw e
                }

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        if (lastJob != null)
            lastJob!!.cancel()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setUpStyle(this, mapStyle)
        googleMap.setOnCameraIdleListener {
            onMapIdle(googleMap)
        }

        if (center != null) {
            moveCamera(googleMap, center!!, radius.toDouble())
        }

        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
            whenGranted = {
                googleMap.isMyLocationEnabled = true
                if (center == null) {
                    val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                    val provider = locationManager.getBestProvider(Criteria(), true)
                    val myLocation = locationManager.getLastKnownLocation(provider)
                    center = if (myLocation != null)
                        LatLng(myLocation.latitude, myLocation.longitude)
                    else
                        LatLng(0.0, 0.0)
                    moveCamera(googleMap, center!!, radius.toDouble())
                }
            },
            whenExplanationNeed = {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.location_permission_needed)
                    .setMessage(R.string.location_permission_rationale)
                    .setNegativeButton(android.R.string.cancel) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        requestPermission(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            PERMISSION_REQUEST_LOCATION
                        )
                    }
                    .show()
            },
            whenDenied = {
                requestPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    PERMISSION_REQUEST_LOCATION
                )
            }
        )
    }

    private fun calculateBounds(center: LatLng, radius: Double): LatLngBounds {
        return LatLngBounds.Builder().include(SphericalUtil.computeOffset(center, radius, 0.0))
            .include(SphericalUtil.computeOffset(center, radius, 90.0))
            .include(SphericalUtil.computeOffset(center, radius, 180.0))
            .include(SphericalUtil.computeOffset(center, radius, 270.0)).build();
    }

    private fun moveCamera(googleMap: GoogleMap, center: LatLng, radius: Double) {
        val bounds = calculateBounds(center, radius)
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, margin)
        // Show the current location in Google Map
        Handler().post {
            googleMap.moveCamera(cameraUpdate)
        }
    }

    private fun onMapIdle(googleMap: GoogleMap) {
        val projection = googleMap.projection
        val cover = binding.mapCover
        val startPoint = projection.fromScreenLocation(
            Point(
                (cover.centerX - cover.radius).toInt(),
                cover.centerY.toInt()
            )
        )
        val endPoint =
            projection.fromScreenLocation(Point(cover.centerX.toInt(), cover.centerY.toInt()))

        // The computed distance is stored in results[0].
        //If results has length 2 or greater, the initial bearing is stored in results[1].
        //If results has length 3 or greater, the final bearing is stored in results[2].
        val results = FloatArray(1)
        Location.distanceBetween(
            startPoint.latitude, startPoint.longitude,
            endPoint.latitude, endPoint.longitude, results
        )
        radius = results[0].toInt()

        projection.visibleRegion.latLngBounds.center.let {
            center = it
            address = it.latitude.toString() + " : " + it.longitude.toString()

            binding.address.text = address

            updateAddress(it)
        }
    }

    private fun updateAddress(center: LatLng) {
        if (lastJob != null)
            lastJob!!.cancel()
        lastJob = lifecycleScope.launch(Dispatchers.Default) {
            val geocoder = Geocoder(this@PlacePickerActivity, Locale.getDefault())
            // Here 1 represents max location result to returned, by documents it recommended 1 to 5
            try {
                val addresses = geocoder.getFromLocation(center.latitude, center.longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val lastAddress = addresses[0].getAddressLine(0)
                    launch(Dispatchers.Main) { setAddress(lastAddress) }
                }
            } catch (e: Exception) {
                Log.d(TAG, "exception: ${e.message}")
            }
        }
    }

    private fun setAddress(address: CharSequence) {
        this.address = address
        binding.address.text = address
    }

    private fun onCancel() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun onSave() {
        if (center != null) {
            val resultIntent = Intent()
            resultIntent.putExtra("latitude", center!!.latitude)
            resultIntent.putExtra("longitude", center!!.longitude)
            resultIntent.putExtra("radius", radius)
            resultIntent.putExtra("address", address)
            setResult(Activity.RESULT_OK, resultIntent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_LOCATION -> {
                recreate()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    val place = PlaceAutocomplete.getPlace(this, data)
                    this.center = place.latLng
                    val mapFragment =
                        supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                    mapFragment.getMapAsync(this)
                }
                PlaceAutocomplete.RESULT_ERROR -> {
                    val status = PlaceAutocomplete.getStatus(this, data)
                    Log.i(TAG, status?.statusMessage ?: "null")
                }
                RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }
    }

    companion object {
        private val TAG = PlacePickerActivity::class.java.simpleName
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 1
        private const val PERMISSION_REQUEST_LOCATION = 99
    }
}

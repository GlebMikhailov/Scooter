package com.development.sota.scooter.ui.map.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.development.sota.scooter.R
import com.development.sota.scooter.ui.map.data.Scooter
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.ClusterRenderer
import com.google.maps.android.ktx.addMarker
import moxy.MvpAppCompatActivity
import moxy.MvpView
import moxy.ktx.moxyPresenter
import moxy.viewstate.strategy.alias.AddToEnd
import kotlin.math.max


interface MapView : MvpView {
    @AddToEnd
    fun updateScooterMarkers(scooters: ArrayList<Scooter>)

    @AddToEnd
    fun initLocationRelationships()
}

class MapActivity : MvpAppCompatActivity(), MapView {

    //private var _binding: ActivityMapBinding? = null
    //private val binding get() = _binding!!

    private val presenter by moxyPresenter { MapPresenter() }
    private lateinit var supportMapFragment: SupportMapFragment
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private lateinit var clusterManager: ClusterManager<ScooterClusterItem>
    private lateinit var googleApi: GoogleApiClient
    private val scooterMarkers = hashMapOf<Long, ScooterClusterItem>() // ID: Marker

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //_binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_map)

        getLocationPermission()
        initLocationRelationships()
        

        fusedLocationProviderClient = FusedLocationProviderClient(this)
        supportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        supportMapFragment.getMapAsync { googleMap ->
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            val scooters = presenter.getScooters()

            clusterManager = ClusterManager(this, googleMap)
            clusterManager.renderer = MarkerClusterRenderer(this, googleMap, clusterManager)
            clusterManager.setOnClusterItemClickListener { presenter.markerClicked(it) }

            googleMap.setOnCameraIdleListener(clusterManager)

            for (scooter in scooters) {
                scooterMarkers[scooter.id] = ScooterClusterItem(scooter)

                clusterManager.addItem(scooterMarkers[scooter.id])
            }

            clusterManager.cluster()

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(44.894, 37.316), 8f))
            map = googleMap
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun updateScooterMarkers(scooters: ArrayList<Scooter>) {
        runOnUiThread {
            Log.w("SCOOTERS", scooters.toString())
            for (scooter in scooters) {
                if (scooter.id in scooterMarkers.keys) {
                    scooterMarkers[scooter.id]!!.scooter = scooter
                } else {
                    scooterMarkers[scooter.id] = ScooterClusterItem(scooter)
                    clusterManager.addItem(scooterMarkers[scooter.id])
                }
            }

            clusterManager.cluster()
        }

    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            presenter.updateLocationPermission(true)
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        presenter.updateLocationPermission(false)
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    presenter.updateLocationPermission(true)
                }
            }
        }
    }

    override fun initLocationRelationships() {
        runOnUiThread {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {
                            val latLng = LatLng(it.latitude, it.longitude)

                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                        }

                    map.isMyLocationEnabled = true
                    map.uiSettings.isMyLocationButtonEnabled = true

                    map.setOnMyLocationButtonClickListener {
                        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {
                                try {
                                        val latLng = LatLng(
                                            it.latitude,
                                            it.longitude
                                        )

                                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                                } catch (e: Exception) {}
                            }

                        return@setOnMyLocationButtonClickListener true
                    }
                } catch (e: Exception) {
                    Log.w("Map location error", e.localizedMessage)
                }
            }
        }
    }

    //TODO: Recalculate without mistakes!
    fun bitmapDescriptorFromVector(
        @DrawableRes vectorResId: Int
    ): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(this, vectorResId)
        val backgroundDrawable =
            ContextCompat.getDrawable(this, R.drawable.ic_purple_circle_with_white_corner)

        vectorDrawable!!.setBounds(
            (((max(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight) * 1.8).toInt() - vectorDrawable.intrinsicWidth * 1.5 ) / 1.5).toInt(),
            (((max(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight) * 1.8).toInt() - vectorDrawable.intrinsicHeight * 1.5) / 1.5).toInt(),
            (vectorDrawable.intrinsicWidth * 1.5 + 0.5 * (max(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight) * 1.8 - vectorDrawable.intrinsicWidth * 1.5) / 1.5).toInt(),
            (vectorDrawable.intrinsicHeight * 1.5 + 0.5 * (max(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight) * 1.8 - vectorDrawable.intrinsicHeight * 1.5) / 1.5).toInt()
        )
        backgroundDrawable!!.setBounds(
            0,
            0,
            (max(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight) * 1.8).toInt(),
            (max(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight) * 1.8).toInt()
        )

        val bitmap = Bitmap.createBitmap(
            (max(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight) * 1.8).toInt(),
            (max(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight) * 1.8).toInt(),
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        backgroundDrawable.draw(canvas)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onBackPressed() {}

    companion object {
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 99
    }
}


package com.development.sota.scooter.ui.map.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.akexorcist.googledirection.DirectionCallback
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.constant.TransportMode.WALKING
import com.akexorcist.googledirection.model.Direction
import com.development.sota.scooter.BASE_IMAGE_URL
import com.development.sota.scooter.GOOGLE_API_KEY
import com.development.sota.scooter.R
import com.development.sota.scooter.databinding.ActivityMapBinding
import com.development.sota.scooter.ui.drivings.DrivingsActivity
import com.development.sota.scooter.ui.drivings.DrivingsStartTarget
import com.development.sota.scooter.ui.map.data.Scooter
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_scooter_driving.view.*
import moxy.MvpAppCompatActivity
import moxy.MvpView
import moxy.ViewStateProvider
import moxy.ktx.moxyPresenter
import moxy.viewstate.strategy.alias.AddToEnd

interface MapView : MvpView {
    @AddToEnd
    fun updateScooterMarkers(scooters: ArrayList<Scooter>)

    @AddToEnd
    fun initLocationRelationships()

    @AddToEnd
    fun drawRoute(origin: LatLng, destination: LatLng)

    @AddToEnd
    fun showScooterCard(scooter: Scooter)
}

class MapActivity : MvpAppCompatActivity(), MapView {
    private var _binding: ActivityMapBinding? = null
    private val binding get() = _binding!!

    private val presenter by moxyPresenter { MapPresenter() }
    private lateinit var supportMapFragment: SupportMapFragment
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private lateinit var clusterManager: ClusterManager<ScooterClusterItem>
    private val scooterMarkers = hashMapOf<Long, ScooterClusterItem>() // ID: Marker
    private var myMarker: Marker? = null

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMapBinding.inflate(layoutInflater)

        setContentView(binding.root)

        getLocationPermission()
        initLocationRelationships()

        binding.contentOfMap.mapScooterItem.cardViewScooterItem.visibility = View.GONE
        binding.contentOfMap.imageButtonMapQr.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(this, DrivingsActivity::class.java)
                intent.putExtra("aim", DrivingsStartTarget.QRandCode)

                startActivity(intent)
            } else {
                getCameraPermission()
            }
        }

        binding.contentOfMap.imageButtonMapMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.contentOfMap.imageButtonMapLocation.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                initLocationRelationships()
            } else {
                getLocationPermission()
            }
        }

        fusedLocationProviderClient = FusedLocationProviderClient(this)
        supportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        supportMapFragment.getMapAsync { googleMap ->
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            val scooters = presenter.getScooters()

            clusterManager = ClusterManager(this, googleMap)
            clusterManager.renderer = MarkerClusterRenderer(this, googleMap, clusterManager)
            clusterManager.setOnClusterItemClickListener { presenter.markerClicked(it) }

            googleMap.setOnCameraIdleListener(clusterManager)

            googleMap.setOnMapClickListener { binding.contentOfMap.mapScooterItem.cardViewScooterItem.visibility = View.GONE }

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

    private fun getCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.CAMERA
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            presenter.updateLocationPermission(true)
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA),
                PERMISSIONS_REQUEST_ACCESS_CAMERA
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

            PERMISSIONS_REQUEST_ACCESS_CAMERA -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    val intent = Intent(this, DrivingsActivity::class.java)
                    intent.putExtra("aim", DrivingsStartTarget.QRandCode)

                    startActivity(intent)
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
                        //presenter.position = latLng

                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))

                        if(myMarker != null) {
                            myMarker!!.remove()
                        }

                        myMarker = map.addMarker(MarkerOptions().position(latLng))
                    }

                    map.setOnMyLocationButtonClickListener {
                        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {
                            try {
                                val latLng = LatLng(
                                    it.latitude,
                                    it.longitude
                                )
                                //presenter.position = latLng

                                if(myMarker != null) {
                                    myMarker!!.remove()
                                }

                                myMarker = map.addMarker(MarkerOptions().position(latLng))

                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
                            } catch (e: Exception) {
                            }
                        }

                        return@setOnMyLocationButtonClickListener true
                    }
                } catch (e: Exception) {
                    Log.w("Map location error", e.localizedMessage)
                }
            }
        }
    }

    override fun drawRoute(origin: LatLng, destination: LatLng) {
        GoogleDirection.withServerKey(GOOGLE_API_KEY)
            .from(LatLng(origin.latitude, origin.longitude))
            .to(
                LatLng(
                    destination.latitude,
                    destination.longitude
                )
            )
            .transportMode(WALKING)
            .execute(
                object : DirectionCallback {
                    override fun onDirectionSuccess(direction: Direction?) {
                        Log.w("Route got", direction?.status ?: "")
                        if (direction != null && direction.isOK) {

                            val polyline = PolylineOptions()
                                .addAll(direction.routeList.first()!!.overviewPolyline.pointList.map {
                                    LatLng(
                                        it.latitude,
                                        it.longitude
                                    )
                                })

                            runOnUiThread {
                                map.addPolyline(polyline)
                            }
                        }
                    }

                    override fun onDirectionFailure(t: Throwable) {
                        Log.w("Route exception", t.localizedMessage)
                    }
                }
            )

    }

    override fun showScooterCard(scooter: Scooter) {
        runOnUiThread {
            binding.contentOfMap.mapScooterItem.cardViewScooterItem.visibility = View.VISIBLE

            binding.contentOfMap.mapScooterItem.cardViewScooterItem.linnearLayoutScooterItemFinishButtons.visibility = View.INVISIBLE
            binding.contentOfMap.mapScooterItem.cardViewScooterItem.linnearLayoutScooterItemBookingButtons.visibility = View.INVISIBLE
            binding.contentOfMap.mapScooterItem.cardViewScooterItem.linnearLayoutScooterItemRentButtons.visibility = View.INVISIBLE
            binding.contentOfMap.mapScooterItem.cardViewScooterItem.textViewItemScooterId.text = "#${scooter.id}"
            binding.contentOfMap.mapScooterItem.cardViewScooterItem.textViewItemScooterBatteryPercent.text = scooter.getBatteryPercentage()

            Picasso.get().load(BASE_IMAGE_URL + scooter.photo).into(binding.contentOfMap.mapScooterItem.cardViewScooterItem.imageViewScooterItemIcon)
        }
    }

    override fun onBackPressed() {}

    companion object {
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 99
        const val PERMISSIONS_REQUEST_ACCESS_CAMERA = 100
    }
}


package com.development.sota.scooter.ui.map.presentation

import android.os.Bundle
import com.development.sota.scooter.R
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MapStyleOptions
import com.google.android.libraries.maps.model.MarkerOptions
import moxy.MvpAppCompatActivity
import moxy.MvpView
import moxy.ktx.moxyPresenter
import moxy.viewstate.strategy.alias.AddToEnd


interface MapView: MvpView {
    @AddToEnd
    fun setMap()
}

class MapActivity : MvpAppCompatActivity(), MapView {

    //private var _binding: ActivityMapBinding? = null
    //private val binding get() = _binding!!

    private val presenter by moxyPresenter { MapPresenter() }
    private lateinit var supportMapFragment: SupportMapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //_binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_map)

        supportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    }

    override fun setMap() {
        runOnUiThread {
            supportMapFragment.getMapAsync {
                //TODO: Ask presenter for a scooters
            }
        }
    }
}
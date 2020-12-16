package com.development.sota.scooter.ui.map.presentation

import android.content.Context
import android.util.Log
import com.development.sota.scooter.R
import com.development.sota.scooter.base.BasePresenter
import com.development.sota.scooter.ui.map.data.Scooter
import com.development.sota.scooter.ui.map.domain.MapInteractor
import com.development.sota.scooter.ui.map.domain.MapInteractorImpl
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moxy.MvpPresenter

class MapPresenter(val context: Context): MvpPresenter<MapView>(), BasePresenter {
    private val interactor: MapInteractor = MapInteractorImpl(this)

    private var scooters = arrayListOf<Scooter>()

    var locationPermissionGranted = false
    var position: LatLng = LatLng(44.894997, 37.316259)
    lateinit var scootersGeoJsonSource: List<Feature>

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        interactor.getAllScooters()
    }

    fun scootersGotFromServer(scooters: ArrayList<Scooter>) {
        this.scooters = scooters

        makeFeaturesFromScootersAndSendToMap()
    }

    fun getScooters(): ArrayList<Scooter> {
        return scooters
    }

    fun clickedOnScooterWith(id: Long) {
        val scooter = scooters.first { it.id == id }
        viewState.showScooterCard(scooter)
        interactor.getRouteFor(destination = position, origin = scooter.getLatLng())
    }

    fun errorGotFromServer(error: String) {
        Log.e("Error", error)
        //TODO: Error alert
    }

    fun updateLocationPermission(permission: Boolean) {
        locationPermissionGranted = permission

        if (locationPermissionGranted) viewState.initLocationRelationships()
    }

    fun gotRouteFromServer(route: DirectionsRoute) {
        viewState.drawRoute(route.geometry() ?: "{}")
    }

    private fun makeFeaturesFromScootersAndSendToMap() {
        GlobalScope.launch {
            scootersGeoJsonSource = scooters.groupBy { it.getScooterIcon() }.mapValues { entry ->
                    entry.value.map {
                        Feature.fromJson(
                            """{
                                "type": "Feature",
                                "geometry": {
                                    "type": "Point",
                                    "coordinates": [${it.longitude}, ${it.latitude}]
                                },
                                "properties": {
                                    "id": ${it.id},
                                    "scooter-image": ${
                                        when(it.getScooterIcon()) {
                                            R.drawable.ic_icon_scooter_third -> "scooter-third"
                                            R.drawable.ic_icon_scooter_second -> "scooter-second"
                                            R.drawable.ic_icon_scooter_first -> "scooter-first"
                                            else -> "scooter-third"
                                        }
                                    }
                                }
                            }"""
                        )
                    }
                }.values.flatten()

            viewState.updateScooterMarkers(scootersGeoJsonSource)
        }
    }

    fun makeFeatureFromLatLng(latLng: LatLng): Feature {
        return   Feature.fromJson(
            """{
                                "type": "Feature",
                                "geometry": {
                                    "type": "Point",
                                    "coordinates": [${latLng.longitude}, ${latLng.latitude}]
                                }
                            }"""
        )
    }

    override fun onDestroyCalled() {
        interactor.disposeRequests()
    }
}
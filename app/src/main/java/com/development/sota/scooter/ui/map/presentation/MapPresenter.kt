package com.development.sota.scooter.ui.map.presentation

import android.util.Log
import com.development.sota.scooter.base.BasePresenter
import com.development.sota.scooter.ui.map.data.Scooter
import com.development.sota.scooter.ui.map.domain.MapInteractor
import com.development.sota.scooter.ui.map.domain.MapInteractorImpl
import com.google.android.gms.maps.model.LatLng
import moxy.MvpPresenter

class MapPresenter : MvpPresenter<MapView>(), BasePresenter {
    private val interactor: MapInteractor = MapInteractorImpl(this)

    private var scooters = arrayListOf<Scooter>()

    var locationPermissionGranted = false
    var position: LatLng? = LatLng(44.894997, 37.316259)

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        interactor.getAllScooters()
    }

    fun scootersGotFromServer(scooters: ArrayList<Scooter>) {
        this.scooters = scooters

        viewState.updateScooterMarkers(scooters)
    }

    fun getScooters(): ArrayList<Scooter> {
        return scooters
    }

    fun errorGotFromServer(error: String) {
        Log.e("Error", error)
        //TODO: Error alert
    }

    fun updateLocationPermission(permission: Boolean) {
        locationPermissionGranted = permission

        if (locationPermissionGranted) viewState.initLocationRelationships()
    }

    fun markerClicked(marker: ScooterClusterItem): Boolean {
        viewState.drawRoute(position!!, marker.position)
        viewState.showScooterCard(marker.scooter)
        return true
    }
    
    override fun onDestroyCalled() {
        interactor.disposeRequests()
    }
}
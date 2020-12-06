package com.development.sota.scooter.ui.map.presentation

import com.development.sota.scooter.base.BasePresenter
import com.development.sota.scooter.ui.map.data.Scooter
import moxy.MvpPresenter

class MapPresenter: MvpPresenter<MapView>(), BasePresenter {
    private val scooters = arrayListOf<Scooter>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
    }
    override fun onDestroyCalled() {}
}
package com.development.sota.scooter.ui.map.domain

import com.development.sota.scooter.api.DirectionsRetrofitProvider
import com.development.sota.scooter.api.MapRetrofitProvider
import com.development.sota.scooter.base.BaseInteractor
import com.development.sota.scooter.ui.map.data.Scooter
import com.development.sota.scooter.ui.map.presentation.MapPresenter
import com.google.android.gms.maps.model.LatLng
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers

interface MapInteractor : BaseInteractor {
    fun getAllScooters()
    fun getRouteFor(origin: LatLng, dest: LatLng)
}

class MapInteractorImpl(private val presenter: MapPresenter) : MapInteractor {
    private val compositeDisposable = CompositeDisposable()

    override fun getAllScooters() {
        compositeDisposable.add(
            MapRetrofitProvider.service.getScooters()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = { presenter.scootersGotFromServer(it as ArrayList<Scooter>) },
                    onError = { presenter.errorGotFromServer(it.localizedMessage) })
        )
    }

    override fun getRouteFor(origin: LatLng, dest: LatLng) {
        compositeDisposable.add(
            DirectionsRetrofitProvider.service.getRoute(
                "${origin.latitude},${origin.longitude}",
                "${dest.latitude},${dest.longitude}"
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = { presenter.scootersGotFromServer(it as ArrayList<Scooter>) },
                    onError = { presenter.errorGotFromServer(it.localizedMessage) })
        )
    }

    override fun disposeRequests() {
        compositeDisposable.clear()
    }
}
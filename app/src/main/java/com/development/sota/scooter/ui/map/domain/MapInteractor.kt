package com.development.sota.scooter.ui.map.domain

import com.development.sota.scooter.R
import com.development.sota.scooter.api.DirectionsRetrofitProvider
import com.development.sota.scooter.api.MapRetrofitProvider
import com.development.sota.scooter.base.BaseInteractor
import com.development.sota.scooter.ui.map.data.Scooter
import com.development.sota.scooter.ui.map.presentation.MapPresenter
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


interface MapInteractor : BaseInteractor {
    fun getAllScooters()
    fun getRouteFor(destination: LatLng, origin: LatLng)
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

    override fun getRouteFor(destination: LatLng, origin: LatLng) {
        val client = MapboxDirections.builder()
            .accessToken(presenter.context.getString(R.string.mabox_access_token))
            .destination(Point.fromLngLat(destination.longitude, destination.latitude))
            .origin(Point.fromLngLat(origin.longitude, origin.latitude))
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .profile(DirectionsCriteria.PROFILE_WALKING)
            .build()

        client.enqueueCall(object: Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
                val routes = response.body()?.routes()

                if(routes != null && routes.isNotEmpty()) {
                    presenter.gotRouteFromServer(routes[0])
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                presenter.errorGotFromServer(t.localizedMessage)
            }

        })
    }

    override fun disposeRequests() {
        compositeDisposable.clear()
    }
}
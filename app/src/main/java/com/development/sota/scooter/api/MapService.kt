package com.development.sota.scooter.api

import com.development.sota.scooter.ui.map.data.Scooter
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET

interface MapService {
    @GET("getScooter/")
    fun getScooters(): Observable<List<Scooter>>
}
package com.development.sota.scooter.api

import com.development.sota.scooter.ui.map.data.Scooter
import com.development.sota.scooter.ui.map.data.ScooterResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET

interface MapService {
    @GET
    fun getScooters(): Observable<ScooterResponse>
}
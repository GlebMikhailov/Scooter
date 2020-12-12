package com.development.sota.scooter.api

import com.development.sota.scooter.GOOGLE_API_KEY
import com.development.sota.scooter.ui.map.domain.entities.Directions
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsService {
    @GET("directions/json")
    fun getRoute(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String = "walking",
        @Query("key") key: String = GOOGLE_API_KEY
    ): Observable<Directions>
}
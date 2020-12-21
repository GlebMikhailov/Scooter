package com.development.sota.scooter.api

import com.development.sota.scooter.ui.drivings.domain.entities.AddOrderResponse
import com.development.sota.scooter.ui.drivings.domain.entities.Order
import com.development.sota.scooter.ui.map.data.Rate
import com.development.sota.scooter.ui.map.data.Scooter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MapService {
    @GET("getScooter/")
    fun getScooters(): Observable<List<Scooter>>

    @GET("getRate/")
    fun getRate(): Observable<List<Rate>>

}
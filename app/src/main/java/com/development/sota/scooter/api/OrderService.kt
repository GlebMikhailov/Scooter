package com.development.sota.scooter.api

import com.development.sota.scooter.ui.drivings.domain.entities.Order
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface OrderService {
    @GET("getOrders/")
    fun getOrdersByClientID(@Query("client") id: Long): Observable<List<Order>>

    @POST("addOrder/")
    fun addOrdersByClientID(
        @Field("date") date: String,
        @Field("start_time") startTime: String,
        @Field("finish_time") finishTime: String,
        @Field("scooter") scooterId: Long,
        @Field("client") clientId: Long,
        @Field("rate") rateId: Long
    ): Completable
}
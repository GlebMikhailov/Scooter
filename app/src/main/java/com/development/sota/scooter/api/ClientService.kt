package com.development.sota.scooter.api

import com.development.sota.scooter.api.client.UserClass
import com.development.sota.scooter.ui.map.data.BookingBlockResponse
import com.development.sota.scooter.ui.map.data.Client
import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ClientService {
    @GET("getClient")
    fun getClient(@Query("id") id: String): Observable<List<Client>>

    @GET("isClientBooksBlocked")
    fun checkBookingBlock(@Query("id") id: String): Observable<BookingBlockResponse>

    @GET("getClient/{id}/name")
    fun getClientName(@Query("id") id: String): Call<List<UserClass>>

}
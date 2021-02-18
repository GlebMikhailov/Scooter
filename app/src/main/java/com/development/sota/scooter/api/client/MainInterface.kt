package com.development.sota.scooter.api.client

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MainInterface {
    @GET("getClient/")
    fun getUser(@Query("format", encoded = true) format: String?, @Query("id", encoded = true) userId: String?): Call<List<UserClass>>
}
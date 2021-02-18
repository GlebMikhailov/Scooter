package com.development.sota.scooter.api

import com.development.sota.scooter.BASE_URL
import com.development.sota.scooter.api.client.UserClass
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface APIService {



    @GET("getClient/")
    fun getUser(@Query("format", encoded = true) format: String?, @Query("id", encoded = true) userId: String?): Call<List<UserClass>>?
}
package com.development.sota.scooter.api.client

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.*


interface RegisterAPI {
    @FormUrlEncoded
    @POST("getClient/")
    fun saveName(
        @Body body: RequestBody
    ): Callback<ResponseBody>

}
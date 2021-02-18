package com.development.sota.scooter.api.client

import android.content.Context
import android.util.Log
import com.development.sota.scooter.BASE_URL
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitClient {
    fun service(context: Context?): MainInterface {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(MainInterface::class.java)
        Log.d("us_user_information","result")
    }

    fun getContentData(method: Call<List<UserClass>>?, webResponse: WebResponse) {
        method!!.enqueue(object : Callback<List<UserClass>> {
            override fun onResponse(call: Call<List<UserClass>>, response: Response<List<UserClass>>) {
                response.body()?.forEach { change ->
                    webResponse.onResponseSuccess(response)
                    Log.d("us_user_data", "data = ${change?.balance}, ${change?.phone}, $call")
                    Log.d("us_user_response.body()", "response.body() = ${response.body()}")
                }
            }

            override fun onFailure(call: Call<List<UserClass>>, throwable: Throwable) {
                webResponse.onResponseFailed(throwable.message)
                Log.d("us_user_error", "error = $throwable, $call")
            }
        })
    }
}

interface WebResponse {
    fun onResponseSuccess(result: Response<List<UserClass>>)
    fun onResponseFailed(error: String?)
}
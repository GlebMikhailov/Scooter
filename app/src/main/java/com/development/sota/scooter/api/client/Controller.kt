package com.development.sota.scooter.api.client

import android.content.Context
import android.util.Log
import com.development.sota.scooter.BASE_URL
import com.development.sota.scooter.api.APIService
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


open class Controller(var context: Context)  {
    fun start() {

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(APIService::class.java)
        val call = apiService.getUser(
            "json",
            context.getSharedPreferences("account", Context.MODE_PRIVATE)
                .getLong("id", 0).toString()
        ) as Call<List<UserClass?>>

        Log.d("us_user_call", "call = $call")
        Log.d("us_user_retrofit", "retrofit = $retrofit")
        call!!.enqueue(object : Callback<List<UserClass?>> {

            override fun onResponse(
                call: Call<List<UserClass?>>,
                response: Response<List<UserClass?>>
            ) {
                if (!response.isSuccessful) {
                    Log.d("us_user_error", "error = error")

                }
                val gson = Gson()
                Log.d(
                    "us_user_response", "response = ${
                        response.body()?.forEach { change ->

                        }
                    }"
                )
                val reviews = response.body()
                Log.d("us_user_reviews", "reviews = $reviews, $call, $response")
                val enums: Array<UserClass> = gson.fromJson(
                    gson.toJson(response.body()),
                    Array<UserClass>::class.java
                )

                val userClass: List<UserClass?>? = response.body()
                response.body()?.forEach { change ->
                    Log.d("us_user_data", "data = ${change?.balance}, ${change?.phone}")

                }
                // val userClass: List<UserClass?>? = response.body()

                //  val collectionType: Type = object : TypeToken<Collection<UserClass>?>() {}.type
                //   val enums: Collection<ChannelSearchEnum> = gson.fromJson(yourJson, collectionType)


                val name = reviews!!
            }

            override fun onFailure(call: Call<List<UserClass?>>, t: Throwable) {
                t.printStackTrace()
                Log.d("us_user_error", "error = ${t.message}")

            }
        }
        )


    }
    fun getName(names: (name: String) -> Unit) {

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(APIService::class.java)
        val call = apiService.getUser(
            "json",
            context.getSharedPreferences("account", Context.MODE_PRIVATE)
                .getLong("id", 0).toString()
        ) as Call<List<UserClass?>>

        Log.d("us_user_call", "call = $call")
        Log.d("us_user_retrofit", "retrofit = $retrofit")
        call!!.enqueue(object : Callback<List<UserClass?>> {

            override fun onResponse(
                call: Call<List<UserClass?>>,
                response: Response<List<UserClass?>>
            ) {
                if (!response.isSuccessful) {
                    Log.d("us_user_error", "error = error")

                }
                val gson = Gson()
                Log.d(
                    "us_user_response", "response = ${
                        response.body()?.forEach { change ->

                        }
                    }"
                )
                val reviews = response.body()
                Log.d("us_user_reviews", "reviews = $reviews, $call, $response")
                val enums: Array<UserClass> = gson.fromJson(
                    gson.toJson(response.body()),
                    Array<UserClass>::class.java
                )

                val userClass: List<UserClass?>? = response.body()
                response.body()?.forEach { change ->
                    Log.d("us_user_data", "data = ${change?.balance}, ${change?.phone}")
               change?.name.toString()
                }
                // val userClass: List<UserClass?>? = response.body()

                //  val collectionType: Type = object : TypeToken<Collection<UserClass>?>() {}.type
                //   val enums: Collection<ChannelSearchEnum> = gson.fromJson(yourJson, collectionType)


                val name = reviews!!
            }

            override fun onFailure(call: Call<List<UserClass?>>, t: Throwable) {
                t.printStackTrace()
                Log.d("us_user_error", "error = ${t.message}")

            }
        }
        )
    }


}
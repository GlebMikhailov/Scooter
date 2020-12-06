package com.development.sota.scooter.api

import com.development.sota.scooter.BASE_URL
import com.development.sota.scooter.util.LoggingInterceptor
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val interceptor = LoggingInterceptor()

val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(interceptor)
    .build()

val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addCallAdapterFactory(RxJava3CallAdapterFactory.createAsync())
    .addConverterFactory(GsonConverterFactory.create())
    .client(client)
    .build()

object LoginRetrofitProvider {
    val service: LoginService = retrofit.create(LoginService::class.java)
}

object MapRetrofitProvider {
    val service: MapService = retrofit.create(MapService::class.java)
}
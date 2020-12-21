package com.development.sota.scooter.api

import com.development.sota.scooter.BASE_URL
import com.development.sota.scooter.util.LoggingInterceptor
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit


@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class Wrapped


val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

val interceptor = LoggingInterceptor()

class HeadersInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val newReq = chain.request().newBuilder()
            .addHeader("User-Agent", "Sota Dev Scooter Android Web Connector")
            .build()
        return chain.proceed(newReq)
    }
}

val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(HeadersInterceptor())
    .addInterceptor(interceptor)
    .build()

val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addCallAdapterFactory(RxJava3CallAdapterFactory.createAsync())
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(client)
    .build()

val directionsRetrofit: Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addCallAdapterFactory(RxJava3CallAdapterFactory.createAsync())
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(client)
    .build()

object LoginRetrofitProvider {
    val service: LoginService = retrofit.create(LoginService::class.java)
}

object MapRetrofitProvider {
    val service: MapService = retrofit.create(MapService::class.java)
}

object OrdersRetrofitProvider {
    val service: OrderService = retrofit.create(OrderService::class.java)
}

object ClientRetrofitProvider {
    val service: ClientService = retrofit.create(ClientService::class.java)
}

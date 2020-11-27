package com.development.sota.scooter.api

import com.development.sota.scooter.ui.login.domain.entities.ClientLoginResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*

interface LoginService {
    @POST("ClientLogin/")
    @FormUrlEncoded
    @Headers(
        "User-Agent: Sota Dev Scooter Android Web Connector",
        "Connection: close"
    )
    fun clientLogin(
        @Field("phone") phone: String,
        @Field("name") name: String
    ): Observable<ClientLoginResponse>
}
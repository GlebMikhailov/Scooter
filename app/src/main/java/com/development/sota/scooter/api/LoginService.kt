package com.development.sota.scooter.api

import com.development.sota.scooter.ui.login.domain.entities.ClientLoginResponse
import com.development.sota.scooter.ui.login.domain.entities.UserBody
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LoginService {

    @POST("ClientLogIn/")
    fun clientLogin(
        @Body body: UserBody
    ): Observable<ClientLoginResponse>
}
package com.development.sota.scooter.ui.login.domain

import com.development.sota.scooter.api.LoginRetrofitProvider
import com.development.sota.scooter.ui.login.ui.LoginPresenter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers


interface LoginInteractor {
    fun sendLoginRequest(phone: String, name: String)
}

class LoginInteractorImpl(val presenter: LoginPresenter): LoginInteractor {
    val compositeDisposable = CompositeDisposable()


    override fun sendLoginRequest(phone: String, name: String) {
        compositeDisposable.add(
        LoginRetrofitProvider.service
            .clientLogin(phone, name).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy (onError = { presenter.gotErrorFromAPI() }, onNext = { presenter.gotCodeFromAPI(it.code) })
        )
    }
}
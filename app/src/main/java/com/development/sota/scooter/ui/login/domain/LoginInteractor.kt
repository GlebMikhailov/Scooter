package com.development.sota.scooter.ui.login.domain

import android.content.Context
import com.development.sota.scooter.api.LoginRetrofitProvider
import com.development.sota.scooter.ui.login.ui.LoginPresenter
import com.development.sota.scooter.ui.login.ui.fragments.code.LoginCodePresenter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers


interface LoginInteractor {
    fun sendLoginRequest(phone: String, name: String)
    fun saveCredentials(phone: String, name: String)
    fun disposeRequests()
}

class LoginInteractorImpl(val presenter: LoginPresenter): LoginInteractor {
    private val compositeDisposable = CompositeDisposable()
    private val sharedPreferences = presenter.context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    override fun sendLoginRequest(phone: String, name: String) {
        compositeDisposable.add(
        LoginRetrofitProvider.service
            .clientLogin(phone, name).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy (onError = { presenter.gotErrorFromAPI(it.localizedMessage) }, onNext = { presenter.gotCodeFromAPI(it.code) })
        )
    }

    override fun saveCredentials(phone: String, name: String) {
        sharedPreferences.edit().putString("phone", phone).putString("name", name).putBoolean("firstInit", true).apply()
    }

    override fun disposeRequests() {
        compositeDisposable.dispose()
        compositeDisposable.clear()
    }
}

class LoginCodeInteractorImpl(val presenter: LoginCodePresenter): LoginInteractor {
    private val compositeDisposable = CompositeDisposable()

    override fun sendLoginRequest(phone: String, name: String) {
        compositeDisposable.add(
            LoginRetrofitProvider.service
                .clientLogin(phone, name).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy (onError = { presenter.gotErrorFromAPI() }, onNext = { presenter.gotCodeFromAPI(it.code) })
        )
    }

    override fun saveCredentials(phone: String, name: String) {}

    override fun disposeRequests() {
        compositeDisposable.dispose()
        compositeDisposable.clear()
    }
}
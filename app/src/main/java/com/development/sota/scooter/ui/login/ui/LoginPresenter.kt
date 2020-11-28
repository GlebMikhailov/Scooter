package com.development.sota.scooter.ui.login.ui

import android.content.Context
import android.util.Log
import com.development.sota.scooter.ui.login.domain.LoginInteractor
import com.development.sota.scooter.ui.login.domain.LoginInteractorImpl
import moxy.MvpPresenter

class LoginPresenter(val context: Context): MvpPresenter<LoginView>()  {
    private val interactor: LoginInteractor = LoginInteractorImpl(this)

    private var phone = ""
    private var name =""

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        viewState.setFragmentInput()
    }

    fun onCodeRequested(phone: String, name: String) {
        this.phone = phone
        this.name = name

        viewState.hideInputFragment()

        interactor.sendLoginRequest(phone, name)

        viewState.setLoginProgressBarVisibility(true)
    }

    fun gotCodeFromAPI(code: Int) {
        viewState.setLoginProgressBarVisibility(false)

        viewState.setFragmentCode(code)
    }

    fun gotErrorFromAPI(message: String) {
        Log.w("ERROR", message)
        viewState.setLoginProgressBarVisibility(false)

        viewState.showToastWarning()
    }

    fun onCloseCodeFragment(result: Boolean) {
        if(result) {
            interactor.saveCredentials(phone, name)
            viewState.finishActivity()
        } else {
            viewState.setFragmentInput()
        }
    }

    fun getPhoneAndName(): Pair<String, String> {
        return Pair(phone, name)
    }

    fun onDestroyCalled() {
        interactor.disposeRequests()
    }
}
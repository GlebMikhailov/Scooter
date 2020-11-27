package com.development.sota.scooter.ui.login.ui

import com.development.sota.scooter.ui.login.domain.LoginInteractor
import com.development.sota.scooter.ui.login.domain.LoginInteractorImpl
import moxy.MvpPresenter

class LoginPresenter: MvpPresenter<LoginView>()  {
    private var countryCode = ""
    private var correctLoginCode = 0

    private val interactor: LoginInteractor = LoginInteractorImpl(this)

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        viewState.setFragmentInput()
    }

    fun gotCodeFromAPI(code: Int) {
        correctLoginCode = code
        viewState.setFragmentCode()
    }

    fun gotErrorFromAPI() {
        viewState.showToastWarning()
    }
}
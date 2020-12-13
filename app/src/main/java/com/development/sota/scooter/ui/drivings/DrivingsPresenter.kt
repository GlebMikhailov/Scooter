package com.development.sota.scooter.ui.drivings

import android.content.Context
import com.development.sota.scooter.ui.drivings.domain.DrivingsInteractor
import com.development.sota.scooter.ui.drivings.domain.DrivingsInteractorImpl
import com.development.sota.scooter.ui.map.data.Scooter
import moxy.MvpPresenter

class DrivingsPresenter(val context: Context) : MvpPresenter<DrivingsView>() {
    private val interactor: DrivingsInteractor = DrivingsInteractorImpl(this)

    lateinit var fragmentType: DrivingsListFragmentType

    private var scooters = arrayListOf<Scooter>()
    private var codeToTest: Long? = null

    private var fragmentDelegate: DrivingsFragmentView? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
    }

    fun testCode(code: Long, delegate: DrivingsFragmentView) {
        codeToTest = code
        fragmentDelegate = delegate

        interactor.getAllAvailableScooters()
    }

    fun returnedFromQRSendToCode() {
        fragmentType = DrivingsListFragmentType.CODE

        viewState.setFragmentByType(DrivingsListFragmentType.CODE)
    }

    fun updateFragmentType(type: DrivingsListFragmentType) {
        fragmentType = type

        viewState.setFragmentByType(type)
    }

    fun gotScootersFromAPI(scooters: ArrayList<Scooter>) {
        this.scooters = scooters

        if (codeToTest != null) {
            if (this.scooters.count { it.id == codeToTest } != 0) {
                fragmentDelegate?.gotResultOfCodeChecking(true)
                TODO("Add driving and close fragment")
            } else {
                fragmentDelegate?.gotResultOfCodeChecking(false)
            }

            fragmentDelegate = null
        }
    }

    fun gotErrorFromAPI(message: String) {
        viewState.showToast(message)
    }

}
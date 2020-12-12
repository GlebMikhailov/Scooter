package com.development.sota.scooter.ui.drivings.presentation

import com.development.sota.scooter.base.BasePresenter
import moxy.MvpPresenter
import moxy.MvpView

class QRPresenter: MvpPresenter<MvpView>(), BasePresenter {
    private var errorCount = 0

    override fun onDestroyCalled() {
        TODO("Not yet implemented")
    }

    fun gotErrorFromScanner() {
        if(++errorCount >= 3) {
            //TODO: Send to code
        }
    }

    fun getDataFromScanner(data: String) {
        val parsed = parseQRData(data)?.toIntOrNull()

        if(parsed == null) {
            gotErrorFromScanner()
        } else {

        }
    }

    // If fails - return null
    // Example: scooter.sota/qr?id=1234
    private fun parseQRData(data: String): String? {
        val index = data.indexOf("id=")

        if(index == -1) {
            return null
        } else {
            return data.substringAfter("id=")
        }
    }
}
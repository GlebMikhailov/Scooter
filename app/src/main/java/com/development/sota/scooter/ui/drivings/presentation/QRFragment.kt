package com.development.sota.scooter.ui.drivings.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.development.sota.scooter.databinding.FragmentLoginInputBinding
import com.development.sota.scooter.databinding.FragmentQrBinding
import com.development.sota.scooter.ui.drivings.DrivingsActivityView
import com.development.sota.scooter.ui.drivings.DrivingsView
import moxy.MvpAppCompatFragment
import moxy.MvpView
import moxy.ktx.moxyPresenter
import moxy.viewstate.strategy.alias.AddToEnd

interface QRView: MvpView {
    @AddToEnd
    fun sendFoundCodeToDrivings(code: Int)
}

class QRFragment(val drivingsView: DrivingsActivityView): MvpAppCompatFragment(), QRView {
    private val presenter by moxyPresenter { QRPresenter() }

    private var _binding: FragmentQrBinding? = null
    private val binding get() = _binding!!

    private lateinit var codeScanner: CodeScanner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrBinding.inflate(inflater, container, false)

        codeScanner = CodeScanner(context ?: activity?.applicationContext!!, binding.qrScanner)

        codeScanner.autoFocusMode = AutoFocusMode.CONTINUOUS
        codeScanner.isAutoFocusEnabled = true

        binding.imageButtonQrScannerLantern.setOnClickListener {
            codeScanner.isFlashEnabled = !codeScanner.isAutoFocusEnabled
        }

        return binding.root
    }

    override fun sendFoundCodeToDrivings(code: Int) {
        activity?.runOnUiThread {
            drivingsView.gotCodeFromQRScanner(code)
        }
    }

    override fun onDestroy() {
        presenter.onDestroyCalled()
        super.onDestroy()
    }
}
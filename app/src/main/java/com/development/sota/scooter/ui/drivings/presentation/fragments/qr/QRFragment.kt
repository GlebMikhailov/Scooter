package com.development.sota.scooter.ui.drivings.presentation.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.development.sota.scooter.databinding.FragmentDrivingsQrBinding
import com.development.sota.scooter.ui.drivings.DrivingsActivityView
import com.development.sota.scooter.ui.drivings.DrivingsFragmentView
import com.development.sota.scooter.ui.drivings.DrivingsListFragmentType
import com.development.sota.scooter.ui.drivings.presentation.fragments.qr.QRPresenter
import com.development.sota.scooter.ui.map.data.Scooter
import moxy.MvpAppCompatFragment
import moxy.MvpView
import moxy.ktx.moxyPresenter
import moxy.viewstate.strategy.alias.AddToEnd

interface QRView : MvpView {
    @AddToEnd
    fun sendFoundCodeToDrivings(code: Long)

    @AddToEnd
    fun setLoading(by: Boolean)

    @AddToEnd
    fun sendToCodeFragment()

    @AddToEnd
    fun finishActivity(code: Long)
}

class QRFragment(val drivingsView: DrivingsActivityView) : MvpAppCompatFragment(), QRView,
    DrivingsFragmentView {
    private val presenter by moxyPresenter { QRPresenter() }

    private var _binding: FragmentDrivingsQrBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraManager: CameraManager
    private var isFlashAvailable = true
    private var isOn = false

    private lateinit var codeScanner: CodeScanner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrivingsQrBinding.inflate(inflater, container, false)

        codeScanner = CodeScanner(context ?: activity?.applicationContext!!, binding.qrScanner)

        codeScanner.autoFocusMode = AutoFocusMode.CONTINUOUS
        codeScanner.isAutoFocusEnabled = true

        codeScanner.setDecodeCallback {
            presenter.getDataFromScanner(it.text)
        }
        codeScanner.setErrorCallback {
            presenter.gotErrorFromScanner()
        }

        codeScanner.startPreview()

        binding.imageButtonQrScannerLantern.setOnClickListener {
            codeScanner.isFlashEnabled = !codeScanner.isFlashEnabled
        }

        binding.imageButtonQrScannerCode.setOnClickListener {
            drivingsView.sendToCodeActivity()
        }

        binding.imageButtonQrScannerBack.setOnClickListener {
            drivingsView.onBackPressedByType(DrivingsListFragmentType.QR)
        }
        /*********** Added by kon3gor *************/

        isFlashAvailable = requireActivity().applicationContext.packageManager
            .hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)

        cameraManager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager

        /*********** Added by kon3gor *************/

        return binding.root
    }

    override fun sendFoundCodeToDrivings(code: Long) {
        activity?.runOnUiThread {
            drivingsView.gotCode(code, this)
        }
    }

    override fun setLoading(by: Boolean) {
        activity?.runOnUiThread {
            if (by) {
                binding.progressBarDrivingsQr.visibility = View.VISIBLE
                codeScanner.stopPreview()
            } else {
                binding.progressBarDrivingsQr.visibility = View.GONE
                codeScanner.startPreview()
            }
        }
    }

    fun toggleLantern() {
        activity?.runOnUiThread {
            codeScanner.isFlashEnabled = !codeScanner.isFlashEnabled
        }
    }

    override fun sendToCodeFragment() {
        activity?.runOnUiThread {
            codeScanner.stopPreview()

            drivingsView.sendToCodeActivity()
        }
    }

    override fun finishActivity(code: Long) {
        val i = Intent().apply {
            putExtra("scooter_id", code)
        }
        val res = if (code != -1L) Activity.RESULT_OK else Activity.RESULT_CANCELED
        requireActivity().setResult(res, i)
        requireActivity().finishAndRemoveTask()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.runOnUiThread {
            try {
                codeScanner.startPreview()
            } catch (e: Exception) {
            }
        }
    }

    /*************** Added by kon3gor  *****************/

    fun toogleLight(v: View){
        if (isFlashAvailable){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val id = cameraManager.cameraIdList[0]
                cameraManager.setTorchMode(id, !isOn)

            }else{
                val mCamera = Camera.open();
                val parameters = mCamera.getParameters();
                if (isOn){
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }else{
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                }
                mCamera.setParameters(parameters);
                mCamera.stopPreview();
            }
            isOn = !isOn
        }
    }

    /*************** Added by kon3gor  *****************/

    override fun onDestroy() {
        presenter.onDestroyCalled()
        super.onDestroy()
    }

    override fun gotResultOfCodeChecking(result: Boolean, scooter: Scooter?) {
        presenter.gotResponseFromActivity(result, scooter)
    }
}
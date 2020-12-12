package com.development.sota.scooter.ui.drivings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.development.sota.scooter.R
import com.development.sota.scooter.databinding.ActivityDrivingsBinding
import com.development.sota.scooter.databinding.ActivityLoginBinding
import moxy.MvpAppCompatActivity
import moxy.MvpView

interface DrivingsView: MvpView {

}

interface DrivingsActivityView {
    fun gotCodeFromQRScanner(code: Int)
}

class DrivingsActivity : MvpAppCompatActivity(), MvpView, DrivingsActivityView {
    private var _binding: ActivityDrivingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDrivingsBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }

    override fun gotCodeFromQRScanner(code: Int) {

    }
}
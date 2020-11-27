package com.development.sota.scooter

import android.content.Intent
import android.os.Bundle
import com.development.sota.scooter.ui.login.ui.LoginActivity
import moxy.MvpAppCompatActivity

class MainActivity : MvpAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startActivity(Intent(this, LoginActivity::class.java))
    }
}
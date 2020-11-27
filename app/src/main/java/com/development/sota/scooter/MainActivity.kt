package com.development.sota.scooter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import com.development.sota.scooter.ui.login.ui.LoginActivity
import com.development.sota.scooter.ui.tutorial.presentation.TutorialActivity
import moxy.MvpAppCompatActivity

class MainActivity : MvpAppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)

        if(!sharedPreferences.getBoolean("firstInit", false)) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else if(!sharedPreferences.getBoolean("wasTutorial", false)) {
            startActivity(Intent(this, TutorialActivity::class.java))
        }
    }
}
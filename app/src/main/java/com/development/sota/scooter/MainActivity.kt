package com.development.sota.scooter

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.development.sota.scooter.ui.login.presentation.LoginActivity
import com.development.sota.scooter.ui.map.presentation.MapActivity
import com.development.sota.scooter.ui.tutorial.presentation.TutorialActivity
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import moxy.MvpAppCompatActivity


class MainActivity : MvpAppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCenter.start(
            application, "0a591f1a-d402-4b85-be43-57d7c041228a",
            Analytics::class.java, Crashes::class.java
        )

        sharedPreferences = getSharedPreferences("account", Context.MODE_PRIVATE)

    if (getConnection(this)) {
        if (!sharedPreferences.getBoolean("firstInit", false)) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else if (!sharedPreferences.getBoolean("wasTutorial", false)) {
            startActivity(Intent(this, TutorialActivity::class.java))
        } else {
            startActivity(Intent(this, MapActivity::class.java))
        }

    } else {
        dialog(this,false)
    }



    }
    fun getConnection(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Activity.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
        // ?????????????????? ??????
    }
    fun dialog(context: Context, boolean: Boolean) {
        var dialog: AlertDialog? = null
        val builder = AlertDialog.Builder(context)
        val i = LayoutInflater.from(context)
        val dilog: View = i.inflate(R.layout.no_internet_dialog, null)
        builder!!.setView(dilog)
        val no_internet_ok = dilog.findViewById(R.id.no_internet_ok) as TextView

        no_internet_ok.setOnClickListener {
            if (getConnection(context)) {
                dialog!!.dismiss()
            }

        }
        dialog = builder.create()
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.setCanceledOnTouchOutside(false)
        if (!boolean) {
            dialog.show()
        } else {
            dialog.dismiss()
        }


        dialog!!.getWindow()?.setLayout(800, ViewGroup.LayoutParams.WRAP_CONTENT);

    }
}
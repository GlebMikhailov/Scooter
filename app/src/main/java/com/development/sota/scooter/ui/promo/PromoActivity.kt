package com.development.sota.scooter.ui.promo

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.development.sota.scooter.R
import com.development.sota.scooter.WifiReceiver
import com.development.sota.scooter.ui.map.presentation.MapActivity
import kotlinx.android.synthetic.main.activity_promo.*


class PromoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_promo)
        var frameLayout = findViewById<FrameLayout>(R.id.promo_main_list)
        Log.d("active_activity", "Promo")
        val manager: FragmentManager = supportFragmentManager
        val transaction: FragmentTransaction = manager.beginTransaction()
        transaction.add(R.id.promo_main_list, PromoListFragment())
        transaction.addToBackStack(null)
        transaction.commit()

        main_promo_image_button_back.setOnClickListener {
          val i = Intent(this, MapActivity::class.java)
            startActivity(i)
        }
        mNetworkReceiver = WifiReceiver()
        registerNetworkBroadcastForNougat()


    }
    private var mNetworkReceiver: BroadcastReceiver? = null
    private fun registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(
                mNetworkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(
                mNetworkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    protected fun unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterNetworkChanges()
    }
}
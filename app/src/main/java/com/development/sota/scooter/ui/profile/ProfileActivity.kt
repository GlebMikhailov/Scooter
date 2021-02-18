package com.development.sota.scooter.ui.profile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.development.sota.scooter.R
import com.development.sota.scooter.WHATSAPP_PHONE_NUMBER
import com.development.sota.scooter.WifiReceiver
import com.development.sota.scooter.db.SharedPreferencesProvider
import com.development.sota.scooter.ui.login.presentation.LoginActivity
import com.development.sota.scooter.ui.profile.adapters.ProfileEditingAdapter
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    var adapter: ProfileEditingAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


        profile_image_button_back.setOnClickListener {
            onBackPressed()
        }

        val titles = arrayListOf("Номер телефона", "Имя")
        val phoneNumber = getSharedPreferences("account", Context.MODE_PRIVATE).getString("phone", "phone").toString()
        val name = getSharedPreferences("account", Context.MODE_PRIVATE).getString("name", "name").toString()

        val hint = arrayListOf(phoneNumber, name)

        val recyclerView: RecyclerView = findViewById(R.id.profile_editing_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProfileEditingAdapter(this, titles, hint)

        recyclerView.adapter = adapter
       // val dividerItemDecoration = DividerItemDecoration(
       //     recyclerView.context,
       //     LinearLayout.VERTICAL
       // )
       // recyclerView.addItemDecoration(dividerItemDecoration)

        sign_out.setOnClickListener {
            signOut()
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
    fun signOut() {
        val sharedPreferences = SharedPreferencesProvider(this).sharedPreferences
        sharedPreferences.edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
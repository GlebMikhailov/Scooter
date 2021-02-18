package com.development.sota.scooter.ui.help

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout.VERTICAL
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.development.sota.scooter.R
import com.development.sota.scooter.WHATSAPP_PHONE_NUMBER
import com.development.sota.scooter.WifiReceiver
import com.development.sota.scooter.ui.help.adapters.MyRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_help.*


class HelpActivity : AppCompatActivity() {
    var adapter: MyRecyclerViewAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        help_image_button_back.setOnClickListener {
            onBackPressed()
        }

        val titles = arrayListOf("Telegram", "WhatsApp")
        val recyclerView: RecyclerView = findViewById(R.id.help_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MyRecyclerViewAdapter(this, titles)

        recyclerView.adapter = adapter
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            VERTICAL
        )
        recyclerView.addItemDecoration(dividerItemDecoration)

        phone_call.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", WHATSAPP_PHONE_NUMBER, null))
            startActivity(intent)
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
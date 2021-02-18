package com.development.sota.scooter

import android.app.ActionBar
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

class WifiReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (getConnection(context)) {
            Log.d("getConnection", "true")
            dialog(context, true)
        } else {
            Log.d("getConnection", "false")
          //  Toast.makeText(context, R.string.error_api, Toast.LENGTH_LONG).show()
          //  no_internet_ok
            dialog(context, false)
        }
    }

    fun getConnection(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Activity.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
        // интернета нет
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
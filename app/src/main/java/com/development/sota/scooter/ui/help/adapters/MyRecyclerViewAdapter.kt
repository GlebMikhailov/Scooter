package com.development.sota.scooter.ui.help.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.development.sota.scooter.R
import com.development.sota.scooter.TELEGRAM
import com.development.sota.scooter.WHATSAPP_PHONE_NUMBER


class MyRecyclerViewAdapter internal constructor(
    var context: Context,
    title: List<String>,
) : RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>() {
    private val title: List<String>
    private val mInflater: LayoutInflater
    private var item: View? = null
    private var mClickListener: ItemClickListener? = null

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.item_help_list, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = title[position]

        item!!.setOnClickListener {
            when (position) {
                0 -> {
                    // use country code with your phone number
                    val telegram = Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/$TELEGRAM"))
                    context.startActivity(telegram)

                }
                1 -> {
                    val url = "https://api.whatsapp.com/send?phone=$WHATSAPP_PHONE_NUMBER"
                    try {
                        val pm: PackageManager = context.packageManager
                        pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(url)
                        context.startActivity(i)
                    } catch (e: PackageManager.NameNotFoundException) {
                        Toast.makeText(
                            context,
                            "Приложение WhatsApp не установлено на ваше устройство",
                            Toast.LENGTH_LONG
                        ).show()
                        e.printStackTrace()
                    }
                }
            }
        }
        //context.getDrawable(R.drawable.ic_purple_gift)

    }

    // total number of rows
    override fun getItemCount(): Int {
        return title.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var title: TextView
        var go: ImageButton


        //          android:drawablePadding="-96dp"
//            android:paddingStart="96dp"
//            android:drawableStart="@drawable/ic_link"
        override fun onClick(view: View) {
          //  if (mClickListener != null) mClickListener!!.onItemClick(view, adapterPosition)


        }

        init {
            item = itemView
            title = itemView.findViewById(R.id.help_title_chat)
            go = itemView.findViewById(R.id.help_go)
            itemView.setOnClickListener(this)
        }
    }

    // convenience method for getting data at click position
    fun getItem(id: Int): String {
        return title[id]
    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int) {

        }
    }

    // data is passed into the constructor
    init {
        mInflater = LayoutInflater.from(context)
        this.title = title
    }
}
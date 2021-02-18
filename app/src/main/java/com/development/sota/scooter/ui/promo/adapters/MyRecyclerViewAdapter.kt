package com.development.sota.scooter.ui.promo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.development.sota.scooter.R
import com.development.sota.scooter.ui.promo.adapters.MyRecyclerViewAdapter
import com.development.sota.scooter.ui.promo.InviteFriendsActivity
import com.google.android.material.button.MaterialButton


class MyRecyclerViewAdapter internal constructor(
    var context: Context,
    title: List<String>,
    additionText: List<String>,
    backgrounds: List<Int>,
    buttonText: List<String>
) : RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>() {
    private val title: List<String>
    private val additionText: List<String>
    private val backgrounds: List<Int>
    private val buttonText: List<String>
    private val mInflater: LayoutInflater
    private var mClickListener: ItemClickListener? = null

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.item_promo_main, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = title[position]
        holder.description.text = additionText[position]
        holder.image.setBackgroundResource(backgrounds[position]) //context.getDrawable(R.drawable.ic_purple_gift)

        if (buttonText[position] == "Пригласить") {
            holder.button.icon = context.getDrawable(R.drawable.ic_link)
            holder.button.text = buttonText[position]
            holder.button.setOnClickListener {
                val i = Intent(context, InviteFriendsActivity::class.java)
                context.startActivity(i)
            }
        } else if (position == 0){
            holder.button.icon = null
            holder.button.text = buttonText[position]
            holder.button.setOnClickListener {

            }
        }
    }

    // total number of rows
    override fun getItemCount(): Int {
        return title.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var title: TextView
        var description: TextView
        var image: ImageView
        var button: MaterialButton


        //          android:drawablePadding="-96dp"
//            android:paddingStart="96dp"
//            android:drawableStart="@drawable/ic_link"
        override fun onClick(view: View) {
            if (mClickListener != null) mClickListener!!.onItemClick(view, adapterPosition)
        }

        init {
            title = itemView.findViewById(R.id.title_promo_main)
            description = itemView.findViewById(R.id.additional_text_promo_main)
            image = itemView.findViewById(R.id.image_promo_main)
            button = itemView.findViewById(R.id.button_promo_main)
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
        fun onItemClick(view: View?, position: Int)
    }

    // data is passed into the constructor
    init {
        mInflater = LayoutInflater.from(context)
        this.title = title
        this.additionText = additionText
        this.backgrounds = backgrounds
        this.buttonText = buttonText
    }
}
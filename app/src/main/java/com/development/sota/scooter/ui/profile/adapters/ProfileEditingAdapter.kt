package com.development.sota.scooter.ui.profile.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.development.sota.scooter.BASE_URL
import com.development.sota.scooter.R
import com.development.sota.scooter.db.SharedPreferencesProvider
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.watchers.FormatWatcher
import ru.tinkoff.decoro.watchers.MaskFormatWatcher


class ProfileEditingAdapter internal constructor(
    var context: Context,
    titles: List<String>,
    hint: List<String>,
) : RecyclerView.Adapter<ProfileEditingAdapter.ViewHolder>() {
    private val titles: List<String>
    private val hint: List<String>
    private val mInflater: LayoutInflater
    private var item: View? = null
    private var mClickListener: ItemClickListener? = null

    val sharedPreferences = SharedPreferencesProvider(context).sharedPreferences

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.item_account_list, parent, false)
        return ViewHolder(view)
    }


    fun rotateImage(from: Float, to: Float, button: ImageButton) {
        val rotate = RotateAnimation(
            from,
            to,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotate.duration = 200
        rotate.fillAfter = true
        button.startAnimation(rotate)
    }

    // binds the data to the TextView in each row
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = titles[position]
        holder.input.setText(hint[position])
        //holder.input.getBackground().mutate().setColorFilter(context.getResources().getColor(R.color.green_edit_text), PorterDuff.Mode.SRC_ATOP);


        when (position) {
            0 -> {
                holder.input.inputType = InputType.TYPE_CLASS_PHONE;

                val slots = UnderscoreDigitSlotsParser().parseSlots("7 ___ ___ __ __")
                val formatWatcher: FormatWatcher =
                    MaskFormatWatcher( // форматировать текст будет вот он
                        MaskImpl.createTerminated(slots)
                    )
                formatWatcher.installOn(holder.input)

                holder.input.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        Log.d("edit_text_profile", "${p0.toString().length}, $p1, $p2, $p3")
                        if (p2 >= 15 && p0.toString().length >= 15) {
                            rotateImage(0F, 90F, holder.button)
                            holder.input.backgroundTintList =
                                context.resources.getColorStateList(R.color.green_edit_text)
                            holder.button.backgroundTintList =
                                context.resources.getColorStateList(R.color.green_edit_text)
                        } else if (p2 < 15 && p0.toString().length < 15) {
                            rotateImage(90F, 0F, holder.button)
                            holder.input.backgroundTintList =
                                context.resources.getColorStateList(R.color.gray)
                            holder.button.backgroundTintList =
                                context.resources.getColorStateList(R.color.gray)

                        }

                    }

                    override fun afterTextChanged(p0: Editable?) {

                    }

                })
            }
            1 -> {
                holder.input.inputType = InputType.TYPE_CLASS_TEXT;
                holder.input.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        if (p0.toString().isNotEmpty() && p2 > 0) {
                            rotateImage(0F, 90F, holder.button)
                            holder.input.backgroundTintList =
                                context.resources.getColorStateList(R.color.green_edit_text)
                            holder.button.backgroundTintList =
                                context.resources.getColorStateList(R.color.green_edit_text)
                        } else {
                            rotateImage(90F, 0F, holder.button)
                            holder.input.backgroundTintList =
                                context.resources.getColorStateList(R.color.gray)
                            holder.button.backgroundTintList =
                                context.resources.getColorStateList(R.color.gray)
                        }


                    }

                    override fun afterTextChanged(p0: Editable?) {

                    }

                })
            }
        }

        holder.button.setOnClickListener {
            Log.d(" holder.button.clicked", "pos = $position")
            when (position) {
                0 -> {
                    sharedPreferences.edit().putString("phone", holder.input.text.toString())
                        .apply()
                    rotateImage(90F, 0F, holder.button)
                    holder.input.backgroundTintList =
                        context.resources.getColorStateList(R.color.gray)
                    holder.button.backgroundTintList =
                        context.resources.getColorStateList(R.color.gray)
                    //TODO: savePhoneNumber
                }
                1 -> {
                    sharedPreferences.edit().putString("name", holder.input.text.toString()).apply()
                    rotateImage(90F, 0F, holder.button)
                    holder.input.backgroundTintList =
                        context.resources.getColorStateList(R.color.gray)
                    holder.button.backgroundTintList =
                        context.resources.getColorStateList(R.color.gray)
                    //TODO: saveName
                    val thread = Thread {
                        try {
                            saveName(holder.input)
                        } catch (e: java.lang.Exception) {
                            Log.d("error_save_name", "er = $e")
                        }
                    }

                    thread.start()

                }
            }
        }


    }


    private fun saveName(editText: EditText) {


    }
    
    override fun getItemCount(): Int {
        return hint.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var title: TextView
        var button: ImageButton
        var input: EditText

        //          android:drawablePadding="-96dp"
//            android:paddingStart="96dp"
//            android:drawableStart="@drawable/ic_link"
        override fun onClick(view: View) {
            //  if (mClickListener != null) mClickListener!!.onItemClick(view, adapterPosition)


        }

        init {
            item = itemView
            title = itemView.findViewById(R.id.additional_text_editing)
            button = itemView.findViewById(R.id.finish_editing)
            input = itemView.findViewById(R.id.input_value_editing)

            itemView.setOnClickListener(this)
        }
    }

    // convenience method for getting data at click position
    fun getItem(id: Int): String {
        return titles[id]
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
        this.titles = titles
        this.hint = hint
    }
}
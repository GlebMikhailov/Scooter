package com.development.sota.scooter.ui.promo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.development.sota.scooter.R
import com.development.sota.scooter.ui.promo.adapters.MyRecyclerViewAdapter


class PromoListFragment : Fragment(), MyRecyclerViewAdapter.ItemClickListener{
    var adapter: MyRecyclerViewAdapter? = null

    var root: View? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_promo_list, container, false)

        val titles = arrayListOf<String>("Промокоды", "Пригласить друзей")
        val additionText = arrayListOf<String>("Активируйте промокод здесь", "Пригласи друга и получите оба бонусы на поездки")
        val backgrounds = arrayListOf<Int>(R.drawable.ic_purple_gift, R.drawable.ic_purple_gift)
        val buttonsText = arrayListOf<String>("Посмотреть", "Пригласить")
        val recyclerView: RecyclerView = root!!.findViewById(R.id.promo_main_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter =
            activity?.let { MyRecyclerViewAdapter(it, titles, additionText, backgrounds, buttonsText) }
        recyclerView.adapter = adapter
        return  root
    }

    override fun onItemClick(view: View?, position: Int) {

    }


}
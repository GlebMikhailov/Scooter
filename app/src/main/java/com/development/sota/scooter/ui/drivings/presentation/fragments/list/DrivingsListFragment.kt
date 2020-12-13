package com.development.sota.scooter.ui.drivings.presentation.fragments.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.development.sota.scooter.databinding.FragmentDrivingsListBinding
import com.development.sota.scooter.ui.drivings.domain.entities.Order
import com.development.sota.scooter.ui.drivings.domain.entities.OrderWithStatus
import moxy.MvpAppCompatFragment
import moxy.MvpView
import moxy.ktx.moxyPresenter
import moxy.viewstate.strategy.alias.AddToEnd

interface DrivingsListView : MvpView {
    @AddToEnd
    fun showToast(message: String)

    @AddToEnd
    fun setLoading(by: Boolean)

    @AddToEnd
    fun initViewPager2(data: Pair<ArrayList<OrderWithStatus>, ArrayList<OrderWithStatus>>)
}

interface OrderManipulatorDelegate {
    fun cancelOrder(id: Long)

    fun activateOrder(id: Long)

    fun setRate() //WTF
}

class DrivingsListFragment(val additionalOrder: Order) : MvpAppCompatFragment(), DrivingsListView {
    private val presenter by moxyPresenter {
        DrivingsListPresenter(
            context ?: activity?.applicationContext!!
        )
    }

    private var _binding: FragmentDrivingsListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrivingsListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun showToast(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(context!!, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun setLoading(by: Boolean) {
        activity?.runOnUiThread {
            if (by) {
                binding.progressBarDrivingsList.visibility = View.VISIBLE
            } else {
                binding.progressBarDrivingsList.visibility = View.GONE
            }
        }
    }

    override fun initViewPager2(data: Pair<ArrayList<OrderWithStatus>, ArrayList<OrderWithStatus>>) {
        activity?.runOnUiThread {
            binding.viewPager2DrivingsList.adapter = DrivingsListViewPager2Adapter(context!!, data)
        }
    }

    override fun onDestroy() {
        presenter.onDestroyCalled()
        super.onDestroy()
    }
}
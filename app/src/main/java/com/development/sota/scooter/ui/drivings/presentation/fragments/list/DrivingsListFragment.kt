package com.development.sota.scooter.ui.drivings.presentation.fragments.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.development.sota.scooter.databinding.FragmentDrivingsListBinding
import com.development.sota.scooter.ui.drivings.DrivingsActivity
import com.development.sota.scooter.ui.drivings.DrivingsActivityView
import com.development.sota.scooter.ui.drivings.DrivingsListFragmentType
import com.development.sota.scooter.ui.drivings.domain.entities.Order
import com.development.sota.scooter.ui.drivings.domain.entities.OrderWithStatus
import com.development.sota.scooter.ui.map.data.RateType
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

    fun setRateAndActivate(id: Long, type: RateType)
}

class DrivingsListFragment(val drivingsView: DrivingsActivityView): MvpAppCompatFragment(), DrivingsListView, OrderManipulatorDelegate {
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

        binding.segmentedControlDrivingsList.setSelectedSegment(0)
        binding.segmentedControlDrivingsList.setOnSegmentSelectRequestListener {
            binding.viewPager2DrivingsList.setCurrentItem(it.absolutePosition, true)

            return@setOnSegmentSelectRequestListener true
        }

        binding.imageButtonDrivingsListBack.setOnClickListener {
            drivingsView.onBackPressedByType(DrivingsListFragmentType.LIST)
        }

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
                binding.segmentedControlDrivingsList.isEnabled = false
                binding.viewPager2DrivingsList.isEnabled = false
            } else {
                binding.progressBarDrivingsList.visibility = View.GONE
                binding.segmentedControlDrivingsList.isEnabled = true
                binding.viewPager2DrivingsList.isEnabled = true
            }
        }
    }

    override fun initViewPager2(data: Pair<ArrayList<OrderWithStatus>, ArrayList<OrderWithStatus>>) {
        activity?.runOnUiThread {
            binding.viewPager2DrivingsList.adapter = DrivingsListViewPager2Adapter(activity!!, data, this)
        }
    }

    override fun cancelOrder(id: Long) {
        presenter.cancelOrder(id)
    }

    override fun activateOrder(id: Long) {
        presenter.activateOrder(id)
    }

    override fun setRateAndActivate(id: Long, type: RateType) {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        presenter.onDestroyCalled()
        super.onDestroy()
    }
}
package com.development.sota.scooter.ui.drivings.presentation.fragments.list

import android.content.Context
import com.development.sota.scooter.base.BasePresenter
import com.development.sota.scooter.ui.drivings.domain.DrivingsListInteractor
import com.development.sota.scooter.ui.drivings.domain.DrivingsListInteractorImpl
import com.development.sota.scooter.ui.drivings.domain.entities.Order
import com.development.sota.scooter.ui.drivings.domain.entities.OrderStatus
import com.development.sota.scooter.ui.drivings.domain.entities.OrderWithStatus
import com.development.sota.scooter.ui.map.data.Scooter
import com.development.sota.scooter.ui.map.data.ScooterStatus
import moxy.MvpPresenter

class DrivingsListPresenter(val context: Context) : MvpPresenter<DrivingsListView>(),
    BasePresenter {
    private val interactor: DrivingsListInteractor = DrivingsListInteractorImpl(this)
    private var orders = arrayListOf<Order>()
    private var ordersWithStatuses = arrayListOf<OrderWithStatus>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        viewState.setLoading(true)
        interactor.getAllOrdersByClientID()
    }

    fun gotOrdersFromServer(orders: ArrayList<Order>) {
        this.orders = orders

        ordersWithStatuses.clear()

        for(order in orders) {
            when(order.scooter.status) {
                ScooterStatus.BOOKED.value ->
                    ordersWithStatuses.add(OrderWithStatus(order, OrderStatus.ACTIVATE))
                ScooterStatus.RENTED.value ->
                    ordersWithStatuses.add(OrderWithStatus(order, OrderStatus.IN_WORK))
                else ->
                    ordersWithStatuses.add(OrderWithStatus(order, OrderStatus.CLOSED))
            }
        }
    }

    fun gotErrorFromServer(message: String) {
        viewState.showToast(message)
    }

    override fun onDestroyCalled() {
        interactor.disposeRequests()
    }

}
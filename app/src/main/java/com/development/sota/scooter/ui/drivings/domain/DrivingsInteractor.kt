package com.development.sota.scooter.ui.drivings.domain

import com.development.sota.scooter.api.MapRetrofitProvider
import com.development.sota.scooter.api.OrdersRetrofitProvider
import com.development.sota.scooter.base.BaseInteractor
import com.development.sota.scooter.db.SharedPreferencesProvider
import com.development.sota.scooter.ui.drivings.DrivingsPresenter
import com.development.sota.scooter.ui.drivings.domain.entities.Order
import com.development.sota.scooter.ui.map.data.Scooter
import com.development.sota.scooter.ui.map.data.ScooterStatus
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers

interface DrivingsInteractor : BaseInteractor {
    fun getAllAvailableScooters()
    fun addOrder(order: Order)
}

class DrivingsInteractorImpl(private val presenter: DrivingsPresenter) : DrivingsInteractor {
    private val compositeDisposable = CompositeDisposable()
    private val sharedPreferences = SharedPreferencesProvider(presenter.context).sharedPreferences

    override fun getAllAvailableScooters() {
        compositeDisposable.add(
            MapRetrofitProvider.service
                .getScooters()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { list ->
                    list.filter {
                        it.status == ScooterStatus.ONLINE.value
                    }
                }
                .subscribeBy(
                    onError = { presenter.gotErrorFromAPI(it.localizedMessage ?: "") },
                    onNext = {
                        presenter.gotScootersFromAPI(it as ArrayList<Scooter>)
                    }
                )
        )
    }

    override fun addOrder(order: Order) {
        compositeDisposable.add(
            OrdersRetrofitProvider.service
                .addOrdersByClientID(
                    order.date,
                    order.startTime,
                    order.finishTime,
                    order.scooter.id,
                    sharedPreferences.getLong("id", -1),
                    1L //TODO: Replace to real rate
                ).subscribe()
        )
    }

    override fun disposeRequests() {
        compositeDisposable.clear()
    }

}



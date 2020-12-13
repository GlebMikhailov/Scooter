package com.development.sota.scooter.ui.drivings.domain

import com.development.sota.scooter.api.OrdersRetrofitProvider
import com.development.sota.scooter.base.BaseInteractor
import com.development.sota.scooter.db.SharedPreferencesProvider
import com.development.sota.scooter.ui.drivings.domain.entities.Order
import com.development.sota.scooter.ui.drivings.presentation.fragments.list.DrivingsListPresenter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers

interface DrivingsListInteractor : BaseInteractor {
    fun getAllOrdersByClientID()
}

class DrivingsListInteractorImpl(private val presenter: DrivingsListPresenter) :
    DrivingsListInteractor {
    private val compositeDisposable = CompositeDisposable()
    private val sharedPreferences = SharedPreferencesProvider(presenter.context).sharedPreferences

    override fun getAllOrdersByClientID() {
        compositeDisposable.add(
            OrdersRetrofitProvider.service
                .getOrdersByClientID(sharedPreferences.getLong("id", -1L))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onError = { presenter.gotErrorFromServer(it.localizedMessage ?: "") },
                    onNext = {
                        presenter.gotOrdersFromServer(it as ArrayList<Order>)
                    }
                )
        )
    }

    override fun disposeRequests() {
        compositeDisposable.clear()
    }

}
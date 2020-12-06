package com.development.sota.scooter.ui.tutorial.domain

import android.content.Context
import com.development.sota.scooter.base.BaseInteractor
import com.development.sota.scooter.ui.tutorial.presentation.TutorialPresenter

interface TutorialInteractor: BaseInteractor {
    fun setSuccessfulFlag()
}

class TutorialInteractorImpl(presenter: TutorialPresenter): TutorialInteractor {
    private val sharedPreferences = presenter.context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    override fun setSuccessfulFlag() {
        sharedPreferences.edit().putBoolean("wasTutorial", true).apply()
    }

    override fun disposeRequests() {}
}
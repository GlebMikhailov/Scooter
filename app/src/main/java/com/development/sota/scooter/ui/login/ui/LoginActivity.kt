package com.development.sota.scooter.ui.login.ui

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.development.sota.scooter.R
import com.development.sota.scooter.ui.login.ui.fragments.input.LoginInputFragment
import moxy.MvpAppCompatActivity
import moxy.MvpAppCompatFragment
import moxy.MvpView
import moxy.ktx.moxyPresenter
import moxy.viewstate.strategy.alias.AddToEnd

interface LoginView : MvpView {
    @AddToEnd
    fun setFragmentInput()

    @AddToEnd
    fun setFragmentCode()

    @AddToEnd
    fun showToastWarning()

    //TODO: Add fragment theory
}

class LoginActivity : MvpAppCompatActivity(),
    LoginView {

    private val presenter: LoginPresenter by moxyPresenter { LoginPresenter() }
    private var state = LoginState.INPUT

    private var saveInputFragment: MvpAppCompatFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    override fun setFragmentInput() {
        if(saveInputFragment == null) {
            saveInputFragment = LoginInputFragment()
        }

        supportFragmentManager.beginTransaction().apply {
            add(R.id.login_frame, saveInputFragment!!)
        }.commitNow()
    }

    override fun setFragmentCode() {
        val codeFragment = Fragment()

        supportFragmentManager.beginTransaction().apply {
            add(R.id.login_frame, codeFragment)
        }.commitNow()
    }

    override fun showToastWarning() {
        Toast.makeText(this, getString(R.string.login_oops), Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {}
}

enum class LoginState {
    INPUT, CODE
}
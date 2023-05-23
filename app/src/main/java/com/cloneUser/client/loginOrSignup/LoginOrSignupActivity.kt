package com.cloneUser.client.loginOrSignup

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseAppActivity
import com.cloneUser.client.databinding.ActivitySignupBinding
import com.cloneUser.client.ut.SessionMaintainence
import javax.inject.Inject

class LoginOrSignupActivity : BaseAppActivity<ActivitySignupBinding, LoginOrSignupVM>(),
    LoginOrSignupNavigator {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivitySignupBinding
    lateinit var navController: NavController

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(LoginOrSignupVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        if (!vm.session.getString(SessionMaintainence.CURRENT_LANGUAGE).isNullOrEmpty()) {
            vm.setLanguage(this, vm.session.getString(SessionMaintainence.CURRENT_LANGUAGE)!!)
        }else{
            vm.setLanguage(this,"en")
        }
        window.apply { navigationBarColor = getColor(R.color.colorPrimary) }
        navController = findNavController(R.id.navControlSignup)
        if (!vm.session.getBoolean(SessionMaintainence.FirstTimeEnglishCalled))
            vm.getSelectedLangBase("en")
        else
            initiate()
    }

    override fun getLayoutId() = R.layout.activity_signup

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    private fun initiate() {
        if (!vm.session.getBoolean(SessionMaintainence.GetStartedScrnLoaded))
            showGetStarted()
            //showTour()
        else
            showLogin()
    }

    private fun showGetStarted() {
        navController.navigate(R.id.getStartedScreen)
    }

    private fun showTour() {
        navController.navigate(R.id.toTourGuide)
    }

    private fun showLogin() {
        if(navController.currentDestination?.label?.equals("Get Started") == true)
            navController.navigate(R.id.toLogin)
    }

    override fun initiateNavigation() {
        initiate()
    }

    override fun showCustomDialog(message: String) {
        showCustomDialog(message)
    }
}
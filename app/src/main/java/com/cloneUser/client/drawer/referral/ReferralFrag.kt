package com.cloneUser.client.drawer.referral

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.databinding.FragmentReferralBinding
import javax.inject.Inject

class ReferralFrag : BaseFragment<FragmentReferralBinding, ReferralVM>(),
    ReferralNavigator {
    companion object {
        const val TAG = "FragmentReferralBinding"
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: FragmentReferralBinding

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(ReferralVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeFrag()
            }
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        binding.backImg.setOnClickListener { closeFrag() }
        vm.getReferralDetailsBase()
    }



    override fun getLayoutId() = R.layout.fragment_referral

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm


    private fun closeFrag(){
        findNavController().popBackStack()
    }

    override fun getAct(): FragmentActivity ?{
        return if(isAdded) requireActivity() else null
    }


}
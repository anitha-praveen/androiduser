package com.cloneUser.client.drawer.support

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.databinding.FragmentSupportBinding
import javax.inject.Inject

class SupportFragment : BaseFragment<FragmentSupportBinding, SupportVM>(),
    SupportNavigator {

    lateinit var _binding: FragmentSupportBinding
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val vm by lazy {
        ViewModelProvider(this, viewModelFactory).get(SupportVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeFragment()
            }
        })

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = getmBinding()
        vm.setNavigator(this)
        _binding.backImg.setOnClickListener {closeFragment()}
        vm.getAdminContactBase()
    }


    override fun getLayoutId() = R.layout.fragment_support

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    fun closeFragment(){
        findNavController().popBackStack()
    }

    override fun sos() {
        findNavController().navigate(R.id.support_to_sos)
    }

    override fun faq() {
        findNavController().navigate(R.id.support_to_faq)
    }

    override fun complaintsHistory() {
        findNavController().navigate(R.id.support_to_complaints_history)
    }


}
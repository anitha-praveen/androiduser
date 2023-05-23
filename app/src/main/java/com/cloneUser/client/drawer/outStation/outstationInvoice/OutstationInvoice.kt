package com.cloneUser.client.drawer.outStation.outstationInvoice

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.responseModels.RequestData
import com.cloneUser.client.databinding.OutstationInvoiceLayBinding
import javax.inject.Inject

class OutstationInvoice : BaseFragment<OutstationInvoiceLayBinding, OutstationInvoiceVm>(),
    OutstationInvoiceNavigator {
    companion object {
        const val TAG = "OutstationInvoice"
    }

    private lateinit var binding: OutstationInvoiceLayBinding


    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(OutstationInvoiceVm::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (vm.mode == 0) {
                    val bundle = bundleOf("REQUEST_DATA" to vm.requestData)
                    findNavController().navigate(R.id.outstation_invoice_to_rating, bundle)
                } else
                    findNavController().popBackStack()
            }
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        arguments?.let {
            vm.requestData = it.getSerializable("REQUEST_DATA") as? RequestData.Data
            vm.mode = it.getInt("MODE")
        }
        vm.setupInitialData()
    }

    override fun getLayoutId() = R.layout.outstation_invoice_lay

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    override fun chooseDirection() {
        if (vm.mode == 0) {
            val bundle = bundleOf("REQUEST_DATA" to vm.requestData)
            findNavController().navigate(R.id.outstation_invoice_to_rating, bundle)
        } else
            findNavController().popBackStack()
    }
}
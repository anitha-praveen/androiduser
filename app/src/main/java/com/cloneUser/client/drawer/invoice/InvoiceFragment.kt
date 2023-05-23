package com.cloneUser.client.drawer.invoice

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.cloneUser.client.BR
import com.cloneUser.client.MyApplication
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.responseModels.RequestData
import com.cloneUser.client.databinding.FragmentInvoiceBinding
import com.cloneUser.client.ut.Utilz
import javax.inject.Inject

class InvoiceFragment : BaseFragment<FragmentInvoiceBinding, InvoiceVM>(),
    InvoiceNavigator {
    companion object {
        const val TAG = "InvoiceFragment"
    }

    private lateinit var binding: FragmentInvoiceBinding
    //  lateinit var googleMap: GoogleMap

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory)[InvoiceVM::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (vm.mode == 0) {
                    val bundle = bundleOf("REQUEST_DATA" to vm.requestData)
                    findNavController().navigate(R.id.invoice_to_rating, bundle)
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

        /* If mode == 1 then this fragment have been called from completed history list*/
        if (vm.mode == 1)
            vm.buttonText.set(vm.translationModel.txt_Gotit)
        else{
            if (vm.requestData?.isLater == 1 && vm.requestData?.isCancelled != 1) {
                if (vm.requestData!!.isCompleted == 1)
                    vm.buttonText.set(vm.translationModel.txt_confirm)
                else
                    vm.buttonText.set(vm.translationModel.txt_cancel_ride)
            } else
                vm.buttonText.set(vm.translationModel.txt_confirm)
        }
    }

    override fun getLayoutId() = R.layout.fragment_invoice

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    override fun chooseDirection() {
        if (vm.mode == 0) {
            val bundle = bundleOf("REQUEST_DATA" to vm.requestData)
            findNavController().navigate(R.id.invoice_to_rating, bundle)
        } else  if (vm.requestData?.isLater == 1 && vm.requestData?.isCancelled != 1) {
            if (vm.requestData!!.isCompleted == 1)
                findNavController().popBackStack()
            else
                vm.cancelRideLaterTrip()
        }else
            findNavController().popBackStack()
    }

    override fun getCtx(): Context {
        if (isAdded)
            return requireContext()
        else
            return MyApplication.appContext!!
    }

    override fun getMarkerIcon(icPickPin: Int): BitmapDescriptor? {
        return Utilz.getBitmapFromDrawable(
            requireContext(),
            icPickPin
        )?.let {
            BitmapDescriptorFactory.fromBitmap(
                it
            )
        }
    }

    override fun close() {
        if (vm.mode == 0) {
            val bundle = bundleOf("REQUEST_DATA" to vm.requestData)
            findNavController().navigate(R.id.invoice_to_rating, bundle)
        } else
            findNavController().popBackStack()
    }

    override fun gotoComplaints() {
        val action = InvoiceFragmentDirections.invoiceToComplaints(1, vm.requestData?.id!!)
        findNavController().navigate(action)
    }
}
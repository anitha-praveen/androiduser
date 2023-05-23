package com.cloneUser.client.dialogs.cancelReasons

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseDialog
import com.cloneUser.client.connection.responseModels.CancelReason
import com.cloneUser.client.databinding.DialogCancelReasonsBinding
import com.cloneUser.client.dialogs.cancelReasons.adapter.CancelReasonsAdapter
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.Utilz
import javax.inject.Inject

class CancelReasonsDialog ():
    BaseDialog<DialogCancelReasonsBinding, CancelReasonsVM>(),
    CancelReasonsNavigator {
    companion object {
        const val TAG = "CancelReasonsDialog"
    }

     var reasons : MutableList<CancelReason.Reason> = mutableListOf()

    private lateinit var binding: DialogCancelReasonsBinding
    var adapter: CancelReasonsAdapter? = null
    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(CancelReasonsVM::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        setDialogFullSCreen()
        setupAdapter()

    }

    override fun getLayout(): Int = R.layout.dialog_cancel_reasons

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getViewModel(): CancelReasonsVM = vm

    private fun setupAdapter() {
        adapter = CancelReasonsAdapter(reasons, vm.session, vm.mConnect , this)
        val mLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.paymentMethodRecycler.layoutManager = mLayoutManager
        binding.paymentMethodRecycler.itemAnimator = DefaultItemAnimator()
        binding.paymentMethodRecycler.adapter = adapter
    }

    override fun close() {
        dismiss()
    }

    override fun sendCancelReason(reason: CancelReason.Reason) {
        val intent = Intent(Config.RECEIVE_CANCEL_REASON)
        intent.putExtra("REASON",reason)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        dismiss()
    }

    override fun setCancelReason(reason: CancelReason.Reason) {
        vm.reason = reason
    }

    override fun showSnackBar(message: String) =
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

    override fun isNetworkConnected(): Boolean = Utilz.checkForInternet(requireActivity())
    override fun showCustomDialog(message: String) {

    }

}
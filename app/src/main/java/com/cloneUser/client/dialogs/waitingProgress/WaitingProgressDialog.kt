package com.cloneUser.client.dialogs.waitingProgress


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ncorti.slidetoact.SlideToActView
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseDialog
import com.cloneUser.client.databinding.DialogWaitingProgressBinding
import com.cloneUser.client.ut.Config
import com.cloneUser.client.ut.SocketHelper
import com.cloneUser.client.ut.Utilz
import javax.inject.Inject


class WaitingProgressDialog(private val requestId : String) : BaseDialog<DialogWaitingProgressBinding, WaitingProgressVM>(),
    WaitingProgressNavigator , SlideToActView.OnSlideCompleteListener {
    companion object {
        const val TAG = "WaitingProgressDialog"
    }


    private lateinit var binding: DialogWaitingProgressBinding
    private val tripDetailsReceiver : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
                dismiss()
        }

    }
    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(WaitingProgressVM::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        setDialogFullSCreen()
        vm.requestId = requestId
        binding.slideMenu.onSlideCompleteListener = this

    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(tripDetailsReceiver, IntentFilter (Config.RECEIVE_TRIP_STATUS))
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(tripDetailsReceiver)
        super.onPause()
    }
    override fun getLayout(): Int = R.layout.dialog_waiting_progress


    override fun getBindingVariable(): Int = BR.viewModel


    override fun getViewModel(): WaitingProgressVM = vm
    override fun close() {
        dismiss()
    }


    override fun showSnackBar(message: String) =
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

    override fun isNetworkConnected(): Boolean = Utilz.checkForInternet(requireActivity())
    override fun showCustomDialog(message: String) {

    }


    override fun onSlideComplete(view: SlideToActView) {
        vm.cancelRequest()
    }

}